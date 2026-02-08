package com.theinside.partii.dto;

import lombok.*;

import java.time.Instant;

/**
 * Response DTO for user data export (GDPR compliance).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportDataResponse {

    /**
     * URL to download the exported data file.
     */
    private String downloadUrl;

    /**
     * When the export was created.
     */
    private Instant createdAt;

    /**
     * When the export file will expire and be deleted.
     */
    private Instant expiresAt;

    /**
     * Size of the export file in bytes.
     */
    private Long fileSizeBytes;

    /**
     * Status message about the export.
     */
    private String message;
}
