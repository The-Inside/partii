package com.theinside.partii.service;

import com.theinside.partii.dto.*;
import com.theinside.partii.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for event operations.
 */
public interface EventService {

    /**
     * Create a new event.
     *
     * @param userId  the ID of the user creating the event
     * @param request the event creation request
     * @return the created event response
     */
    EventResponse createEvent(Long userId, CreateEventRequest request);

    /**
     * Get an event by its ID.
     *
     * @param eventId the event ID
     * @return the event response
     */
    EventResponse getEvent(Long eventId);

    /**
     * Get an event by its private link code.
     *
     * @param privateLinkCode the private link code
     * @return the event response
     */
    EventResponse getEventByPrivateLinkCode(String privateLinkCode);

    /**
     * List all events (admin only).
     *
     * @param pageable pagination information
     * @return page of event responses
     */
    Page<EventResponse> getAllEvents(Pageable pageable);

    /**
     * Partially update an existing event (PATCH semantics with MapStruct).
     * Only non-null fields in the request will be updated.
     *
     * @param eventId the event ID
     * @param userId  the ID of the user updating the event
     * @param request the partial update request
     * @return the updated event response
     */
    EventResponse updateEvent(Long eventId, Long userId, UpdateEventRequest request);

    /**
     * Delete an event.
     *
     * @param eventId the event ID
     * @param userId  the ID of the user deleting the event
     */
    void deleteEvent(Long eventId, Long userId);

    /**
     * Publish an event (change status to ACTIVE).
     *
     * @param eventId the event ID
     * @param userId  the ID of the user publishing the event
     * @return the updated event response
     */
    EventResponse publishEvent(Long eventId, Long userId);

    /**
     * Cancel an event.
     *
     * @param eventId the event ID
     * @param userId  the ID of the user canceling the event
     * @param reason  the cancellation reason
     * @return the updated event response
     */
    EventResponse cancelEvent(Long eventId, Long userId, String reason);

    /**
     * List public events using keyset pagination (significantly faster than offset).
     *
     * @param cursor the cursor from previous page (null for first page)
     * @param limit  the maximum number of results to return
     * @return cursor page of event responses
     */
    CursorPage<EventResponse> getPublicEvents(String cursor, int limit);

    /**
     * List all events using keyset pagination (admin only).
     *
     * @param cursor the cursor from previous page (null for first page)
     * @param limit  the maximum number of results to return
     * @return cursor page of event responses
     */
    CursorPage<EventResponse> getAllEventsKeyset(String cursor, int limit);

    /**
     * Search events with dynamic filters and sorting.
     *
     * @param searchRequest the search criteria
     * @param pageable      pagination and sorting information
     * @return page of matching event responses
     */
    Page<EventResponse> searchEvents(EventSearchRequest searchRequest, Pageable pageable);

    /**
     * List all public and visible events.
     *
     * @param pageable pagination information
     * @return page of event responses
     */
    Page<EventResponse> getPublicEvents(Pageable pageable);

    /**
     * List events belonging to the authenticated user.
     * Includes events the user organized and events the user is attending.
     *
     * @param userId   the authenticated user's ID
     * @param status   optional filter by event status
     * @param role     optional filter by user's role ("organizer" or "attendee")
     * @param pageable pagination and sorting information
     * @return page of event responses
     */
    Page<EventResponse> getMyEvents(Long userId, EventStatus status, String role, Pageable pageable);
}
