package com.mss.adminservice.Service;

import com.mss.adminservice.Entities.Notification;
import com.mss.adminservice.Entities.SubscriptionRequest;
import com.mss.adminservice.Entities.User;
import com.mss.adminservice.Repo.NotificationRepository;
import com.mss.adminservice.Repo.SubscriptionRequestRepository;
import com.mss.adminservice.Repo.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final SubscriptionRequestRepository subscriptionRequestRepository;
    private final UserRepository userRepository;

    public List<Notification> getNotificationsForUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return notificationRepository.findByUserAndReadFalse(user);
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    public void createNotification(String message, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setRead(false);
        notification.setTimestamp(LocalDateTime.now());
        notification.setUser(user);
        notificationRepository.save(notification);
    }

    public void markNotificationAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void markNotificationAsReadByRequestId(Long requestId) {
        Notification notification = notificationRepository.findBySubscriptionRequestId(requestId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void notifyAdminOfSubscriptionRequest(Long subscriptionRequestId, Long userId, Long groupId) {
        SubscriptionRequest request = subscriptionRequestRepository.findById(subscriptionRequestId)
                .orElseThrow(() -> new RuntimeException("Subscription request not found: " + subscriptionRequestId));

        Notification notification = new Notification();
        notification.setMessage("Subscription request for service ID: " + request.getServiceId() + " from group ID: " + request.getGroupId());
        notification.setRead(false);
        notification.setTimestamp(LocalDateTime.now());
        notification.setUserId(userId);
        notification.setGroupId(groupId);
        notification.setSubscriptionRequest(request);
        notificationRepository.save(notification);
    }

    public List<Notification> getUnreadNotifications() {
        return notificationRepository.findByReadFalse();
    }
}
