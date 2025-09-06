package com.togethershop.backend.service;

import com.togethershop.backend.domain.Businesses;
import com.togethershop.backend.domain.Coupon;
import com.togethershop.backend.domain.CouponStatus;
import com.togethershop.backend.dto.CouponIssueRequestDTO;
import com.togethershop.backend.dto.CouponResponseDTO;
import com.togethershop.backend.repository.BusinessRepository;
import com.togethershop.backend.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceImplTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private QRCodeService qrCodeService;

    @Mock
    private JwtJtiService jwtService;

    @InjectMocks
    private CouponServiceImpl couponService;

    private Businesses businesses;
    private Coupon coupon;

    @BeforeEach
    void setUp() {
        businesses = new Businesses();
        businesses.setId(1L);
        businesses.setName("Test Business");

        coupon = new Coupon();
        coupon.setCouponCode("TESTCODE");
        coupon.setBusinesses(businesses);
        coupon.setIssueDate(LocalDateTime.now().minusDays(1));
        coupon.setExpireDate(LocalDateTime.now().plusDays(5));
        coupon.setMinimumOrderAmount(BigDecimal.valueOf(10000));
        coupon.setStatus(CouponStatus.ISSUED);
        coupon.setJtiToken(UUID.randomUUID().toString());
    }

    @Test
    void testIssueCoupon_success() {
        CouponIssueRequestDTO requestDTO = new CouponIssueRequestDTO();
        requestDTO.setStoreId(businesses.getId());
        requestDTO.setCouponCode("TESTCODE");
        requestDTO.setExpiredAt(LocalDateTime.now().plusDays(5));
        requestDTO.setMinimumOrderAmount(BigDecimal.valueOf(10000));

        when(businessRepository.findById(anyLong())).thenReturn(Optional.of(businesses));
        when(couponRepository.save(any(Coupon.class))).thenAnswer(i -> i.getArgument(0));

        CouponResponseDTO responseDTO = couponService.issueCoupon(requestDTO);

        assertEquals("TESTCODE", responseDTO.getCouponCode());
        assertEquals(businesses.getId(), responseDTO.getStoreId());
        assertEquals(businesses.getName(), responseDTO.getStoreName());
        assertEquals(CouponStatus.ISSUED.name(), responseDTO.getStatus());
        assertEquals(BigDecimal.valueOf(10000), responseDTO.getMinimumOrderAmount());

        verify(businessRepository).findById(businesses.getId());
        verify(couponRepository).save(any(Coupon.class));
    }

    @Test
    void testReadCoupon_success() {
        when(couponRepository.findByCouponCode(anyString())).thenReturn(Optional.of(coupon));

        CouponResponseDTO responseDTO = couponService.readCoupon("TESTCODE");

        assertEquals("TESTCODE", responseDTO.getCouponCode());
        assertEquals(businesses.getName(), responseDTO.getStoreName());
        assertEquals(CouponStatus.ISSUED.name(), responseDTO.getStatus());

        verify(couponRepository).findByCouponCode("TESTCODE");
    }

    @Test
    void testReadCoupon_notFound() {
        when(couponRepository.findByCouponCode(anyString())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            couponService.readCoupon("NOTFOUND");
        });
        assertEquals("Coupon not found", exception.getMessage());
    }

    @Test
    void testGenerateCouponQrCode_success() throws Exception {
        String fakeJwtToken = "dummy.jwt.token";
        byte[] fakeQrCode = new byte[]{1, 2, 3};

        when(couponRepository.findByCouponCode("TESTCODE")).thenReturn(Optional.of(coupon));
        when(jwtService.generateTokenWithJti(anyString(), anyString())).thenReturn(fakeJwtToken);
        when(qrCodeService.generateQRCode(fakeJwtToken)).thenReturn(fakeQrCode);

        byte[] qrCode = couponService.generateCouponQrCode("TESTCODE");

        assertArrayEquals(fakeQrCode, qrCode);

        verify(couponRepository).findByCouponCode("TESTCODE");
        verify(jwtService).generateTokenWithJti("TESTCODE", coupon.getJtiToken());
        verify(qrCodeService).generateQRCode(fakeJwtToken);
    }

    @Test
    void testGenerateCouponQrCode_invalidStatus() {
        coupon.setStatus(CouponStatus.USED);
        when(couponRepository.findByCouponCode("TESTCODE")).thenReturn(Optional.of(coupon));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            couponService.generateCouponQrCode("TESTCODE");
        });
        assertEquals("Coupon is not valid for QR code generation", exception.getMessage());
    }

    @Test
    void testUseCoupon_success() {
        coupon.setStatus(CouponStatus.ISSUED);
        String jti = coupon.getJtiToken();

        when(couponRepository.findByCouponCode("TESTCODE")).thenReturn(Optional.of(coupon));
        when(couponRepository.save(any(Coupon.class))).thenAnswer(i -> i.getArgument(0));

        CouponResponseDTO responseDTO = couponService.useCoupon("TESTCODE", jti);

        assertEquals(CouponStatus.USED.name(), responseDTO.getStatus());
        assertNotNull(responseDTO.getUsedAt());

        verify(couponRepository).findByCouponCode("TESTCODE");
        verify(couponRepository).save(any(Coupon.class));
    }

    @Test
    void testUseCoupon_invalidJti() {
        coupon.setStatus(CouponStatus.ISSUED);
        when(couponRepository.findByCouponCode("TESTCODE")).thenReturn(Optional.of(coupon));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            couponService.useCoupon("TESTCODE", "invalid-jti");
        });
        assertEquals("Invalid coupon token", exception.getMessage());
    }

    @Test
    void testUseCoupon_alreadyUsed() {
        coupon.setStatus(CouponStatus.USED);
        when(couponRepository.findByCouponCode("TESTCODE")).thenReturn(Optional.of(coupon));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            couponService.useCoupon("TESTCODE", coupon.getJtiToken());
        });
        assertEquals("Coupon already used or invalid", exception.getMessage());
    }

    @Test
    void testCancelCouponUse_success() {
        coupon.setStatus(CouponStatus.USED);
        when(couponRepository.findByCouponCode("TESTCODE")).thenReturn(Optional.of(coupon));
        when(couponRepository.save(any(Coupon.class))).thenAnswer(i -> i.getArgument(0));

        CouponResponseDTO responseDTO = couponService.cancelCouponUse("TESTCODE");

        assertEquals(CouponStatus.CANCELLED.name(), responseDTO.getStatus());
        assertNull(responseDTO.getUsedAt());

        verify(couponRepository).findByCouponCode("TESTCODE");
        verify(couponRepository).save(any(Coupon.class));
    }

    @Test
    void testCancelCouponUse_invalidStatus() {
        coupon.setStatus(CouponStatus.ISSUED);
        when(couponRepository.findByCouponCode("TESTCODE")).thenReturn(Optional.of(coupon));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            couponService.cancelCouponUse("TESTCODE");
        });
        assertEquals("Only used coupons can be cancelled", exception.getMessage());
    }
}