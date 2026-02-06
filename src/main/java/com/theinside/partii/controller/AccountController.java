package com.theinside.partii.controller;

import com.theinside.partii.dto.DeleteAccountRequest;
import com.theinside.partii.dto.ExportDataResponse;
import com.theinside.partii.security.SecurityUser;
import com.theinside.partii.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for account management endpoints.
 * Handles account deletion, deactivation, and GDPR data export.
 */
@Slf4j
@RestController
@RequestMapping("/partii/api/v1/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * DELETE /api/account
     * Delete the authenticated user's account after a grace period.
     * Requires confirmation text "DELETE MY ACCOUNT".
     */
    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteAccount(
        @AuthenticationPrincipal SecurityUser currentUser,
        @Valid @RequestBody DeleteAccountRequest request
    ) {
        log.warn("User {} requesting account deletion", currentUser.getUserId());
        accountService.deleteAccount(currentUser.getUserId(), request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    /**
     * POST /api/account/deactivate
     * Temporarily deactivate the authenticated user's account.
     */
    @PostMapping("/deactivate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deactivateAccount(
        @AuthenticationPrincipal SecurityUser currentUser
    ) {
        log.info("User {} deactivating account", currentUser.getUserId());
        accountService.deactivateAccount(currentUser.getUserId());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    /**
     * POST /api/account/reactivate
     * Reactivate a previously deactivated account.
     */
    @PostMapping("/reactivate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> reactivateAccount(
        @AuthenticationPrincipal SecurityUser currentUser
    ) {
        log.info("User {} reactivating account", currentUser.getUserId());
        accountService.reactivateAccount(currentUser.getUserId());
        return ResponseEntity.ok().build();
    }

    /**
     * GET /api/account/export
     * Export the authenticated user's personal data (GDPR compliance).
     */
    @GetMapping("/export")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ExportDataResponse> exportData(
        @AuthenticationPrincipal SecurityUser currentUser
    ) {
        log.info("User {} exporting personal data", currentUser.getUserId());
        ExportDataResponse response = accountService.exportUserData(currentUser.getUserId());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/account/deactivation-status
     * Check if the authenticated user's account is deactivated.
     */
    @GetMapping("/deactivation-status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> isDeactivated(
        @AuthenticationPrincipal SecurityUser currentUser
    ) {
        boolean deactivated = accountService.isDeactivated(currentUser.getUserId());
        return ResponseEntity.ok(deactivated);
    }
}
