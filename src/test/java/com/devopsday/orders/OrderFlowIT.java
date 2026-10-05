package com.devopsday.orders;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusIntegrationTest
class OrderFlowIT {

    @Test
    void placesAndReadsBackAnOrderThroughRealAdapters() {
        var location =
                given().contentType(ContentType.JSON)
                        .body(
                                """
                {"customerId":"customer-it","lines":[{"sku":"SKU-1","quantity":2}]}
                """)
                        .when()
                        .post("/api/v1/orders")
                        .then()
                        .statusCode(201)
                        .body("status", equalTo("PENDING"))
                        .extract()
                        .header("Location");

        given().when()
                .get(location)
                .then()
                .statusCode(200)
                .body("customerId", equalTo("customer-it"))
                .body("lines", hasSize(1));
    }

    @Test
    void validationErrorsAreProblemDetails() {
        given().contentType(ContentType.JSON)
                .body("{\"customerId\":\"\",\"lines\":[]}")
                .when()
                .post("/api/v1/orders")
                .then()
                .statusCode(400)
                .contentType("application/problem+json")
                .body("title", equalTo("Validation failed"))
                .body("errors.field", hasItem("customerId"));
    }

    @Test
    void readinessCoversDatabaseAndKafka() {
        given().when().get("/q/health/ready").then().statusCode(200).body("status", equalTo("UP"));
    }
}
