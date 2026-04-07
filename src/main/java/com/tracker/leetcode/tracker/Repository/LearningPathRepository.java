package com.tracker.leetcode.tracker.Repository;

import com.tracker.leetcode.tracker.Models.LearningPath;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface LearningPathRepository extends MongoRepository<LearningPath, String> {
    // Allows us to quickly fetch all paths created by a specific mentor
    List<LearningPath> findByMentorId(String mentorId);
}