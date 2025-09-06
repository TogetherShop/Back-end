package com.togethershop.backend.dto;

public enum ChatStatus {
    WAITING,            // 요청 보낸 상태(수신자 대기중)
    IN_NEGOTIATION,     // 협의 중(수락되어 채팅 허용)
    COMPLETED,          // 협의 완료(쿠폰 발급)
    REJECTED,          // 거절됨
    ACCEPTED
}
