package com.devopsday.orders.adapter.in.rest;

import com.devopsday.orders.application.port.in.PlaceOrderUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PlaceOrderRequest(
        @NotBlank @Size(max = 64) String customerId,
        @NotEmpty @Size(max = 50) List<@Valid @NotNull Line> lines) {

    public record Line(
            @NotBlank @Size(max = 64) String sku, @Positive @Max(1_000_000) int quantity) {}

    public PlaceOrderUseCase.Command toCommand() {
        return new PlaceOrderUseCase.Command(
                customerId,
                lines.stream()
                        .map(
                                line ->
                                        new PlaceOrderUseCase.Command.Line(
                                                line.sku(), line.quantity()))
                        .toList());
    }
}
