package com.kgu.life_watch.domain.notification.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;

import com.kgu.life_watch.global.exception.ErrorCode;
import com.kgu.life_watch.global.exception.LifelineException;

@Configuration
public class FirebaseConfig {

  @Value("${FIREBASE_CONFIG_BASE64}")
  String base64Config;

  @PostConstruct
  public void init() throws IOException {
    if (base64Config == null || base64Config.isEmpty()) {
      throw LifelineException.from(ErrorCode.FIREBASE_ENV_NOT_FOUND);
    }

    byte[] decoded = Base64.getDecoder().decode(base64Config);
    ByteArrayInputStream serviceAccount = new ByteArrayInputStream(decoded);

    FirebaseOptions options =
        FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build();

    if (FirebaseApp.getApps().isEmpty()) {
      FirebaseApp.initializeApp(options);
    }
  }
}
