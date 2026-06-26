package com.leafall.yourtaxi.controller;

import com.leafall.yourtaxi.BaseIntegrationTest;
import com.leafall.yourtaxi.core.db.TokenDbHelper;
import com.leafall.yourtaxi.core.db.UserDbHelper;
import com.leafall.yourtaxi.core.db.VariableDbHelper;
import com.leafall.yourtaxi.core.utils.dto.VariableDtoUtils;
import com.leafall.yourtaxi.core.utils.entity.VariableEntityUtils;
import com.leafall.yourtaxi.core.utils.equals.VariableEqualsUtils;
import com.leafall.yourtaxi.dto.variable.VariableResponseDto;
import com.leafall.yourtaxi.entity.enums.UserRole;
import com.leafall.yourtaxi.entity.enums.VariableType;
import com.leafall.yourtaxi.utils.pagination.PaginationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.core.type.TypeReference;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class VariableControllerTest extends BaseIntegrationTest {
    @Autowired
    private VariableDbHelper variableDbHelper;
    @Autowired
    private UserDbHelper userDbHelper;
    @Autowired
    private TokenDbHelper tokenDbHelper;

    private String accessToken = null;

    @BeforeEach
    public void setUp() {
        dbCleaner.clear();
        redisCleaner.clear();
        var user = userDbHelper.save(UserRole.DISPATCHER);
        accessToken = tokenDbHelper.generateAccessToken(user.getId());
    }

    @Test
    public void getVariables_happyPath() throws Exception {
        // given
        var variable = variableDbHelper.save();
        // when
        var mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/v1/variables?limit=10&page=0")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();
        // then
        var bytes = mvcResult.getResponse().getContentAsByteArray();
        var response = objectMapper.readValue(bytes, new TypeReference<PaginationResponse<VariableResponseDto>>() {});
        assertEquals(1, response.items().size());
        assertEquals(1, response.cursor().getTotal());
        assertEquals(0, response.cursor().getPage());
        assertEquals(10, response.cursor().getLimit());
        VariableEqualsUtils.equals(variable, response.items().get(0));
    }

    @Test
    public void getVariablesById_happyPath() throws Exception {
        // given
        var variable = variableDbHelper.save();
        // when
        var mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/v1/variables/{id}", variable.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();
        // then
        var bytes = mvcResult.getResponse().getContentAsByteArray();
        var response = objectMapper.readValue(bytes, VariableResponseDto.class);
        VariableEqualsUtils.equals(variable, response);
    }

    @Test
    public void getVariablesById_notFound() throws Exception {
        // given
        // when
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/variables/{id}", UUID.randomUUID())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.status").exists());
        // then
    }

    @Test
    public void getVariablesByKey_happyPath() throws Exception {
        // given
        var variable = variableDbHelper.save();
        // when
        var mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/v1/variables/key/{key}", variable.getKey())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();
        // then
        var bytes = mvcResult.getResponse().getContentAsByteArray();
        var response = objectMapper.readValue(bytes, VariableResponseDto.class);
        VariableEqualsUtils.equals(variable, response);
    }

    @Test
    public void getVariablesByKey_notFound() throws Exception {
        // given
        // when
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/variables/key/{key}", UUID.randomUUID().toString())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.status").exists());
        // then
    }

    @Test
    public void updateVariables_happyPath() throws Exception {
        // given
        var variable = variableDbHelper.save();
        var dto = VariableDtoUtils.generate(variable.getId());
        // when
        var mvcResult = mockMvc.perform(MockMvcRequestBuilders.put("/v1/variables")
                        .content(objectMapper.writeValueAsBytes(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();
        // then
        var bytes = mvcResult.getResponse().getContentAsByteArray();
        var response = objectMapper.readValue(bytes, VariableResponseDto.class);
        VariableEqualsUtils.equals(response, dto);
    }

    @Test
    public void updateVariables_notFound() throws Exception {
        // given
        var dto = VariableDtoUtils.generate(UUID.randomUUID());
        // when
        mockMvc.perform(MockMvcRequestBuilders.put("/v1/variables")
                        .content(objectMapper.writeValueAsBytes(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.status").exists());
        // then
    }

    @Test
    public void updateVariables_badFormat() throws Exception {
        // given
        var entity = VariableEntityUtils.generate();
        entity.setValue("10");
        entity.setType(VariableType.NUMBER);
        var variable = variableDbHelper.save(entity);
        var dto = VariableDtoUtils.generate(variable.getId());
        dto.setValue("Another text");
        // when
        var mvcResult = mockMvc.perform(MockMvcRequestBuilders.put("/v1/variables")
                        .content(objectMapper.writeValueAsBytes(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.status").exists());
        // then
    }

    @Test
    public void updateVariables_happyFormat() throws Exception {
        // given
        var entity = VariableEntityUtils.generate();
        entity.setValue("10");
        entity.setType(VariableType.NUMBER);
        var variable = variableDbHelper.save(entity);
        var dto = VariableDtoUtils.generate(variable.getId());
        dto.setValue("21");
        // when
        var mvcResult = mockMvc.perform(MockMvcRequestBuilders.put("/v1/variables")
                        .content(objectMapper.writeValueAsBytes(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();
        // then
        var bytes = mvcResult.getResponse().getContentAsByteArray();
        var response = objectMapper.readValue(bytes, VariableResponseDto.class);
        VariableEqualsUtils.equals(response, dto);
    }
}
