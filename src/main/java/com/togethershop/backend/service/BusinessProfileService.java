package com.togethershop.backend.service;

import com.togethershop.backend.domain.Business;
import com.togethershop.backend.domain.GroupPurchaseParticipant;
import com.togethershop.backend.domain.GroupPurchaseProject;
import com.togethershop.backend.domain.Partnership;
import com.togethershop.backend.dto.BusinessProfileDTO;
import com.togethershop.backend.dto.PartnershipStatus;
import com.togethershop.backend.repository.BusinessRepository;
import com.togethershop.backend.repository.GroupPurchaseParticipantRepository;
import com.togethershop.backend.repository.GroupPurchaseProjectRepository;
import com.togethershop.backend.repository.PartnershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusinessProfileService {

    private final BusinessRepository businessRepository;
    private final PartnershipRepository partnershipRepository;
    private final GroupPurchaseProjectRepository projectRepository;
    private final GroupPurchaseParticipantRepository participantRepository;

    public BusinessProfileDTO getProfileSummaryByUsername(String username) {
        // ✔️ 레포 시그니처 맞춤
        Business me = businessRepository.findFirstByUsernameOrderByIdAsc(username)
                .orElseThrow(() -> new IllegalArgumentException("Business not found: " + username));

        // 제휴 요청함: 내가 보낸/받은 최근 항목
        List<Partnership> sent = partnershipRepository.findByRequester_Id(me.getId());
        List<Partnership> received = partnershipRepository.findByPartner_Id(me.getId());

        List<BusinessProfileDTO.RequestItem> sentDtos = sent.stream()
                .sorted(Comparator.comparing(Partnership::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .map(p -> BusinessProfileDTO.RequestItem.builder()
                        .partner(safeName(p.getPartner()))
                        .daysAgo(daysAgo(p))
                        .status(mapPartnerStatus(p.getStatus()))
                        .build())
                .toList();

        List<BusinessProfileDTO.RequestItem> recvDtos = received.stream()
                .sorted(Comparator.comparing(Partnership::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .map(p -> BusinessProfileDTO.RequestItem.builder()
                        .partner(safeName(p.getRequester()))
                        .daysAgo(daysAgo(p))
                        .status(mapPartnerStatus(p.getStatus()))
                        .build())
                .toList();

        // 누적 제휴(예시: ACCEPTED/COMPLETED 카운트)
        int accumulated = (int) (sent.stream().filter(this::isApprovedLike).count()
                + received.stream().filter(this::isApprovedLike).count());

        // ✅ 공동구매: 내가 ‘개설’한 프로젝트
        List<GroupPurchaseProject> owned = projectRepository
                .findByBusinessIdOrderByCreatedAtDesc(me.getId());

        List<BusinessProfileDTO.GroupItem> groupOwnedDtos = owned.stream()
                .limit(10)
                .map(this::toGroupItem)
                .toList();

        // ✅ 공동구매: 내가 ‘참여’한 프로젝트 (참여 엔티티 → projectId 모아 다시 로드)
        var page0 = PageRequest.of(0, 20);
        List<GroupPurchaseParticipant> myPart = participantRepository
                .findByBusinessIdOrderByJoinedAtDesc(me.getId(), page0)
                .getContent();

        // 중복 projectId 제거 후 상위 10개만
        List<Long> participatedProjectIds = myPart.stream()
                .map(GroupPurchaseParticipant::getProjectId)
                .filter(Objects::nonNull)
                .distinct()
                .limit(10)
                .toList();

        List<GroupPurchaseProject> participatedProjects = participatedProjectIds.stream()
                .map(pid -> projectRepository.findById(pid).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<BusinessProfileDTO.GroupItem> groupApplyDtos = participatedProjects.stream()
                .map(this::toGroupItem)
                .toList();

        return BusinessProfileDTO.builder()
                .businessName(me.getBusinessName())
                .businessCategory(me.getBusinessCategory())
                .address(me.getAddress())
                .togetherScore(toIntScore(me.getTogetherIndex()))
                .profileImageUrl(me.getProfileImageUrl())
                .accumulatedDonations(accumulated)
                .sentRequests(sentDtos)
                .receivedRequests(recvDtos)
                .groupOwned(groupOwnedDtos)
                .groupApply(groupApplyDtos)
                .build();
    }

    private String safeName(Business b) {
        return b == null ? "알 수 없음" : Objects.toString(b.getBusinessName(), "알 수 없음");
    }

    private int daysAgo(Partnership p) {
        if (p.getCreatedAt() == null) return 0;
        return (int) ChronoUnit.DAYS.between(p.getCreatedAt().toLocalDate(), LocalDate.now());
    }

    private boolean isApprovedLike(Partnership p) {
        PartnershipStatus s = p.getStatus();
        return s == PartnershipStatus.ACCEPTED || s == PartnershipStatus.COMPLETED;
    }

    private String mapPartnerStatus(PartnershipStatus s) {
        // 프론트 요구: accept | wait | reject
        return switch (s) {
            case ACCEPTED, COMPLETED -> "accept";
            case REQUESTED, IN_NEGOTIATION -> "wait";
            case REJECTED -> "reject";
        };
    }

    private BusinessProfileDTO.GroupItem toGroupItem(GroupPurchaseProject gp) {
        // 🧩 참여자 수는 participantRepo에서 카운트
        int joined = Optional.ofNullable(participantRepository.countParticipantsByProjectId(gp.getId()))
                .map(Long::intValue).orElse(0);

        // 🧩 목표 인원은 엔티티의 targetNumber 사용 (프로젝트 서비스에서 사용 중인 필드)
        int target = Optional.ofNullable(gp.getTargetNumber()).orElse(0);

        // 🧩 제목은 description 사용 (DTO에 title 역할로 쓰고 있음)
        String title = Optional.ofNullable(gp.getDescription()).orElse("공동구매");

        // 🧩 마감일에서 디데이 계산
        int dday = 0;
        if (gp.getEndDate() != null) {
            LocalDate end = gp.getEndDate().toLocalDate();
            dday = (int) ChronoUnit.DAYS.between(LocalDate.now(), end);
        }

        String status = mapGroupStatus(gp, joined, target, dday);

        return BusinessProfileDTO.GroupItem.builder()
                .title(title)
                .joined(joined)
                .target(target)
                .dday(Math.max(dday, 0))
                .status(status)
                .build();
    }

    private String mapGroupStatus(GroupPurchaseProject gp, int joined, int target, int dday) {
        // 프론트: recruit | fail | pending | success
        GroupPurchaseProject.ProjectStatus st = gp.getStatus();
        if (st == null || st == GroupPurchaseProject.ProjectStatus.OPEN) {
            if (dday > 0) return "recruit";
            if (dday <= 0 && target > 0 && joined < target) return "fail";
            if (dday <= 0 && target > 0 && joined >= target) return "success";
            return "pending";
        }
        return switch (st) {
            case FULFILLED -> "success";
            case CANCELLED -> "fail";
            case CLOSED -> (target > 0 && joined >= target) ? "success" : "fail";
            default -> "pending";
        };
    }

    private Integer toIntScore(Double d) {
        return d == null ? null : (int) Math.round(d);
    }
}
