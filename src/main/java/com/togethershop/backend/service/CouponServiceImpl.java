package com.togethershop.backend.service;

import com.togethershop.backend.domain.Businesses;
import com.togethershop.backend.domain.Coupon;
import com.togethershop.backend.domain.CouponStatus;
import com.togethershop.backend.dto.CouponIssueRequestDTO;
import com.togethershop.backend.dto.CouponResponseDTO;
import com.togethershop.backend.repository.BusinessRepository;
import com.togethershop.backend.repository.CouponRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final QRCodeService qrCodeService;
    private final BusinessRepository businessRepository;
    private final JwtJtiService jwtService;

    @Transactional
    public CouponResponseDTO issueCoupon(CouponIssueRequestDTO dto) {
        Businesses businesses = businessRepository.findById(dto.getBusinessId())
                .orElseThrow(() -> new RuntimeException("Store not found"));

        String jtiValue = UUID.randomUUID().toString();

        Coupon coupon = Coupon.builder()
                .couponCode(dto.getCouponCode())
                .businesses(businesses)
                .templateId(1L)  // 필요시 dto에서 값 받는 것도 가능
                .customerId(1L)  // 임시값, dto에서 받도록 개선 가능
                .expireDate(dto.getExpiredDate())
                .status(CouponStatus.ISSUED)
                .jtiToken(jtiValue)
                .issueDate(LocalDateTime.now())
                .build();

        couponRepository.save(coupon);

        return toResponseDto(coupon);
    }

    @Transactional(readOnly = true)
    public CouponResponseDTO readCoupon(String couponCode) {
        Coupon coupon = couponRepository.findByCouponCode(couponCode)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        return toResponseDto(coupon);
    }

    @Transactional
    public byte[] generateCouponQrCode(String couponCode) throws Exception {
        saveQrCodeDataAndPin(couponCode);

        Coupon coupon = couponRepository.findByCouponCode(couponCode)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        if (coupon.getStatus() != CouponStatus.ISSUED) {
            throw new RuntimeException("Coupon is not valid for QR code generation");
        }
        if (coupon.getExpireDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Coupon expired");
        }

        String jwtToken = jwtService.generateTokenWithJti(couponCode, coupon.getJtiToken());
        return qrCodeService.generateQRCode(jwtToken);
    }

    public void saveQrCodeDataAndPin(String couponCode) {
        Coupon coupon = couponRepository.findByCouponCode(couponCode)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        if (coupon.getStatus() != CouponStatus.ISSUED) {
            throw new RuntimeException("Coupon is not valid for QR code generation");
        }
        if (coupon.getExpireDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Coupon expired");
        }

        String qrData = "QR_" + couponCode + "_DATA";
        coupon.setQrCodeData(qrData);

        int pinCode = ThreadLocalRandom.current().nextInt(1000, 10000);
        log.info("Saving QR code data : {},{}", qrData, pinCode);

        coupon.setPinCode(String.valueOf(pinCode));

        couponRepository.save(coupon);
    }

    @Transactional
    public CouponResponseDTO useCoupon(String couponCode, String jti) {
        Coupon coupon = couponRepository.findByCouponCode(couponCode)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        if (!coupon.getJtiToken().equals(jti)) {
            throw new RuntimeException("Invalid coupon token");
        }

        if (coupon.getStatus() != CouponStatus.ISSUED) {
            throw new RuntimeException("Coupon already used or invalid");
        }

        if (coupon.getExpireDate().isBefore(LocalDateTime.now())) {
            coupon.setStatus(CouponStatus.EXPIRED);
            couponRepository.save(coupon);
            throw new RuntimeException("Coupon expired");
        }

        coupon.setStatus(CouponStatus.USED);
        coupon.setUsedDate(LocalDateTime.now());

        Coupon savedCoupon = couponRepository.save(coupon);

        return toResponseDto(savedCoupon);
    }

    @Transactional
    public CouponResponseDTO cancelCouponUse(String couponCode) {
        Coupon coupon = couponRepository.findByCouponCode(couponCode)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        if (coupon.getStatus() != CouponStatus.USED) {
            throw new RuntimeException("Only used coupons can be cancelled");
        }

        coupon.setStatus(CouponStatus.CANCELLED);
        coupon.setUsedDate(null);

        Coupon savedCoupon = couponRepository.save(coupon);

        return toResponseDto(savedCoupon);
    }

    private CouponResponseDTO toResponseDto(Coupon coupon) {
        return new CouponResponseDTO(
                coupon.getCouponCode(),
                coupon.getBusinesses().getId(),
                coupon.getBusinesses().getName(),
                coupon.getIssueDate(),
                coupon.getExpireDate(),
                coupon.getUsedDate(),
                coupon.getStatus().name()
        );
    }
}
