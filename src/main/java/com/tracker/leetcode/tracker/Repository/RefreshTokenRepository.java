package com.tracker.leetcode.tracker.Repository;

import com.tracker.leetcode.tracker.Models.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends MongoRepository<RefreshToken,String > {
    Optional<RefreshToken> findByToken(String token);
    void deleteByMentorId(String mentor);
}
