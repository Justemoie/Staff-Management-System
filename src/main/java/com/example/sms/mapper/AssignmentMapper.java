package com.example.sms.mapper;

import com.example.sms.dto.request.AssignmentRequest;
import com.example.sms.dto.response.AssignmentResponse;
import com.example.sms.entity.Assignment;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AssignmentMapper {

    AssignmentResponse toAssignmentResponse(Assignment assignment);

    List<AssignmentResponse> toAssignmentResponseList(List<Assignment> assignments);

    @Mapping(target = "id", ignore = true)
    Assignment toAssignment(AssignmentRequest assignmentRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    Assignment partialUpdate(AssignmentRequest assignmentRequest,
                             @MappingTarget Assignment assignment);
}
