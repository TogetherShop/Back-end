package com.togethershop.backend.service;

import com.togethershop.backend.domain.Business;
import com.togethershop.backend.domain.ChatRoom;
import com.togethershop.backend.dto.BusinessHomeResponseDTO;
import com.togethershop.backend.dto.ChatStatus;
import com.togethershop.backend.domain.Partnership;
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

    public BusinessHomeResponseDTO getHomeSummaryByUsername(String username) {
        Business me = businessRepository.findFirstByUsernameOrderByIdAsc(username)
                .orElseThrow(() -> new IllegalStateException("사용자 매장을 찾을 수 없습니다. username=" + username));

        List<ChatRoom> rooms = chatRoomRepository
                .findByRequesterIdOrRecipientIdOrderByCreatedAtDesc(me.getId(), me.getId());

        Map<String, ChatRoom> latestByKey = new LinkedHashMap<>();
        for (ChatRoom cr : rooms) {
            String key = (cr.getPartnership() != null)
                    ? "p:" + cr.getPartnership().getId()
                    : "r:" + cr.getRoomId();
            latestByKey.putIfAbsent(key, cr);
        }

        List<PartnerDTO> items = latestByKey.values().stream()
                .map(cr -> {
                    Business other = resolveOther(me, cr);
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
                .businessName(me.getBusinessName())
                .togetherScore(toIntScore(me.getTogetherIndex()))
                .partners(items)
                .build();
    }

    private Business resolveOther(Business me, ChatRoom cr) {
        if (cr.getRequester() != null && Objects.equals(cr.getRequester().getId(), me.getId())) {
            return cr.getRecipient();
        }
        return cr.getRequester();
    }

    private PartnerStatus mapToPartnerStatus(ChatStatus s) {
        if (s == null) return PartnerStatus.PENDING;
        switch (s) {
            case ACCEPTED:       return PartnerStatus.ACTIVE;
            case IN_NEGOTIATION: return PartnerStatus.PENDING;
            case REJECTED:       return PartnerStatus.ENDED;
            default:             return PartnerStatus.PENDING;
        }
    }

    private String toKoreanLabel(PartnerStatus st) {
        if (st == null) return "";
        switch (st) {
            case ACTIVE:  return "활성";
            case PENDING: return "협의중";
            case ENDED:   return "종료";
            default:      return "";
        }
    }

    private int toIntScore(Double v) {
        return v == null ? 0 : (int)Math.round(v);
    }
}
