package com.tracker.leetcode.tracker.DTO;
import lombok.Builder;
@Builder
public record AuthenticationResponse(String accessToken, String mentorId, String name, String  refreshToken) {}