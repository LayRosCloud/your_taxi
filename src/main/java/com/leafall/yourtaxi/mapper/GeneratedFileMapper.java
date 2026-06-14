package com.leafall.yourtaxi.mapper;

import com.leafall.yourtaxi.dto.generatedFiles.GeneratedFileResponseDto;
import com.leafall.yourtaxi.entity.GeneratedFileEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = { UserMapper.class })
public interface GeneratedFileMapper {

    GeneratedFileResponseDto mapToDto(GeneratedFileEntity entity);
}
