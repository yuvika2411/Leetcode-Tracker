package com.tracker.leetcode.tracker.DTO;

import com.tracker.leetcode.tracker.Models.ProblemStats;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class StudentStatsDTO {
    private String leetcodeUsername;
    private List<ProblemStats> problemStats;
}