package com.leafall.yourtaxi.controller;

import com.leafall.yourtaxi.dto.coordinates.CoordinateSaveDto;
import com.leafall.yourtaxi.service.GeoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@MessageMapping("/v1/coords")
public class GeoController {
    private final GeoService geoService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/v1/coords")
    public ResponseEntity<List<Map<String, Object>>> getNearbyCoords(@RequestParam Double lon, @RequestParam Double lat, @RequestParam Double radius) {
        var geos = geoService.getNearbyDrivers(lon, lat, radius);
        return new ResponseEntity<>(geos, HttpStatus.OK);
    }

    @MessageMapping("/set/driver.location")
    @SendToUser("/queue/errors")
    public void setCoordinate(CoordinateSaveDto dto, SimpMessageHeaderAccessor headerAccessor) {
        UUID driverId = UUID.fromString((String) headerAccessor.getSessionAttributes().get("DRIVER_ID"));
        var result = geoService.updateDriverLocation(driverId, dto);
        messagingTemplate.convertAndSend("/topic/set/position", result);

    }
}
