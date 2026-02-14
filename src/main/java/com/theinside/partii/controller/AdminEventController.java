package com.theinside.partii.controller;

import com.theinside.partii.dto.CursorPage;
import com.theinside.partii.dto.EventResponse;
import com.theinside.partii.service.EventService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin-only REST controller for event management.
 * Provides administrative access to all events in the system.
 */
@Slf4j
@RestController
@RequestMapping("/partii/api/v1/admin/events")
@RequiredArgsConstructor
public class AdminEventController {

    private final EventService eventService;

    /**
     * GET /api/v1/admin/events
     * List all events in the system (offset pagination).
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<EventResponse>> getAllEvents(
        @PageableDefault(size = 50) Pageable pageable
    ) {
        log.info("Admin fetching all events (offset), page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<EventResponse> events = eventService.getAllEvents(pageable);
        return ResponseEntity.ok(events);
    }

    /**
     * GET /api/v1/admin/events/cursor
     * List all events using keyset pagination (O(1) performance).
     */
    @GetMapping("/cursor")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CursorPage<EventResponse>> getAllEventsKeyset(
        @RequestParam(required = false) String cursor,
        @RequestParam(defaultValue = "50") @Min(1) @Max(100) int limit
    ) {
        log.info("Admin fetching all events (keyset), cursor: {}, limit: {}", cursor, limit);
        CursorPage<EventResponse> events = eventService.getAllEventsKeyset(cursor, limit);
        return ResponseEntity.ok(events);
    }
}
