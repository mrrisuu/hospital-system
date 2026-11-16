package com.project.hospital.invoice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record InvoiceCreateRequest(
        @NotNull(message = "Visit ID is required")
        @Positive(message = "Visit ID must be greater than 0")
        Long visitId,

        @NotEmpty(message = "At least one item is required")
        @Size(max = 50, message = "Items must not exceed 50")
        List<@Valid InvoiceItemRequest> items
) {}