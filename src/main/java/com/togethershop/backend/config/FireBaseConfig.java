package com.togethershop.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;

@Configuration
@Slf4j
public class FireBaseConfig {

    @Value("${fcm.key.path}")
    private String firebaseKeyPath;

    @PostConstruct
    public void initialize() {
        try (FileInputStream serviceAccount = new FileInputStream(firebaseKeyPath)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase App initialized successfully.");
            }
        } catch (Exception e) {
            log.error("Firebase initialization error: ", e);
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging() {
        return FirebaseMessaging.getInstance();
    }
}
