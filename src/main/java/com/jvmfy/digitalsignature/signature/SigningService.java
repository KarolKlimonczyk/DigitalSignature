package com.jvmfy.digitalsignature.signature;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SigningService {

    private final String keyStorePath;
    private final String keyStorePassword;

    public SigningService(@Value(value = "keystore-path") String keyStorePath, @Value(value = "keystore-password") String keyStorePassword) {
        this.keyStorePath = keyStorePath;
        this.keyStorePassword = keyStorePassword;
    }
}
