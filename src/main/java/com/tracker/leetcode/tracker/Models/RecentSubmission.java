package com.tracker.leetcode.tracker.Models;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RecentSubmission {
    private String title;
    private String titleSlug;
    private long timestamp;

    // NEW: The auto-generated clickable URL
    private String questionLink;

    // Custom constructor: We use this in the LeetCodeApiClient!
    public RecentSubmission(String title, String titleSlug, long timestamp) {
        this.title = title;
        this.titleSlug = titleSlug;
        this.timestamp = timestamp;
        // Automatically build the URL the moment the object is created
        this.questionLink = "https://leetcode.com/problems/" + titleSlug + "/";
    }

    // Custom setter: Just in case Spring Data MongoDB or Jackson tries to set it manually
    public void setTitleSlug(String titleSlug) {
        this.titleSlug = titleSlug;
        this.questionLink = "https://leetcode.com/problems/" + titleSlug + "/";
    }
}