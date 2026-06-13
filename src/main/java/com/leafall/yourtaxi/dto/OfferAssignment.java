package com.leafall.yourtaxi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OfferAssignment {
    private UUID orderId;
    private UUID driverId;
    private Long createdAt;
    private Long expiresAt;
}
