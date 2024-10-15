package com.mss.adminservice.Entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mss.adminservice.Config.UserGroupDTO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "groups")
@Data
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Group implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String keycloakId;
    private boolean tokenGenerated;
    private String description;
    private Boolean paid;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "group_access_tokens", joinColumns = @JoinColumn(name = "group_id"))
    @MapKeyColumn(name = "service_id")
    @Column(name = "access_token")
    private Map<UUID, String> accessTokens = new HashMap<>();

    @ManyToMany
    @JoinTable(
            name = "group_users",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> users = new HashSet<>();

    @OneToMany(mappedBy = "group")
    private Set<Role> roles;
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private Set<SubscriptionRequest> subscriptionRequests = new HashSet<>();
    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Group group = (Group) obj;
        return Objects.equals(id, group.id) && Objects.equals(name, group.name);
    }

    public Group(UserGroupDTO data) {
        this.name = data.getName();
        this.description = data.getDescription();
    }
}
