package com.tracker.leetcode.tracker.DTO;

import com.tracker.leetcode.tracker.Models.Badge;
import com.tracker.leetcode.tracker.Models.ContestHistory;
import com.tracker.leetcode.tracker.Models.SocialMedia;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class StudentExtendedDTO {
    private String name;
    private String leetcodeUsername;
    private String about;
    private String rank;
    private double currentContestRating;
    private SocialMedia socialMedia;
    private List<Badge> badges;
    private List<ContestHistory> contestHistory;
}