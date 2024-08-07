package com.mss.servicemanager.DTO;

import com.mss.dto.base.SimpleBaseDTO;
import com.mss.servicemanager.entities.service;
import groovy.transform.builder.Builder;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.UUID;

@Builder
public class ServiceDto extends SimpleBaseDTO<service> {

    private UUID id;
    private String name;
    private String description;
    private String version;
    private String endpoint;
    private boolean status;
    private String configuration;
    private String pricing;
    private Collection<GroupDto> groups;
    private OffsetDateTime createdDate;
    private OffsetDateTime lastModifiedDate;
    private String accessToken ;

    // Getters and setters for all fields

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public String getPricing() {
        return pricing;
    }

    public void setPricing(String pricing) {
        this.pricing = pricing;
    }

    public Collection<GroupDto> getGroups() {
        return groups;
    }

    public void setGroups(Collection<GroupDto> groups) {
        this.groups = groups;
    }

    public OffsetDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(OffsetDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public OffsetDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(OffsetDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }
}
