package com.leafall.yourtaxi.controller;

import com.leafall.yourtaxi.BaseIntegrationTest;
import com.leafall.yourtaxi.core.db.*;
import com.leafall.yourtaxi.core.utils.dto.PointCreateDtoUtils;
import com.leafall.yourtaxi.core.utils.equals.PointEqualsUtils;
import com.leafall.yourtaxi.dispatch.SearchService;
import com.leafall.yourtaxi.dto.coordinates.CoordinateSaveDto;
import com.leafall.yourtaxi.dto.order.OrderCreateDto;
import com.leafall.yourtaxi.dto.order.OrderRedisWaitingChildDto;
import com.leafall.yourtaxi.dto.order.OrderRedisWaitingDto;
import com.leafall.yourtaxi.dto.order.OrderResponseDto;
import com.leafall.yourtaxi.dto.point.PointCreateDto;
import com.leafall.yourtaxi.dto.point.PointOSRMResponse;
import com.leafall.yourtaxi.dto.point.PointOSRMRoute;
import com.leafall.yourtaxi.dto.trip.TripStartDto;
import com.leafall.yourtaxi.entity.enums.OrderPaymentType;
import com.leafall.yourtaxi.entity.enums.OrderStatus;
import com.leafall.yourtaxi.entity.enums.UserRole;
import com.leafall.yourtaxi.dispatch.GeoService;
import com.leafall.yourtaxi.service.TripService;
import com.leafall.yourtaxi.utils.TimeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.HashSet;
import java.util.List;

import static com.leafall.yourtaxi.dispatch.DriverDispatchService.DRIVER_STATUS;
import static com.leafall.yourtaxi.dispatch.GeoService.DRIVER_COORDS_PREFIX;
import static com.leafall.yourtaxi.dispatch.SearchService.DRIVER_LOCK_PREFIX;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
public class OrderControllerTest extends BaseIntegrationTest {

    @Autowired
    private UserDbHelper userDbHelper;
    @Autowired
    private OrderDbHelper orderDbHelper;
    @Autowired
    private CarDbHelper carDbHelper;
    @Autowired
    private TripService tripService;
    @Autowired
    private SearchService searchService;
    @Autowired
    private TripDbHelper tripDbHelper;
    @Autowired
    private TokenDbHelper tokenDbHelper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @MockitoSpyBean
    private GeoService geoService;

    @BeforeEach
    void setUp() {
        dbCleaner.clear();
        redisCleaner.clear();
        reset(geoService);
    }

    @Test
    public void testCreateOrder_unauthorized() throws Exception {
        // given

        // when
         mockMvc.perform(post("/v1/orders")
                .content(objectMapper.writeValueAsString(new OrderCreateDto()))
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isUnauthorized());

        // then
    }

    @Test
    public void testCreateOrder_badRequest() throws Exception {
        // given
        var user = userDbHelper.save();
        var accessToken = tokenDbHelper.generateAccessToken(user.getId());
        // when
        mockMvc.perform(post("/v1/orders")
                .content(objectMapper.writeValueAsString(new OrderCreateDto()))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
        ).andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.status").exists());

        // then
    }

    @Test
    public void testCreateOrder_happyPath() throws Exception {
        // given
        // employee №1
        var employeeUser = userDbHelper.save(UserRole.EMPLOYEE);
        var car = carDbHelper.save();
        var trip = new TripStartDto();
        trip.setCarId(car.getId());
        tripService.startTrip(trip, employeeUser.getId());
        var dto = new CoordinateSaveDto();
        dto.setLongitude(10.0);
        dto.setLatitude(10.0);
        dto.setAngle(10.0);
        geoService.updateDriverLocation(employeeUser.getId(), dto);
        // client №2
        var clientUser = userDbHelper.save();
        var accessToken = tokenDbHelper.generateAccessToken(clientUser.getId());
        // order №3
        var from = PointCreateDtoUtils.generate(10, 10);
        var to = PointCreateDtoUtils.generate(10, 10);
        var order = new OrderCreateDto();
        order.setFrom(from);
        order.setTo(to);
        order.setPaymentType(OrderPaymentType.CARD);
        var pointOSRM = new PointOSRMResponse();
        var route = new PointOSRMRoute();
        route.setDistance(0.1);
        route.setDuration(10.0);
        pointOSRM.setRoutes(List.of(route));
        doReturn(pointOSRM).when(geoService).getDistance(any(PointCreateDto.class), any(PointCreateDto.class));
        // when
        var mvcResult = mockMvc.perform(post("/v1/orders")
                        .content(objectMapper.writeValueAsString(order))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                ).andExpect(status().isCreated())
                .andReturn();
        // then
        var bytes = mvcResult.getResponse().getContentAsByteArray();
        var response = objectMapper.readValue(bytes, OrderResponseDto.class);
        var firstOrderPoint = response.getPoints().stream().filter(item -> item.getIndex().equals(0)).findFirst().get();
        var secondOrderPoint = response.getPoints().stream().filter(item -> item.getIndex().equals(1)).findFirst().get();
        PointEqualsUtils.equals(order.getFrom(), firstOrderPoint);
        PointEqualsUtils.equals(order.getTo(), secondOrderPoint);
        assertEquals(order.getPaymentType(), response.getPaymentType());
    }

    @Test
    public void testAcceptOrder_happyPath() throws Exception {
        // given
        // employee №1
        var employeeUser = userDbHelper.save(UserRole.EMPLOYEE);
        var car = carDbHelper.save();
        var trip = new TripStartDto();
        trip.setCarId(car.getId());
        tripService.startTrip(trip, employeeUser.getId());
        var accessToken = tokenDbHelper.generateAccessToken(employeeUser.getId());
        // client №2
        var clientUser = userDbHelper.save();
        // order №3
        var order = orderDbHelper.save(clientUser, null);
        var dto = new CoordinateSaveDto();
        dto.setLongitude(10.0);
        dto.setLatitude(10.0);
        dto.setAngle(10.0);
        geoService.updateDriverLocation(employeeUser.getId(), dto);
        // when
        var mvcResult = mockMvc.perform(patch("/v1/orders/{id}/accept", order.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                ).andExpect(status().is2xxSuccessful())
                .andReturn();
        // then
        var bytes = mvcResult.getResponse().getContentAsByteArray();
        var response = objectMapper.readValue(bytes, OrderResponseDto.class);
        assertEquals(order.getPaymentType(), response.getPaymentType());
        assertEquals(response.getStatus(), OrderStatus.ACCEPT);
        assertEquals(response.getExecutor().getUser().getId(), employeeUser.getId());
        assertEquals("BUSY", redisTemplate.opsForHash().get(DRIVER_STATUS + employeeUser.getId(), "status"));
    }

    @Test
    public void testCompleteOrder_happyPath() throws Exception {
        // given
        // employee №1
        var employeeUser = userDbHelper.save(UserRole.EMPLOYEE);
        var car = carDbHelper.save();
        var trip = new TripStartDto();
        trip.setCarId(car.getId());
        var newTrip = tripService.startTrip(trip, employeeUser.getId());
        var accessToken = tokenDbHelper.generateAccessToken(employeeUser.getId());
        // client №2
        var clientUser = userDbHelper.save();
        // order №3
        var findedTrip = tripDbHelper.findById(newTrip.getId()).orElse(null);
        var order = orderDbHelper.save(clientUser, OrderStatus.IN_PROCESS, findedTrip);
        var dto = new CoordinateSaveDto();
        dto.setLongitude(10.0);
        dto.setLatitude(10.0);
        dto.setAngle(10.0);
        geoService.updateDriverLocation(employeeUser.getId(), dto);
        // when
        var mvcResult = mockMvc.perform(patch("/v1/orders/{id}/complete", order.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                ).andExpect(status().is2xxSuccessful())
                .andReturn();
        // then
        var bytes = mvcResult.getResponse().getContentAsByteArray();
        var response = objectMapper.readValue(bytes, OrderResponseDto.class);
        assertEquals(order.getPaymentType(), response.getPaymentType());
        assertEquals(response.getStatus(), OrderStatus.COMPLETED);
        assertEquals(response.getExecutor().getUser().getId(), employeeUser.getId());
        assertEquals("FREE", redisTemplate.opsForHash().get(DRIVER_STATUS + employeeUser.getId(), "status"));
    }

    @Test
    public void testCancelOrder_happyPath() throws Exception {
        // given
        //employee №0
        var rejectUser = userDbHelper.save(UserRole.EMPLOYEE);
        var rejectCar = carDbHelper.save();
        var rejectTrip = new TripStartDto();
        rejectTrip.setCarId(rejectCar.getId());
        var newRejectTrip = tripService.startTrip(rejectTrip, rejectUser.getId());
        // employee №1
        var employeeUser = userDbHelper.save(UserRole.EMPLOYEE);
        var car = carDbHelper.save();
        var trip = new TripStartDto();
        trip.setCarId(car.getId());
        var newTrip = tripService.startTrip(trip, employeeUser.getId());
        var accessToken = tokenDbHelper.generateAccessToken(rejectUser.getId());
        // client №2
        var clientUser = userDbHelper.save();
        // order №3
        var order = orderDbHelper.save(clientUser, null);
        var dto = new CoordinateSaveDto();
        dto.setLongitude(10.0);
        dto.setLatitude(10.0);
        dto.setAngle(10.0);
        geoService.updateDriverLocation(employeeUser.getId(), dto);
        geoService.updateDriverLocation(rejectUser.getId(), dto);
        var orderToDb = new OrderRedisWaitingDto();
        orderToDb.setId(order.getId());
        orderToDb.setLatitude(10.0);
        orderToDb.setLongitude(10.0);
        var set = new HashSet<OrderRedisWaitingChildDto>();
        var child = new OrderRedisWaitingChildDto();
        child.setId(rejectUser.getId().toString());
        child.setCreatedAt(TimeUtils.getCurrentTimeFromUTC());
        set.add(child);
        orderToDb.setIds(set);
        searchService.addToOrderQueue(orderToDb);
        // when
        var mvcResult = mockMvc.perform(post("/v1/orders/{id}/cancel", order.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                ).andExpect(status().is2xxSuccessful())
                .andReturn();
        // then
        var bytes = mvcResult.getResponse().getContentAsByteArray();
        var response = objectMapper.readValue(bytes, OrderResponseDto.class);
        assertEquals(order.getPaymentType(), response.getPaymentType());
        assertEquals(response.getStatus(), OrderStatus.NEW);
        assertEquals(Boolean.FALSE, redisTemplate.hasKey(DRIVER_LOCK_PREFIX + rejectUser.getId()));
        assertEquals(Boolean.TRUE, redisTemplate.hasKey(DRIVER_LOCK_PREFIX + employeeUser.getId()));
    }
}
