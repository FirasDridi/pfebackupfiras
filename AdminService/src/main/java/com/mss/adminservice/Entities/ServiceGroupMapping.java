package com.mss.adminservice.Entities;

import com.mss.adminservice.Entities.Group;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ServiceGroupMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "service_id")
    private String serviceId;
    @Column(name = "service_name")
    private String serviceName;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }
}
