package com.mss.servicemanager.entities;

import jakarta.persistence.PrePersist;
import org.apache.commons.lang3.RandomStringUtils;

public class ServiceEntityListener {

    @PrePersist
    public void prePersist(service service) {
        if (service.getAccessToken() == null || service.getAccessToken().isEmpty()) {
            service.setAccessToken(RandomStringUtils.randomAlphanumeric(20)); // Generates a random alphanumeric string of 20 characters
        }
    }
}
