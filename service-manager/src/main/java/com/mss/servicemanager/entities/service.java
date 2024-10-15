package com.mss.servicemanager.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mss.annotations.searchable.Searchable;
import com.mss.annotations.searchable.SearchableEntity;
import com.mss.annotations.searchable.enums.Type;
import com.mss.entities.SimpleBaseEntity;
import com.mss.servicemanager.DTO.GroupDto;
import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@Entity(name = "service")
@SearchableEntity(getDepth = 1)
@EntityListeners(ServiceEntityListener.class)
@Data
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class service extends SimpleBaseEntity<String> {

    @Column(name = "name")
    @Searchable(type = Type.STRING, target = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "version")
    private String version;

    @Column(name = "endpoint")
    private String endpoint;

    @Column(name = "status")
    private boolean status;

    @Column(name = "configuration")
    private String configuration;

    @Column(name = "pricing")
    private String pricing;

    @Column(name = "access_token", unique = true)
    private String accessToken;

    @Transient
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Collection<GroupDto> groups = new ArrayList<>();

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

    public OffsetDateTime getCreatedDate() {
        return super.getCreatedDate();
    }

    public OffsetDateTime getLastModifiedDate() {
        return super.getLastModifiedDate();
    }

    public Collection<GroupDto> getGroups() {
        return groups;
    }

    public void setGroups(Collection<GroupDto> groups) {
        this.groups = groups;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }


}
