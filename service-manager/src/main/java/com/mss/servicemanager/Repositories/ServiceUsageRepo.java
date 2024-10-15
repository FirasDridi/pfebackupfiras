package com.mss.servicemanager.Repositories;


import com.mss.advanced.specifications.models.SearchData;
import com.mss.base.repositories.BaseRepository;
import com.mss.search.models.SearchCriteria;
import com.mss.search.models.SearchResponse;
import com.mss.servicemanager.entities.service;
import org.springframework.data.history.Revision;
import org.springframework.data.history.Revisions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceUsageRepo  extends BaseRepository<service, UUID>   {
    @Query("SELECT s FROM service s")
    List<service> findAll();
    Optional<service> findByAccessToken(String accessToken);

    Optional<service> findById(UUID id);

    Optional<service> findByEndpoint(String endpoint);
}
