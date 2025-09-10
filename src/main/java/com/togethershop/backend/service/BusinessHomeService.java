package com.togethershop.backend.service;

import com.togethershop.backend.domain.Business;
import com.togethershop.backend.domain.ChatRoom;
import com.togethershop.backend.dto.BusinessHomeResponseDTO;
import com.togethershop.backend.dto.ChatStatus;
import com.togethershop.backend.dto.PartnerDTO;
import com.togethershop.backend.dto.PartnerStatus;
import com.togethershop.backend.repository.BusinessRepository;
import com.togethershop.backend.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessHomeService {

    private final BusinessRepository businessRepository;
    private final ChatRoomRepository chatRoomRepository;

    /**
     * NOTE: memberId를 Business의 PK로 사용한다는 전제.
     * (엔티티를 수정하지 않으므로 owner 연관관계는 사용하지 않음)
     */
    public BusinessHomeResponseDTO getHomeSummary(Long memberId) {
        Business mine = businessRepository.findById(memberId)
                .orElseThrow(() -> new IllegalStateException("사용자 매장을 찾을 수 없습니다. (businessId=" + memberId + ")"));

        // ChatRoomRepository는 비즈니스 ID 기준으로 조회한다고 가정
        // 메서드 시그니처가 다르면 아래처럼 _Id 버전으로 리포지토리 메서드를 맞춰 주세요.
        List<ChatRoom> rooms = chatRoomRepository
                .findByRequesterIdOrRecipientIdOrderByCreatedAtDesc(mine.getId(), mine.getId());

        // partnershipId 단위 최신만 유지(없으면 roomId 기준)
        Map<String, ChatRoom> latestByKey = new LinkedHashMap<>();
        for (ChatRoom cr : rooms) {
            String key = (cr.getPartnershipId() != null) ? "p:" + cr.getPartnershipId() : "r:" + cr.getRoomId();
            latestByKey.putIfAbsent(key, cr); // rooms가 최신순이므로 먼저 put된 것이 최신
        }

        var partnerItems = latestByKey.values().stream()
                .map(cr -> {
                    Business other = resolveOtherParty(mine, cr);
                    PartnerStatus pStatus = mapToPartnerStatus(cr.getStatus());
                    return PartnerDTO.builder()
                            .id(other != null ? other.getId() : null)
                            .name(other != null ? other.getBusinessName() : "(알 수 없음)")
                            .status(pStatus)
                            .statusLabel(toKoreanLabel(pStatus))
                            .detail("제휴 대화방 ID: " + cr.getRoomId())
                            .build();
                })
                .collect(Collectors.toList());

        return BusinessHomeResponseDTO.builder()
                .businessName(mine.getBusinessName())
                .togetherScore(toIntScore(mine.getTogetherIndex()))
                .partners(partnerItems)
                .build();
    }

    private Business resolveOtherParty(Business mine, ChatRoom cr) {
        if (cr.getRequester() != null && Objects.equals(cr.getRequester().getId(), mine.getId())) {
            return cr.getRecipient();
        }
        return cr.getRequester();
    }

    private PartnerStatus mapToPartnerStatus(ChatStatus chatStatus) {
        if (chatStatus == null) return PartnerStatus.PENDING;
        switch (chatStatus) {
            case COMPLETED: return PartnerStatus.ACTIVE;
            case WAITING:   return PartnerStatus.PENDING;
            case REJECTED:  return PartnerStatus.ENDED;
            default:        return PartnerStatus.PENDING;
        }
    }

    private String toKoreanLabel(PartnerStatus status) {
        if (status == null) return "";
        switch (status) {
            case ACTIVE:  return "활성";
            case PENDING: return "협의중";
            case ENDED:   return "종료";
            default:      return "";
        }
    }

    private Integer toIntScore(Double val) {
        if (val == null) return 0;
        return (int) Math.round(val);
    }
}
