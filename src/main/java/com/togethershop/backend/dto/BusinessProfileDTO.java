package com.togethershop.backend.dto;

import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class BusinessProfileDTO {

    private String businessName;
    private String businessCategory;
    private String address;
    private Integer togetherScore;
    private String profileImageUrl;

    @Builder.Default private Integer rankPercent = 10;
    @Builder.Default private Integer accumulatedDonations = 0;

    @Builder.Default private Metrics metrics = new Metrics();

    @Builder.Default private List<RequestItem> sentRequests = List.of();
    @Builder.Default private List<RequestItem> receivedRequests = List.of();

    @Builder.Default private List<GroupItem> groupApply = List.of();  // 내가 참가
    @Builder.Default private List<GroupItem> groupOwned  = List.of(); // 내가 개설

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Metrics {
        @Builder.Default private Long monthSalesIncrease = 131_000L;
        @Builder.Default private Integer newCustomersThisMonth = 45;
        @Builder.Default private Integer couponsUsedToday = 20;
        @Builder.Default private Double csat = 4.7;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RequestItem {
        private String partner;   // 상대 매장명
        private int daysAgo;      // N일 전
        // accept | wait | reject
        private String status;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class GroupItem {
        private String title;
        private int joined;
        private int target;
        private int dday;         // D-숫자
        // recruit | fail | pending | success
        private String status;
    }
}
