package com.togethershop.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Configuration
@RequiredArgsConstructor
public class FcmConfig {

    @Value("${fcm.key}")
    private String firebaseConfigPath;

    @PostConstruct
    public void initialize() throws IOException {
        // FirebaseApp이 이미 초기화 되어있는지 확인
        if (FirebaseApp.getApps().isEmpty()) {
            ClassPathResource resource = new ClassPathResource(firebaseConfigPath);
            try (InputStream inputStream = resource.getInputStream()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(inputStream))
                        .build();
                FirebaseApp.initializeApp(options);
            }
        }
    }
}