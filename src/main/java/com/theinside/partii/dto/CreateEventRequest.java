package com.theinside.partii.dto;

import com.theinside.partii.enums.EventType;
import com.theinside.partii.enums.EventVisibility;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Request DTO for creating a new event.
 */
public record CreateEventRequest(

    @NotBlank(message = "Event title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    String title,

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    String description,

    @NotNull(message = "Event type is required")
    EventType eventType,

    @Size(max = 500, message = "Location address cannot exceed 500 characters")
    String locationAddress,

    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    Double latitude,

    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    Double longitude,

    @NotNull(message = "Event date is required")
    @Future(message = "Event date must be in the future")
    LocalDateTime eventDate,

    @Size(max = 500, message = "Image URL cannot exceed 500 characters")
    String imageUrl,

    @DecimalMin(value = "0.0", message = "Budget cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Invalid budget format")
    BigDecimal estimatedBudget,

    @Size(max = 3, message = "Currency code must be 3 characters")
    String currency,

    @Min(value = 2, message = "Event must have at least 2 attendees")
    @Max(value = 10000, message = "Event cannot exceed 10000 attendees")
    Integer maxAttendees,

    @Min(value = 0, message = "Age restriction cannot be negative")
    Integer ageRestriction,

    LocalDateTime paymentDeadline,

    LocalDateTime joinDeadline,

    @NotNull(message = "Event visibility is required")
    EventVisibility visibility,

    @Valid
    List<CreateContributionItemRequest> contributionItems
) {}
