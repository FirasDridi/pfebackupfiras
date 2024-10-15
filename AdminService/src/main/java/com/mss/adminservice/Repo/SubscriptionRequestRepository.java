package com.mss.adminservice.Repo;

import com.mss.adminservice.Entities.SubscriptionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRequestRepository extends JpaRepository<SubscriptionRequest, Long> {
    List<SubscriptionRequest> findByStatus(String status);

    Optional<SubscriptionRequest> findTopByGroupIdAndServiceIdOrderByIdDesc(Long groupId, UUID serviceId);
}
