package com.example.sms.mapper;

import com.example.sms.dto.request.FeedBackRequest;
import com.example.sms.dto.response.FeedBackResponse;
import com.example.sms.entity.FeedBack;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface FeedBackMapper {

    FeedBackResponse toFeedBackResponse(FeedBack feedBack);

    List<FeedBackResponse> toFeedBackResponseList(List<FeedBack> feedBacks);

    @Mapping(target = "id", ignore = true)
    FeedBack toFeedBack(FeedBackRequest feedBackRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    FeedBack partialUpdate(
            FeedBackRequest feedBackRequest, @MappingTarget FeedBack feedBack);
}
