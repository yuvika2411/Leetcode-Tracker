package com.tracker.leetcode.tracker.DTO;

import com.tracker.leetcode.tracker.Models.ProblemStats;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentStatsDTO {
    private String leetcodeUsername;
    private List<ProblemStats> problemStats;
}