package com.mss.servicemanager.Repositories;

import com.mss.servicemanager.entities.service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TestRepo  extends JpaRepository<service, UUID> {
}
