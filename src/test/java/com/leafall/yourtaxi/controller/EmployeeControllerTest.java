package com.leafall.yourtaxi.controller;

import com.leafall.yourtaxi.BaseIntegrationTest;
import com.leafall.yourtaxi.core.db.TokenDbHelper;
import com.leafall.yourtaxi.core.db.UserDbHelper;
import com.leafall.yourtaxi.dto.car.CarResponseDto;
import com.leafall.yourtaxi.dto.user.UserDetailResponseDto;
import com.leafall.yourtaxi.dto.user.UserResponseDto;
import com.leafall.yourtaxi.entity.enums.UserRole;
import com.leafall.yourtaxi.utils.pagination.PaginationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.core.type.TypeReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EmployeeControllerTest extends BaseIntegrationTest {

    @Autowired
    private UserDbHelper userDbHelper;
    @Autowired
    private TokenDbHelper tokenDbHelper;

    private String accessToken;

    @BeforeEach
    public void setUp() {
        dbCleaner.clear();
        redisCleaner.clear();
        var user = userDbHelper.save(UserRole.DISPATCHER, true);
        accessToken = tokenDbHelper.generateAccessToken(user.getId());
    }

    @Test
    public void getAllEmployee_happyPath() throws Exception {
        // given
        var employee = userDbHelper.save(UserRole.EMPLOYEE);

        // when
        var mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/v1/employees?limit=10&page=0")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        // then
        var bytes = mvcResult.getResponse().getContentAsByteArray();
        var response = objectMapper.readValue(bytes, new TypeReference<PaginationResponse<UserResponseDto>>() {});
        assertEquals(1, response.items().size());
        assertEquals(1, response.cursor().getTotal());
        assertEquals(0, response.cursor().getPage());
        assertEquals(10, response.cursor().getLimit());
    }

    @Test
    public void getByEmployeeId_happyPath() throws Exception {
        // given
        var employee = userDbHelper.save(UserRole.EMPLOYEE);

        // when
        var mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/v1/employees/{id}", employee.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        // then
        var bytes = mvcResult.getResponse().getContentAsByteArray();
        var response = objectMapper.readValue(bytes, UserDetailResponseDto.class);
    }
}
