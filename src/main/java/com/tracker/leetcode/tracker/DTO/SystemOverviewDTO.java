package com.tracker.leetcode.tracker.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemOverviewDTO {
    private long totalStudents;
    private long totalMentors;
    private long totalClassrooms;
    private List<MentorDTO> allMentors;
    private List<ClassroomDashboardDTO> allClassrooms;
}
