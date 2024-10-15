package com.mss.adminservice.Repo;

import com.mss.adminservice.Entities.Group;
import com.mss.adminservice.Entities.ServiceGroupMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;

@Repository
public interface ServiceGroupMappingRepository extends JpaRepository<ServiceGroupMapping, Long> {
    List<ServiceGroupMapping> findByServiceId(String serviceId);
    void deleteByServiceNameAndGroupId(String serviceName, Long groupId);
    boolean existsByServiceNameAndGroupId(String serviceName, Long groupId); // Correct method signature

    HashSet<ServiceGroupMapping> findServiceGroupMappingByGroupIs(Group group);
    List<ServiceGroupMapping> findByGroup(Group group);
    void deleteByGroupAndServiceName(Group group, String serviceName);
}
