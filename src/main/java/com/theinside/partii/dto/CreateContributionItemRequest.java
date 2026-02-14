package com.theinside.partii.dto;

import com.theinside.partii.enums.ContributionType;
import com.theinside.partii.enums.Priority;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Request DTO for creating a contribution item for an event.
 */
public record CreateContributionItemRequest(

    @NotBlank(message = "Item name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    String name,

    @Size(max = 50, message = "Category cannot exceed 50 characters")
    String category,

    @NotNull(message = "Contribution type is required")
    ContributionType type,

    @Min(value = 1, message = "Quantity must be at least 1")
    Integer quantity,

    @Min(value = 0, message = "Time commitment cannot be negative")
    Integer timeCommitment,

    @DecimalMin(value = "0.0", message = "Estimated cost cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Invalid estimated cost format")
    BigDecimal estimatedCost,

    @NotNull(message = "Priority is required")
    Priority priority,

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    String notes
) {}
