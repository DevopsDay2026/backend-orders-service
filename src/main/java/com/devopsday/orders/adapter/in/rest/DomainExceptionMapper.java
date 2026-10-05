package com.devopsday.orders.adapter.in.rest;

import com.devopsday.orders.domain.exception.DomainException;
import com.devopsday.orders.domain.exception.InvalidOrder;
import com.devopsday.orders.domain.exception.InvalidOrderState;
import com.devopsday.orders.domain.exception.OrderNotFound;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class DomainExceptionMapper implements ExceptionMapper<DomainException> {

    @Override
    public Response toResponse(DomainException exception) {
        var status =
                switch (exception) {
                    case OrderNotFound notFound -> Response.Status.NOT_FOUND;
                    case InvalidOrderState invalidState -> Response.Status.CONFLICT;
                    case InvalidOrder invalid -> Response.Status.BAD_REQUEST;
                };
        return Problem.of(
                        exception.code(),
                        status.getReasonPhrase(),
                        status.getStatusCode(),
                        exception.getMessage())
                .toResponse();
    }
}
