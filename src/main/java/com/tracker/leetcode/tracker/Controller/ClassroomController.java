package com.tracker.leetcode.tracker.Controller;

import com.tracker.leetcode.tracker.DTO.ClassroomDashboardDTO;
import com.tracker.leetcode.tracker.DTO.SubmissionUrlRequest;
import com.tracker.leetcode.tracker.Models.Assignment;
import com.tracker.leetcode.tracker.Models.Classroom;
import com.tracker.leetcode.tracker.Models.Student;
import com.tracker.leetcode.tracker.Service.ClassroomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/classrooms")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ClassroomController {

    private final ClassroomService classroomService;

    // 1. Create a Classroom
    // URL Example: POST /api/classrooms?mentorId=65f1a2b...&className=Data%20Structures
    @PostMapping
    public ResponseEntity<Classroom> createClassroom(
            @RequestParam String mentorId,
            @RequestParam String className) {
        return ResponseEntity.ok(classroomService.createClassroom(mentorId, className));
    }

    // 2. Add Student to Classroom
    // URL Example: POST /api/classrooms/65f1a2c.../students/akshayur0404
    @PostMapping("/{classroomId}/students/{leetcodeUsername}")
    public ResponseEntity<Classroom> addStudentToClassroom(
            @PathVariable String classroomId,
            @PathVariable String leetcodeUsername) {
        return ResponseEntity.ok(classroomService.addStudentToClassroom(classroomId, leetcodeUsername));
    }

    // 3. Get the Classroom Dashboard (With Sorting)
    // URL Examples:
    // GET /api/classrooms/{id}/dashboard
    // GET /api/classrooms/{id}/dashboard?sortBy=consistency
    // GET /api/classrooms/{id}/dashboard?sortBy=rating
    // GET /api/classrooms/{id}/dashboard?sortBy=solved
    @GetMapping("/{classroomId}/dashboard")
    public ResponseEntity<ClassroomDashboardDTO> getClassroomDashboard(
            @PathVariable String classroomId,
            @RequestParam(required = false, defaultValue = "name") String sortBy) {

        return ResponseEntity.ok(classroomService.getClassroomDashboard(classroomId, sortBy));
    }

    // 4. Assign a Question to the Classroom
    // URL: POST /api/classrooms/{id}/assignments
    /* Body Example:
       {
           "titleSlug": "two-sum",
           "startTimestamp": 1711000000,
           "endTimestamp": 1711604800
       }
    */
    @PostMapping("/{classroomId}/assignments")
    public ResponseEntity<Classroom> assignQuestion(
            @PathVariable String classroomId,
            @RequestBody Assignment assignment) {
        return ResponseEntity.ok(classroomService.assignQuestionToClassroom(classroomId, assignment));
    }

    // 5. Manually Validate Assignment URL
    // URL: POST /api/classrooms/{classroomId}/students/{username}/assignments/{assignmentId}/validate
    /* Body: { "url": "https://leetcode.com/problems/two-sum/submissions/123456789/" } */
    @PostMapping("/{classroomId}/students/{username}/assignments/{assignmentId}/validate")
    public ResponseEntity<Student> validateSubmission(
            @PathVariable String classroomId,
            @PathVariable String username,
            @PathVariable String assignmentId,
            @RequestBody SubmissionUrlRequest request) {

        return ResponseEntity.ok(classroomService.validateManualSubmission(classroomId, username, assignmentId, request.getUrl()));
    }
}