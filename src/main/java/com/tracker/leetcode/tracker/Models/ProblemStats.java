package com.tracker.leetcode.tracker.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProblemStats {
    private String difficulty;
    private int count;
    private double beatsPercentage;
}
