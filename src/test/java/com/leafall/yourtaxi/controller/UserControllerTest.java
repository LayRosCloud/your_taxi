package com.leafall.yourtaxi.controller;

import com.leafall.yourtaxi.BaseIntegrationTest;
import com.leafall.yourtaxi.dto.user.SignUpDto;
import com.leafall.yourtaxi.dto.user.SuccessAuthDto;
import com.leafall.yourtaxi.dto.user.VerificationDto;
import com.leafall.yourtaxi.entity.enums.UserRole;
import com.leafall.yourtaxi.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTest extends BaseIntegrationTest {

    @MockitoBean
    private EmailService emailService;
    @BeforeEach
    public void setUp() {
        dbCleaner.clear();
        redisCleaner.clear();
    }

    @Test
    public void signUp_happyPath() throws Exception {
        // given
        var userDto = new SignUpDto();
        userDto.setEmail("test@example.com");
        userDto.setPassword("12345678");
        userDto.setRepeatPassword("12345678");
        userDto.setFullName("Петров П.П.");
        doNothing().when(emailService).sendVerificationMessage(any());
        // when
        var mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/v1/users/signup")
                    .content(objectMapper.writeValueAsString(userDto))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();
        // then
        var bytes = mvcResult.getResponse().getContentAsByteArray();
        var response = objectMapper.readValue(bytes, SuccessAuthDto.class);
        assertNull(response.getTokens());
        assertNotNull(response.getUser());
        assertEquals(userDto.getEmail(), response.getUser().getEmail());
        assertEquals(userDto.getFullName(), response.getUser().getFullName());
        assertEquals(Boolean.FALSE,response.getUser().getIsActive());
        assertEquals(UserRole.USER.getAuthority(), response.getUser().getRole());
        assertNull(response.getUser().getInfo());
    }
}
