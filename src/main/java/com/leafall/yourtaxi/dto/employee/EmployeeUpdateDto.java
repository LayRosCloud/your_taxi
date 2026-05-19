package com.leafall.yourtaxi.dto.employee;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.UUID;

@Data
public class EmployeeUpdateDto {
    @Schema(description = "Id", example = "9f76f9e4-c370-4066-bc21-1fffe0d4adcf")
    @NotNull(message = "{validation.not-null}")
    private UUID id;
    @Schema(description = "Как к вам обращаться", example = "Григорий")
    @NotNull(message = "{validation.not-null}")
    @NotBlank(message = "{validation.name.not-blank}")
    @Length(max = 100, message = "{validation.name.max-length}")
    private String fullName;
    @Schema(description = "Почта", example = "example@example.com")
    @NotNull(message = "{validation.not-null}")
    @NotBlank(message = "{validation.email.not-blank}")
    @Email(message = "{validation.email.invalid}")
    private String email;
    @Schema(description = "Телефон", example = "+7(000)888-99-33")
    @NotNull(message = "{validation.not-null}")
    @NotBlank(message = "{validation.phone.not-blank}")
    @Length(max = 30, message = "{validation.phone.max-length}")
    private String phone;
}
