package com.devopsday.orders.adapter.in.rest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.devopsday.orders.application.port.in.GetOrderQuery;
import com.devopsday.orders.application.port.in.PlaceOrderUseCase;
import com.devopsday.orders.domain.exception.InvalidOrder;
import com.devopsday.orders.domain.exception.InvalidOrderState;
import com.devopsday.orders.domain.exception.OrderNotFound;
import com.devopsday.orders.domain.model.OrderId;
import com.devopsday.orders.testsupport.InMemoryMessagingProfile;
import com.devopsday.orders.testsupport.OrderFixtures;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@QuarkusTest
@TestProfile(InMemoryMessagingProfile.class)
class OrderResourceTest {

    private static final String PROBLEM_JSON = "application/problem+json";

    @InjectMock PlaceOrderUseCase placeOrder;
    @InjectMock GetOrderQuery getOrder;

    @Test
    void placeReturns201WithLocationAndBody() {
        var order = OrderFixtures.pendingOrder();
        when(placeOrder.place(any())).thenReturn(order);

        given().contentType(ContentType.JSON)
                .body(
                        """
            {"customerId":"customer-1","lines":[{"sku":"SKU-1","quantity":2},{"sku":"SKU-2","quantity":1}]}
            """)
                .when()
                .post("/api/v1/orders")
                .then()
                .statusCode(201)
                .header("Location", endsWith("/api/v1/orders/" + order.id()))
                .contentType(ContentType.JSON)
                .body("id", equalTo(order.id().toString()))
                .body("status", equalTo("PENDING"))
                .body("customerId", equalTo("customer-1"))
                .body("lines.sku", hasItems("SKU-1", "SKU-2"))
                .body("rejectionReason", nullValue());

        var command = ArgumentCaptor.forClass(PlaceOrderUseCase.Command.class);
        verify(placeOrder).place(command.capture());
        assertThat(command.getValue().customerId()).isEqualTo("customer-1");
        assertThat(command.getValue().lines())
                .containsExactly(
                        new PlaceOrderUseCase.Command.Line("SKU-1", 2),
                        new PlaceOrderUseCase.Command.Line("SKU-2", 1));
    }

    @Test
    void validationFailureReturnsProblemDetails() {
        given().contentType(ContentType.JSON)
                .body(
                        """
            {"customerId":" ","lines":[{"sku":"SKU-1","quantity":0}]}
            """)
                .when()
                .post("/api/v1/orders")
                .then()
                .statusCode(400)
                .contentType(PROBLEM_JSON)
                .body("type", equalTo("urn:problem:validation"))
                .body("title", equalTo("Validation failed"))
                .body("status", equalTo(400))
                .body("errors.field", hasItems("customerId", "lines[0].quantity"));

        verifyNoInteractions(placeOrder);
    }

    @Test
    void emptyLinesReturnProblemDetails() {
        given().contentType(ContentType.JSON)
                .body(
                        """
            {"customerId":"customer-1","lines":[]}
            """)
                .when()
                .post("/api/v1/orders")
                .then()
                .statusCode(400)
                .contentType(PROBLEM_JSON)
                .body("errors.field", hasItems("lines"));

        verifyNoInteractions(placeOrder);
    }

    @Test
    void malformedJsonReturnsProblemDetails() {
        given().contentType(ContentType.JSON)
                .body("{\"customerId\":")
                .when()
                .post("/api/v1/orders")
                .then()
                .statusCode(400)
                .contentType(PROBLEM_JSON)
                .body("status", equalTo(400));

        verifyNoInteractions(placeOrder);
    }

    @Test
    void wrongJsonTypeReturnsProblemDetailsWithoutInternals() {
        given().contentType(ContentType.JSON)
                .body(
                        """
            {"customerId":"customer-1","lines":"not-a-list"}
            """)
                .when()
                .post("/api/v1/orders")
                .then()
                .statusCode(400)
                .contentType(PROBLEM_JSON)
                .body("type", equalTo("urn:problem:malformed-request"))
                .body("detail", not(containsString("com.")));

        verifyNoInteractions(placeOrder);
    }

    @Test
    void domainRejectionReturns400Problem() {
        when(placeOrder.place(any()))
                .thenThrow(new InvalidOrder("an order needs at least one line"));

        given().contentType(ContentType.JSON)
                .body(
                        """
            {"customerId":"customer-1","lines":[{"sku":"SKU-1","quantity":1}]}
            """)
                .when()
                .post("/api/v1/orders")
                .then()
                .statusCode(400)
                .contentType(PROBLEM_JSON)
                .body("type", equalTo("urn:problem:invalid-order"));
    }

    @Test
    void getReturnsOrder() {
        var order = OrderFixtures.pendingOrder().reject("insufficient stock for SKU-1");
        when(getOrder.byId(order.id())).thenReturn(order);

        given().when()
                .get("/api/v1/orders/{id}", order.id().value())
                .then()
                .statusCode(200)
                .body("id", equalTo(order.id().toString()))
                .body("status", equalTo("REJECTED"))
                .body("rejectionReason", equalTo("insufficient stock for SKU-1"))
                .body("lines.size()", equalTo(2));
    }

    @Test
    void unknownOrderReturns404Problem() {
        var id = new OrderId(UUID.randomUUID());
        when(getOrder.byId(id)).thenThrow(new OrderNotFound(id));

        given().when()
                .get("/api/v1/orders/{id}", id.value())
                .then()
                .statusCode(404)
                .contentType(PROBLEM_JSON)
                .body("type", equalTo("urn:problem:order-not-found"))
                .body("status", equalTo(404))
                .body("detail", equalTo("order " + id + " does not exist"));
    }

    @Test
    void invalidStateReturns409Problem() {
        var id = new OrderId(UUID.randomUUID());
        when(getOrder.byId(id)).thenThrow(new InvalidOrderState("cannot confirm"));

        given().when()
                .get("/api/v1/orders/{id}", id.value())
                .then()
                .statusCode(409)
                .contentType(PROBLEM_JSON)
                .body("type", equalTo("urn:problem:invalid-order-state"));
    }

    @Test
    void nonUuidIdReturns404Problem() {
        given().when()
                .get("/api/v1/orders/not-a-uuid")
                .then()
                .statusCode(404)
                .contentType(PROBLEM_JSON);

        verifyNoInteractions(getOrder);
    }

    @Test
    void unexpectedErrorReturns500ProblemWithoutInternals() {
        var id = new OrderId(UUID.randomUUID());
        when(getOrder.byId(id))
                .thenThrow(new IllegalStateException("jdbc:postgresql://secret-host"));

        given().when()
                .get("/api/v1/orders/{id}", id.value())
                .then()
                .statusCode(500)
                .contentType(PROBLEM_JSON)
                .body("type", equalTo("urn:problem:internal-error"))
                .body("detail", equalTo("An unexpected error occurred"));
    }

    @Test
    void openApiDocumentListsTheOrdersApi() {
        given().accept(ContentType.JSON)
                .when()
                .get("/q/openapi")
                .then()
                .statusCode(200)
                .body("paths.'/api/v1/orders'.post", not(nullValue()));
    }
}
