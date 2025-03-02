package com.example.sms.repository;

import com.example.sms.entity.FeedBack;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedBackRepository extends JpaRepository<FeedBack, Long> {
    Optional<FeedBack> findById(Long id);
}
