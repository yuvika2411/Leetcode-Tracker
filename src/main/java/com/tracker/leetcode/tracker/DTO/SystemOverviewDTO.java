package com.tracker.leetcode.tracker.DTO;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SystemOverviewDTO {
    private long totalStudents;
    private long totalMentors;
    private long totalClassrooms;
    private List<MentorDTO> allMentors;
    private List<ClassroomDashboardDTO> allClassrooms;
}
