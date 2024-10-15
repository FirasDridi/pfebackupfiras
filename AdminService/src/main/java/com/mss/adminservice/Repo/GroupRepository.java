
package com.mss.adminservice.Repo;

import com.mss.adminservice.Entities.Group;
import com.mss.adminservice.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByName(String name);
    Optional<Group> findByKeycloakId(String keycloakId); // Change this line

    List<Group> findByNameContaining(String groupName);

    List<Group> findByUsersContaining(User user);
    @Query("SELECT g FROM Group g JOIN g.accessTokens t WHERE KEY(t) = :serviceId AND VALUE(t) = :accessToken")
    Optional<Group> findByAccessToken(@Param("serviceId") UUID serviceId, @Param("accessToken") String accessToken);

    @Query("SELECT g FROM Group g JOIN g.accessTokens t WHERE VALUE(t) = :accessToken")
    Optional<Group> findByAccessTokenValue(@Param("accessToken") String accessToken);

    @Query("SELECT g.id FROM Group g WHERE g.name = :groupName")
    Long findGroupIdByGroupName(String groupName);
}
