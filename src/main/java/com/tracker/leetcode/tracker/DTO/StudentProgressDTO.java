package com.tracker.leetcode.tracker.DTO;

import com.tracker.leetcode.tracker.Models.DailyProgress;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class StudentProgressDTO {
    private String leetcodeUsername;
    private List<DailyProgress> progressHistory;
}