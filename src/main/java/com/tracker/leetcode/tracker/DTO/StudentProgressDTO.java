package com.tracker.leetcode.tracker.DTO;

import com.tracker.leetcode.tracker.Models.DailyProgress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProgressDTO {
    private String leetcodeUsername;
    private List<DailyProgress> progressHistory;
}