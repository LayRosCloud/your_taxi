package com.leafall.yourtaxi.mapper;

import com.leafall.yourtaxi.dto.feedback.FeedbackCreateDto;
import com.leafall.yourtaxi.dto.feedback.FeedbackResponseDto;
import com.leafall.yourtaxi.entity.FeedbackEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface FeedbackMapper {
    FeedbackEntity mapToEntity(FeedbackCreateDto dto);
    FeedbackResponseDto mapToDto(FeedbackEntity entity);
    List<FeedbackResponseDto> mapToDto(List<FeedbackEntity> entities);
}
