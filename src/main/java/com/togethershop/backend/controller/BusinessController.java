package com.togethershop.backend.controller;

import com.togethershop.backend.dto.BusinessDTO;
import com.togethershop.backend.service.BusinessService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class BusinessController {

    private final BusinessService businessService;

    /** 초기 로딩/검색: 중심+반경(m) */
    @GetMapping("/nearby")
    public List<BusinessDTO> nearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5000") int radius, // m
            @RequestParam(defaultValue = "200") int limit
    ) {
        return businessService.findNearby(lat, lng, radius, limit);
    }

    /** 지도 이동 후: 현재 가시영역(바운딩박스) */
    @GetMapping("/in-bounds")
    public List<BusinessDTO> inBounds(
            @RequestParam double swLat,
            @RequestParam double swLng,
            @RequestParam double neLat,
            @RequestParam double neLng,
            @RequestParam double centerLat,
            @RequestParam double centerLng,
            @RequestParam(defaultValue = "200") int limit
    ) {
        return businessService.findInBounds(swLat, swLng, neLat, neLng, centerLat, centerLng, limit);
    }
}
