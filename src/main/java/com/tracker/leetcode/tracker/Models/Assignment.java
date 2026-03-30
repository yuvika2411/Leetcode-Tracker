package com.tracker.leetcode.tracker.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Assignment {
    private String id = UUID.randomUUID().toString();

    private String titleSlug; // e.g., "two-sum"

    // NEW: The clickable URL for the frontend
    private String questionLink;

    private long startTimestamp;
    private long endTimestamp;

    // Custom setter: Automatically builds the LeetCode URL when the titleSlug is set!
    public void setTitleSlug(String titleSlug) {
        this.titleSlug = titleSlug;
        this.questionLink = "https://leetcode.com/problems/" + titleSlug + "/";
    }
}