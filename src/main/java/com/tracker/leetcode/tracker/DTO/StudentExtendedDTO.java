package com.tracker.leetcode.tracker.DTO;

import com.tracker.leetcode.tracker.Models.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentExtendedDTO {
    private String name;
    private String leetcodeUsername;
    private String about;
    private String rank;
    private double currentContestRating;
    private SocialMedia socialMedia;
    private List<Badge> badges;
    private List<ContestHistory> contestHistory;
    private List<ProblemStats> problemStats;
    private List<RecentSubmission> recentSubmissions;
    private List<DailyProgress> progressHistory;
    private String id;
    private String email;
    private String avatarUrl;
    private List<String> manuallyCompletedAssignments;
    private List<Classroom> classrooms;
    private int consistencyStreak;
    private int totalSolved;
    private List<SkillStat> skills;
}