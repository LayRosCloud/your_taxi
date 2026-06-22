package com.leafall.yourtaxi.controller;

import com.leafall.yourtaxi.BaseIntegrationTest;
import com.leafall.yourtaxi.core.db.TokenDbHelper;
import com.leafall.yourtaxi.core.db.UserDbHelper;
import com.leafall.yourtaxi.core.utils.equals.UserEqualsUtils;
import com.leafall.yourtaxi.dto.token.TokenHolder;
import com.leafall.yourtaxi.dto.user.*;
import com.leafall.yourtaxi.entity.enums.UserRole;
import com.leafall.yourtaxi.service.EmailService;
import com.leafall.yourtaxi.service.EncodingService;
import com.leafall.yourtaxi.utils.TimeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.leafall.yourtaxi.core.utils.FakerUtils.faker;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class UserControllerTest extends BaseIntegrationTest {
    @Autowired
    private UserDbHelper userDbHelper;
    @Autowired
    private EncodingService encodingService;
    @MockitoBean
    private EmailService emailService;
    @Autowired
    private TokenDbHelper tokenDbHelper;
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
        UserEqualsUtils.equalsUserSignUp(response, userDto);
    }

    @Test
    public void signUp_validationError() throws Exception {
        // given
        var userDto = new SignUpDto();
        userDto.setEmail("test");
        userDto.setPassword("1234");
        userDto.setRepeatPassword("1234");
        userDto.setFullName("Петров П.П.");
        // when
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/users/signup")
                        .content(objectMapper.writeValueAsString(userDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.status").exists());
        // then
    }

    @Test
    public void signUp_differentPasswordError() throws Exception {
        // given
        var userDto = new SignUpDto();
        userDto.setEmail("test@mail.ru");
        userDto.setPassword("123456789");
        userDto.setRepeatPassword("1234567899");
        userDto.setFullName("Петров П.П.");
        // when
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/users/signup")
                        .content(objectMapper.writeValueAsString(userDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.status").exists());
        // then
    }

    @Test
    public void signUp_duplicateEmail() throws Exception {
        // given
        var user = userDbHelper.save(UserRole.USER);
        var userDto = new SignUpDto();
        userDto.setEmail(user.getEmail());
        userDto.setPassword("123456789");
        userDto.setRepeatPassword("123456789");
        userDto.setFullName("Петров П.П.");
        // when
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/users/signup")
                        .content(objectMapper.writeValueAsString(userDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.status").exists());
        // then
    }

    @Test
    public void signUpNotActive_happyPath() throws Exception {
        // given
        var user = userDbHelper.save(UserRole.USER, false);
        var userDto = new SignUpDto();
        userDto.setEmail(user.getEmail());
        userDto.setPassword("123456789");
        userDto.setRepeatPassword("123456789");
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
        UserEqualsUtils.equalsUserSignUp(response, userDto);
    }

    @Test
    public void signUpNotDeleted_happyPath() throws Exception {
        // given
        var user = userDbHelper.save(UserRole.USER, true, TimeUtils.getEndOfDayUTC());
        var userDto = new SignUpDto();
        userDto.setEmail(user.getEmail());
        userDto.setPassword("123456789");
        userDto.setRepeatPassword("123456789");
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
        UserEqualsUtils.equalsUserSignUpDeletedAccount(response, userDto);
    }

    @Test
    public void signIn_happyPath() throws Exception {
        // given
        var user = userDbHelper.save(UserRole.USER, true);
        var password = "12345678";
        user.setPassword(encodingService.encode(password));
        userDbHelper.save(user);
        var userDto = new SignInDto();
        userDto.setEmail(user.getEmail());
        userDto.setPassword(password);
        // when
        var mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/v1/users/signin")
                        .content(objectMapper.writeValueAsString(userDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        // then
        var bytes = mvcResult.getResponse().getContentAsByteArray();
        var response = objectMapper.readValue(bytes, SuccessAuthDto.class);
        UserEqualsUtils.equalsUserAndTokens(response, user);
    }

    @Test
    public void signIn_notValidBody() throws Exception {
        // given
        var password = "12345678";
        var userDto = new SignInDto();
        userDto.setEmail("exampleName");
        userDto.setPassword(password);
        // when
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/users/signin")
                        .content(objectMapper.writeValueAsString(userDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.status").exists());
        // then
    }

    @Test
    public void signIn_badRequestNotFoundEmail() throws Exception {
        // given
        var password = "12345678";
        var userDto = new SignInDto();
        userDto.setEmail(faker.internet().emailAddress());
        userDto.setPassword(password);
        // when
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/users/signin")
                        .content(objectMapper.writeValueAsString(userDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.status").exists());
        // then
    }

    @Test
    public void signIn_badRequestNotValidPassword() throws Exception {
        // given
        var user = userDbHelper.save(UserRole.USER, true);
        var password = "super_uniq_pwd228";
        var userDto = new SignInDto();
        userDto.setEmail(user.getEmail());
        userDto.setPassword(password);
        // when
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/users/signin")
                        .content(objectMapper.writeValueAsString(userDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.status").exists());
        // then
    }

    @Test
    public void currentUser_unauthorized() throws Exception {
        // given

        // when
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/users/current"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.status").exists());
        // then
    }

    @Test
    public void currentUser_happyPath() throws Exception {
        // given
        var user = userDbHelper.save(UserRole.USER, true);
        var accessToken = tokenDbHelper.generateAccessToken(user.getId());
        // when
        var mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/v1/users/current")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();
        // then
        var bytes = mvcResult.getResponse().getContentAsByteArray();
        var response = objectMapper.readValue(bytes, UserResponseDto.class);
        UserEqualsUtils.equalsUsers(response, user);
    }

    @Test
    public void refreshToken_happyPath() throws Exception {
        // given
        var user = userDbHelper.save(UserRole.USER, true);
        var refreshToken = tokenDbHelper.generateRefreshToken(user.getId());
        var refreshDto = new RefreshDto();
        refreshDto.setRefreshToken(refreshToken);
        // when
        var mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/v1/users/tokens/refresh")
                        .content(objectMapper.writeValueAsString(refreshDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        // then
        var bytes = mvcResult.getResponse().getContentAsByteArray();
        var response = objectMapper.readValue(bytes, TokenHolder.class);
        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
    }
}
