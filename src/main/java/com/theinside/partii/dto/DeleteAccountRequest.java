package com.theinside.partii.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Request DTO for account deletion.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteAccountRequest {

    /**
     * Confirmation text - user must type "DELETE MY ACCOUNT" to confirm.
     */
    @NotBlank(message = "Confirmation is required")
    private String confirmation;

    /**
     * Optional reason for deletion.
     */
    private String reason;
}
