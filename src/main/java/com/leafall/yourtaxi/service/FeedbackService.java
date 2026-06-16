package com.leafall.yourtaxi.service;

import com.leafall.yourtaxi.dto.feedback.FeedbackCountDto;
import com.leafall.yourtaxi.dto.feedback.FeedbackCreateDto;
import com.leafall.yourtaxi.dto.feedback.FeedbackResponseDto;
import com.leafall.yourtaxi.exception.NotFoundException;
import com.leafall.yourtaxi.mapper.FeedbackMapper;
import com.leafall.yourtaxi.repository.FeedbackRepository;
import com.leafall.yourtaxi.repository.UserRepository;
import com.leafall.yourtaxi.utils.TimeUtils;
import com.leafall.yourtaxi.utils.pagination.PaginationCursor;
import com.leafall.yourtaxi.utils.pagination.PaginationParams;
import com.leafall.yourtaxi.utils.pagination.PaginationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final FeedbackMapper mapper;

    @Transactional(readOnly = true)
    public PaginationResponse<FeedbackResponseDto> findAll(PaginationParams params) {
        var pagination = params.getPageable(false, "createdAt");
        var feedbacks = feedbackRepository.findAll(pagination);
        var mappedDtos = mapper.mapToDto(feedbacks.getContent());
        return new PaginationResponse<>(mappedDtos, new PaginationCursor(params, feedbacks.getTotalElements()));
    }

    @Transactional(readOnly = true)
    public FeedbackCountDto count() {
        var count = feedbackRepository.countByReadAtIsNull();
        var feedbackCount = new FeedbackCountDto();
        feedbackCount.setCount(count);
        return feedbackCount;
    }

    @Transactional(readOnly = true)
    public FeedbackResponseDto findById(UUID id) {
        var feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("base.error.not-found"));
        return mapper.mapToDto(feedback);
    }

    @Transactional(rollbackFor = Exception.class)
    public FeedbackResponseDto create(FeedbackCreateDto dto, UUID userId) {
        var toSave = mapper.mapToEntity(dto);
        if (userId != null) {
            var user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("user.error.not-found"));
            toSave.setCreatedBy(user);
        }
        var feedback = feedbackRepository.save(toSave);
        return mapper.mapToDto(feedback);
    }
    @Transactional(rollbackFor = Exception.class)
    public FeedbackResponseDto read(UUID id, UUID userId) {
        var toRead = feedbackRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("base.error.not-found"));
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user.error.not-found"));
        toRead.setReadBy(user);
        toRead.setReadAt(TimeUtils.getCurrentTimeFromUTC());
        var feedback = feedbackRepository.save(toRead);
        return mapper.mapToDto(feedback);
    }

}
