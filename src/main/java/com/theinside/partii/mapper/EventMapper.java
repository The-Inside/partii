package com.theinside.partii.mapper;

import com.theinside.partii.dto.UpdateEventRequest;
import com.theinside.partii.entity.Event;
import org.mapstruct.*;

/**
 * MapStruct mapper for Event entity.
 * Handles partial updates with null-ignoring strategy.
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface EventMapper {

    /**
     * Updates an existing event entity with non-null values from the request.
     * Fields that are null in the request are ignored (not updated).
     *
     * @param request the update request with potentially partial data
     * @param event   the existing event entity to update
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEventFromDto(UpdateEventRequest request, @MappingTarget Event event);
}
