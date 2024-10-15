package com.mss.adminservice.Repo;

import com.mss.adminservice.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String emailId);
    void deleteByKeycloakId(String keycloakId);
    Optional<User> findByKeycloakId(String keycloakId);
    List<User> findByGroupsId(Long groupId);
    List<User> findByServicesContaining(String serviceId);
    boolean existsByGroups_IdAndId(Long groupId, Long userId);

    boolean existsByIdAndServicesContaining(Long userId, String serviceName);

}
