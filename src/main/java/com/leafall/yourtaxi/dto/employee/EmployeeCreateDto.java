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
    @NotNull
    @NotBlank
    @Length(max = 100, message = "Имя должно быть не более 100 символов")
    private String fullName;

    @Schema(description = "Почта", example = "example@example.com")
    @NotNull
    @NotBlank(message = "Почта не может быть пустой")
    @Email(message = "Почта имеет неправильный формат")
    private String email;

    @Schema(description = "Пароль", example = "12345678")
    @NotNull
    @NotBlank(message = "Пароль не может быть пустым")
    @Length(min = 6, max = 40, message = "Пароль может быть от 6 до 40 символов")
    private String password;

    @Schema(description = "повторите пароль", example = "12345678")
    @NotNull
    @NotBlank(message = "Пароль не может быть пустым")
    @Length(min = 6, max = 40, message = "Пароль может быть от 6 до 40 символов")
    private String repeatPassword;

    @Schema(description = "Телефон", example = "+7(000)888-99-33")
    @NotNull
    @NotBlank(message = "Телефон не может быть пустым")
    @Length(max = 30, message = "Телефон может быть максимум 30 символов")
    private String phone;
}
