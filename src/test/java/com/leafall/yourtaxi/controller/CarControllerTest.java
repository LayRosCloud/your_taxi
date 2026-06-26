package com.leafall.yourtaxi.controller;

import com.leafall.yourtaxi.BaseIntegrationTest;
import com.leafall.yourtaxi.core.db.CarDbHelper;
import com.leafall.yourtaxi.core.db.TokenDbHelper;
import com.leafall.yourtaxi.core.db.TripDbHelper;
import com.leafall.yourtaxi.core.db.UserDbHelper;
import com.leafall.yourtaxi.core.utils.dto.CarDtoUtils;
import com.leafall.yourtaxi.core.utils.equals.CarEqualsUtils;
import com.leafall.yourtaxi.dto.car.CarResponseDto;
import com.leafall.yourtaxi.entity.enums.UserRole;
import com.leafall.yourtaxi.utils.pagination.PaginationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.core.type.TypeReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CarControllerTest extends BaseIntegrationTest {
    @Autowired
    private CarDbHelper carDbHelper;
    @Autowired
    private UserDbHelper userDbHelper;
    @Autowired
    private TokenDbHelper tokenDbHelper;
    @Autowired
    private TripDbHelper tripDbHelper;

    @BeforeEach
    public void setUp() {
        dbCleaner.clear();
        redisCleaner.clear();
    }

    @Test
    public void getCars_happyPath() throws Exception {
        // given
        var user = userDbHelper.save(UserRole.DISPATCHER);
        carDbHelper.save(user);
        var accessToken = tokenDbHelper.generateAccessToken(user.getId());
        // when
        var mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/v1/cars?limit=10&page=0")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        // then
        var bytes = mvcResult.getResponse().getContentAsByteArray();
        var response = objectMapper.readValue(bytes, new TypeReference<PaginationResponse<CarResponseDto>>() {});
        assertEquals(1, response.items().size());
        assertEquals(1, response.cursor().getTotal());
        assertEquals(0, response.cursor().getPage());
        assertEquals(10, response.cursor().getLimit());
    }

    @Test
    public void getCars_byEmployee_happyPath() throws Exception {
        // given
        var user = userDbHelper.save(UserRole.EMPLOYEE);
        carDbHelper.save(user);
        var accessToken = tokenDbHelper.generateAccessToken(user.getId());
        // when
        var mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/v1/cars?limit=10&page=0")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        // then
        var bytes = mvcResult.getResponse().getContentAsByteArray();
        var response = objectMapper.readValue(bytes, new TypeReference<PaginationResponse<CarResponseDto>>() {});
        assertEquals(1, response.items().size());
        assertEquals(1, response.cursor().getTotal());
        assertEquals(0, response.cursor().getPage());
        assertEquals(10, response.cursor().getLimit());
    }

    @Test
    public void getCars_available_happyPath() throws Exception {
        // given
        var user = userDbHelper.save(UserRole.EMPLOYEE);
        var dispatcher = userDbHelper.save(UserRole.DISPATCHER);
        carDbHelper.save(user);
        carDbHelper.save(user);
        carDbHelper.save(dispatcher);
        var accessToken = tokenDbHelper.generateAccessToken(user.getId());
        // when
        var mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/v1/cars/available?limit=10&page=0")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        // then
        var bytes = mvcResult.getResponse().getContentAsByteArray();
        var response = objectMapper.readValue(bytes, new TypeReference<PaginationResponse<CarResponseDto>>() {});
        assertEquals(3, response.items().size());
        assertEquals(3, response.cursor().getTotal());
        assertEquals(0, response.cursor().getPage());
        assertEquals(10, response.cursor().getLimit());
    }

    @Test
    public void getCarsLast_notFound() throws Exception {
        // given
        var user = userDbHelper.save(UserRole.EMPLOYEE);
        var accessToken = tokenDbHelper.generateAccessToken(user.getId());
        // when
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/cars/last")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.status").exists());

        // then
    }

    @Test
    public void getCarsLast_happyPath() throws Exception {
        // given
        var user = userDbHelper.save(UserRole.EMPLOYEE);
        var car = carDbHelper.save(user);
        var trip = tripDbHelper.save(user, car);
        var accessToken = tokenDbHelper.generateAccessToken(user.getId());
        // when
        var mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/v1/cars/last")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        // then
        var bytes = mvcResult.getResponse().getContentAsByteArray();
        var response = objectMapper.readValue(bytes, CarResponseDto.class);
        CarEqualsUtils.equals(car, response);
    }

    @Test
    public void getCarsById_happyPath() throws Exception {
        // given
        var user = userDbHelper.save(UserRole.DISPATCHER);
        var car = carDbHelper.save(user);
        var accessToken = tokenDbHelper.generateAccessToken(user.getId());
        // when
        var mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/v1/cars/{id}", car.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        // then
        var bytes = mvcResult.getResponse().getContentAsByteArray();
        var response = objectMapper.readValue(bytes, CarResponseDto.class);
        CarEqualsUtils.equals(car, response);
    }

    @Test
    public void getCarsById_notFound() throws Exception {
        // given
        var user = userDbHelper.save(UserRole.DISPATCHER);
        var accessToken = tokenDbHelper.generateAccessToken(user.getId());
        // when
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/cars/{id}", Long.MAX_VALUE)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.status").exists());

        // then
    }

    @Test
    public void create_happyPath() throws Exception {
        // given
        var user = userDbHelper.save(UserRole.DISPATCHER);
        var accessToken = tokenDbHelper.generateAccessToken(user.getId());
        var dto = CarDtoUtils.generateDto();
        // when
        var mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/v1/cars")
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andReturn();

        // then
        var bytes = mvcResult.getResponse().getContentAsByteArray();
        var response = objectMapper.readValue(bytes, CarResponseDto.class);
        CarEqualsUtils.equals(dto, response);
    }

    @Test
    public void create_badRequest() throws Exception {
        // given
        var user = userDbHelper.save(UserRole.DISPATCHER);
        var accessToken = tokenDbHelper.generateAccessToken(user.getId());
        var dto = CarDtoUtils.generateDto();
        var overLongString = "bdshjfbnasjkfgbdsahjghfsaujikfgbhhjasvbfhjasgbfhjasvfhjasfvhgjasgvfhdjashgjdkabshjvcbahsjfvgbhjasfgdhjasgvfhasdasdasdasdaddgjkasnfjksahbfjasgbhjj";
        dto.setMark(overLongString);
        dto.setNumber(overLongString);
        dto.setColor(overLongString);
        // when
        var mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/v1/cars")
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.status").exists());;

        // then
    }

    @Test
    public void update_happyPath() throws Exception {
        // given
        var user = userDbHelper.save(UserRole.DISPATCHER);
        var car = carDbHelper.save(user);
        var accessToken = tokenDbHelper.generateAccessToken(user.getId());
        var dto = CarDtoUtils.generateDto(car.getId());
        // when
        var mvcResult = mockMvc.perform(MockMvcRequestBuilders.put("/v1/cars")
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        // then
        var bytes = mvcResult.getResponse().getContentAsByteArray();
        var response = objectMapper.readValue(bytes, CarResponseDto.class);
        CarEqualsUtils.equals(dto, response);
    }

    @Test
    public void update_notFound() throws Exception {
        // given
        var user = userDbHelper.save(UserRole.DISPATCHER);
        var accessToken = tokenDbHelper.generateAccessToken(user.getId());
        var dto = CarDtoUtils.generateDto(Long.MAX_VALUE);
        // when
        mockMvc.perform(MockMvcRequestBuilders.put("/v1/cars")
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.status").exists());

        // then
    }

    @Test
    public void update_forbidden() throws Exception {
        // given
        var user = userDbHelper.save(UserRole.DISPATCHER);
        var employee = userDbHelper.save(UserRole.EMPLOYEE);
        var car = carDbHelper.save(user);
        var accessToken = tokenDbHelper.generateAccessToken(employee.getId());
        var dto = CarDtoUtils.generateDto(car.getId());
        // when
        mockMvc.perform(MockMvcRequestBuilders.put("/v1/cars")
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.status").exists());

        // then

    }

    @Test
    public void update_dispatcherUpdateEmployeeCar_happyPath() throws Exception {
        // given
        var user = userDbHelper.save(UserRole.DISPATCHER);
        var employee = userDbHelper.save(UserRole.EMPLOYEE);
        var car = carDbHelper.save(employee);
        var accessToken = tokenDbHelper.generateAccessToken(user.getId());
        var dto = CarDtoUtils.generateDto(car.getId());
        // when
        var mvcResult = mockMvc.perform(MockMvcRequestBuilders.put("/v1/cars")
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        // then
        var bytes = mvcResult.getResponse().getContentAsByteArray();
        var response = objectMapper.readValue(bytes, CarResponseDto.class);
        CarEqualsUtils.equals(dto, response);

    }

    @Test
    public void delete_happyPath() throws Exception {
        // given
        var user = userDbHelper.save(UserRole.DISPATCHER);
        var car = carDbHelper.save(user);
        var accessToken = tokenDbHelper.generateAccessToken(user.getId());
        // when
        mockMvc.perform(MockMvcRequestBuilders.delete("/v1/cars/{id}", car.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent())
                .andReturn();

        // then
    }

    @Test
    public void delete_notFound() throws Exception {
        // given
        var user = userDbHelper.save(UserRole.DISPATCHER);
        var accessToken = tokenDbHelper.generateAccessToken(user.getId());
        // when
        mockMvc.perform(MockMvcRequestBuilders.delete("/v1/cars/{id}", Long.MAX_VALUE)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.status").exists());

        // then
    }

    @Test
    public void delete_forbidden() throws Exception {
        // given
        var user = userDbHelper.save(UserRole.DISPATCHER);
        var employee = userDbHelper.save(UserRole.EMPLOYEE);
        var car = carDbHelper.save(user);
        var accessToken = tokenDbHelper.generateAccessToken(employee.getId());
        // when
        mockMvc.perform(MockMvcRequestBuilders.delete("/v1/cars/{id}", car.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.status").exists());

        // then
    }

    @Test
    public void delete_dispatcherDeleteEmployeeCar_happyPath() throws Exception {
        // given
        var user = userDbHelper.save(UserRole.DISPATCHER);
        var employee = userDbHelper.save(UserRole.EMPLOYEE);
        var car = carDbHelper.save(employee);
        var accessToken = tokenDbHelper.generateAccessToken(user.getId());
        // when
        mockMvc.perform(MockMvcRequestBuilders.delete("/v1/cars/{id}", car.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        // then
    }
}
