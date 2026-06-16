package com.leafall.yourtaxi.dto.feedback;

import com.leafall.yourtaxi.dto.user.UserResponseDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class FeedbackCreateDto {
    @NotNull(message = "{validation.not-null}")
    @NotBlank(message = "{validation.not-blank}")
    @Size(min = 1, max = 200)
    private String title;
    @NotNull(message = "{validation.not-null}")
    @NotBlank
    @Size(min = 10, max = 5000)
    private String description;
    @Size(max = 100, message = "validation.phone.max-length")
    private String phone;
}
