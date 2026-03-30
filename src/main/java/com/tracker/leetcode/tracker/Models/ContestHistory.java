package com.tracker.leetcode.tracker.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContestHistory {
    private String title;
    private long timestamp;
    private double rating;
    private int ranking;
    private int problemsSolved;
    private int totalProblems;
}