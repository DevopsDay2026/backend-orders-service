package com.devopsday.orders.adapter.in.rest;

import com.devopsday.orders.application.port.in.GetOrderQuery;
import com.devopsday.orders.application.port.in.PlaceOrderUseCase;
import com.devopsday.orders.domain.model.OrderId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.UUID;

@Path("/api/v1/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    private final PlaceOrderUseCase placeOrder;
    private final GetOrderQuery getOrder;

    OrderResource(PlaceOrderUseCase placeOrder, GetOrderQuery getOrder) {
        this.placeOrder = placeOrder;
        this.getOrder = getOrder;
    }

    @POST
    public Response place(@NotNull @Valid PlaceOrderRequest request, @Context UriInfo uriInfo) {
        var order = placeOrder.place(request.toCommand());
        var location = uriInfo.getAbsolutePathBuilder().path(order.id().toString()).build();
        return Response.created(location).entity(OrderResponse.from(order)).build();
    }

    @GET
    @Path("/{id}")
    public OrderResponse get(@PathParam("id") UUID id) {
        return OrderResponse.from(getOrder.byId(new OrderId(id)));
    }
}
