package com.leafall.yourtaxi.dto.employee;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class EmployeeCreateDto {
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

    @Schema(description = "Пароль", example = "12345678")
    @NotNull(message = "{validation.not-null}")
    @NotBlank(message = "{validation.password.not-blank}")
    @Length(min = 6, max = 40, message = "{validation.password.size}")
    private String password;

    @Schema(description = "повторите пароль", example = "12345678")
    @NotNull(message = "{validation.not-null}")
    @NotBlank(message = "{validation.password.not-blank}")
    @Length(min = 6, max = 40, message = "{validation.password.size}")
    private String repeatPassword;

    @Schema(description = "Телефон", example = "+7(000)888-99-33")
    @NotNull(message = "{validation.not-null}")
    @NotBlank(message = "{validation.phone.not-blank}")
    @Length(max = 30, message = "{validation.phone.max-length}")
    private String phone;
}
