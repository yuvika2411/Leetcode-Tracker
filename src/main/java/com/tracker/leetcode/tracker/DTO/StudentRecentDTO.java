package com.tracker.leetcode.tracker.DTO;

import com.tracker.leetcode.tracker.Models.RecentSubmission;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class StudentRecentDTO {
    private String leetcodeUsername;
    private List<RecentSubmission> recentSubmissions;
}