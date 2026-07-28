package com.vaultflow.repository;

import com.vaultflow.entity.ActivityLog;
import com.vaultflow.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {
    List<ActivityLog> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
}
