package com.tracker.leetcode.tracker.Exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private LocalDateTime time;
    private int status;
    private String error;
    private String message;
}
