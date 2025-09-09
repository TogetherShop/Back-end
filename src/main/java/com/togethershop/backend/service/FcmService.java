package com.togethershop.backend.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.togethershop.backend.domain.Business;
import com.togethershop.backend.domain.Customer;
import com.togethershop.backend.dto.FcmSendDTO;
import com.togethershop.backend.repository.BusinessRepository;
import com.togethershop.backend.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class FcmService {

    private final FirebaseMessaging firebaseMessaging;
    private final CustomerRepository customerRepository;
    private final BusinessRepository businessRepository;


    @Transactional
    public void updateCustomerFcmToken(Long customerId, String fcmToken) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new UsernameNotFoundException("Customer User not found"));
        customer.setCustomerFcmToken(fcmToken);
        customerRepository.save(customer);
    }
    @Transactional
    public void updateBusinessFcmToken(Long businessId, String fcmToken) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new UsernameNotFoundException("Business User not found"));
        business.setBusinessFcmToken(fcmToken);
        businessRepository.save(business);
    }

    public boolean sendNotification(FcmSendDTO dto) {
        try {
            Notification.Builder notificationBuilder = Notification.builder()
                    .setTitle(dto.getTitle())
                    .setBody(dto.getBody());

            if (dto.getImage()!=null) {
                notificationBuilder.setImage(dto.getImage());
            }

            Message message = Message.builder()
                    .setToken(dto.getToken())
                    .setNotification(notificationBuilder.build())
                    .build();

            String response = firebaseMessaging.send(message);
            log.info("Firebase message sent successfully: {}", response);
            return true;
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send Firebase message: {}", e.getMessage());
            return false;
        }
    }

    public ResponseEntity<String> sendTestNotification(FcmSendDTO dto){
        try {
            Notification.Builder notificationBuilder = Notification.builder()
                    .setTitle(dto.getTitle())
                    .setBody(dto.getBody());

            if (dto.getImage()!=null) {
                notificationBuilder.setImage(dto.getImage());
            }

            Message message = Message.builder()
                    .setToken(dto.getToken())
                    .setNotification(notificationBuilder.build())
                    .build();

            String response = firebaseMessaging.send(message);
            log.info("Firebase message sent : {}", message.toString());
            return ResponseEntity.ok(response);
        } catch (FirebaseMessagingException e) {
            log.error(e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

}
