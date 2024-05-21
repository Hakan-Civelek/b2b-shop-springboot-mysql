package com.b2bshop.project.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

import javax.annotation.PostConstruct;

@Configuration
public class FirebaseConfig {

    @Bean
    public FirebaseApp initializeFirebase() throws IOException {
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.getApplicationDefault())
                .setDatabaseUrl("https://<VERİTABANI_ADİ>.firebaseio.com/")
                .build();

        FirebaseApp app = FirebaseApp.initializeApp(options);
        return app; // Başlatılan FirebaseApp nesnesini döndür
    }
}


