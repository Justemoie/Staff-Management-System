package com.example.sms.mapper;

import com.example.sms.dto.request.AssignmentRequest;
import com.example.sms.dto.response.AssignmentResponse;
import com.example.sms.dto.response.FeedBackResponse;
import com.example.sms.entity.Assignment;
import com.example.sms.entity.FeedBack;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AssignmentMapper {

    @Mapping(target = "description", defaultValue = "")
    @Mapping(target = "feedBacks", qualifiedByName = "mapFeedBacks")
    AssignmentResponse toAssignmentResponse(Assignment assignment);

    List<AssignmentResponse> toAssignmentResponseList(List<Assignment> assignments);

    @Mapping(target = "id", ignore = true)
    Assignment toAssignment(AssignmentRequest assignmentRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    Assignment partialUpdate(AssignmentRequest assignmentRequest,
                             @MappingTarget Assignment assignment);

    @Named("mapFeedBacks")
    default List<FeedBackResponse> mapFeedBacks(List<FeedBack> feedBacks) {
        if (feedBacks == null) {
            return Collections.emptyList(); // Используем неизменяемый пустой список
        }
        return feedBacks.stream()
                .filter(fb -> fb != null) // Фильтруем null элементы
                .map(feedBack -> {
                    if (feedBack == null) return null;
                    return new FeedBackResponse(feedBack.getId(), feedBack.getComment(), feedBack.getCreatedAt());
                })
                .filter(fbr -> fbr != null) // Фильтруем null результаты
                .collect(Collectors.toList());
    }
}