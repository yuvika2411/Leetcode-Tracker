package com.tracker.leetcode.tracker.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Badge {
    private String title;
    private String icon;
    private String timestamp; // LeetCode returns this as a string formatted date
}
