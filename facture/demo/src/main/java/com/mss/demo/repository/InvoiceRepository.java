package com.mss.demo.repository;

import com.mss.demo.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    List<Invoice> findByServiceId(UUID serviceId); // Change to UUID
    List<Invoice> findByUserId(UUID userId);
    List<Invoice> findByGroupId(Long groupId);
    boolean existsByUserIdAndGroupIdAndTimestampAndServiceId(UUID userId, Long groupId, LocalDateTime timestamp, UUID serviceId);
    Optional<Invoice> findByUserIdAndGroupIdAndTimestamp(UUID userId, Long groupId, LocalDateTime timestamp);






}
