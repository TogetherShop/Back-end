package com.togethershop.backend.service;

import com.togethershop.backend.domain.Coupon;
import com.togethershop.backend.domain.CouponStatus;
import com.togethershop.backend.dto.CouponIssueRequestDTO;
import com.togethershop.backend.dto.CouponResponseDTO;
import com.togethershop.backend.repository.BusinessRepository;
import com.togethershop.backend.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final QRCodeService qrCodeService;
    private final BusinessRepository businessRepository;
    private final JwtJtiService jwtService;

    @Transactional
    public CouponResponseDTO issueCoupon(CouponIssueRequestDTO dto) {
        var business = businessRepository.findById(dto.getStoreId())
                .orElseThrow(() -> new RuntimeException("Store not found"));

        var coupon = new Coupon();
        coupon.setCouponCode(dto.getCouponCode());
        coupon.setBusiness(business);
        coupon.setIssuedAt(LocalDateTime.now());
        coupon.setExpiredAt(dto.getExpiredAt());
        coupon.setMinimumOrderAmount(dto.getMinimumOrderAmount());
        coupon.setStatus(CouponStatus.ISSUED);
        coupon.setJwtJti(UUID.randomUUID().toString());

        couponRepository.save(coupon);

        return toResponseDto(coupon);
    }

    @Transactional(readOnly = true)
    public CouponResponseDTO readCoupon(String couponCode) {
        var coupon = couponRepository.findByCouponCode(couponCode)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        return toResponseDto(coupon);
    }

    @Transactional(readOnly = true)
    public byte[] generateCouponQrCode(String couponCode) throws Exception {
        var coupon = couponRepository.findByCouponCode(couponCode)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        if (coupon.getStatus() != CouponStatus.ISSUED) {
            throw new RuntimeException("Coupon is not valid for QR code generation");
        }
        if (coupon.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Coupon expired");
        }

        String jwtToken = jwtService.generateTokenWithJti(couponCode, coupon.getJwtJti());
        return qrCodeService.generateQRCode(jwtToken);
    }

    @Transactional
    public CouponResponseDTO useCoupon(String couponCode, String jti) {
        var coupon = couponRepository.findByCouponCode(couponCode)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        if (!coupon.getJwtJti().equals(jti)) {
            throw new RuntimeException("Invalid coupon token");
        }

        if (coupon.getStatus() != CouponStatus.ISSUED) {
            throw new RuntimeException("Coupon already used or invalid");
        }

        if (coupon.getExpiredAt().isBefore(LocalDateTime.now())) {
            coupon.setStatus(CouponStatus.EXPIRED);
            couponRepository.save(coupon);
            throw new RuntimeException("Coupon expired");
        }

        coupon.setStatus(CouponStatus.USED);
        coupon.setUsedAt(LocalDateTime.now());

        var saved = couponRepository.save(coupon);

        return toResponseDto(saved);
    }

    @Transactional
    public CouponResponseDTO cancelCouponUse(String couponCode) {
        var coupon = couponRepository.findByCouponCode(couponCode)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        if (coupon.getStatus() != CouponStatus.USED) {
            throw new RuntimeException("Only used coupons can be cancelled");
        }

        coupon.setStatus(CouponStatus.CANCELLED);
        coupon.setUsedAt(null);  // 사용 일시 초기화 또는 필요에 따라 기록 유지

        var saved = couponRepository.save(coupon);
        return toResponseDto(saved);
    }


    private CouponResponseDTO toResponseDto(Coupon coupon) {
        return new CouponResponseDTO(
                coupon.getCouponCode(),
                coupon.getBusiness().getId(),
                coupon.getBusiness().getName(),
                coupon.getIssuedAt(),
                coupon.getExpiredAt(),
                coupon.getUsedAt(),
                coupon.getStatus().name(),
                coupon.getMinimumOrderAmount()
        );
    }
}
