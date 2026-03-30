package com.tracker.leetcode.tracker.DTO;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ClassroomDashboardDTO {
    private String classroomId;
    private String className;
    private String mentorName;

    // Notice we reuse our existing StudentSummaryDTO here!
    private List<StudentSummaryDTO> enrolledStudents;
}