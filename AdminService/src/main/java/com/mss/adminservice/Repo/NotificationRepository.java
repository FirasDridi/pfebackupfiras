// NotificationRepository.java
package com.mss.adminservice.Repo;

import com.mss.adminservice.Entities.Notification;
import com.mss.adminservice.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserAndReadFalse(User user);
    Optional<Notification> findBySubscriptionRequestId(Long requestId);
    List<Notification> findByReadFalse();
}
