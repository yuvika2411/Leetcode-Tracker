package com.tracker.leetcode.tracker.Service;

import com.tracker.leetcode.tracker.DTO.ClassroomDashboardDTO;
import com.tracker.leetcode.tracker.DTO.StudentSummaryDTO;
import com.tracker.leetcode.tracker.Exception.*;
import com.tracker.leetcode.tracker.Mapper.StudentMapper;
import com.tracker.leetcode.tracker.Models.Assignment;
import com.tracker.leetcode.tracker.Models.Classroom;
import com.tracker.leetcode.tracker.Models.Mentor;
import com.tracker.leetcode.tracker.Models.Student;
import com.tracker.leetcode.tracker.Repository.ClassroomRepository;
import com.tracker.leetcode.tracker.Repository.MentorRepository;
import com.tracker.leetcode.tracker.Repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final MentorRepository mentorRepository;
    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;
    private final LeetCodeApiClient leetCodeApiClient;

    // 1. Create Classroom
    public Classroom createClassroom(String mentorId, String className) {
        log.info("Creating classroom {} for mentor ID: {}", className, mentorId);
        Mentor mentor = mentorRepository.findById(mentorId)
                .orElseThrow(() -> new MentorNotFoundException("Mentor not found with ID: " + mentorId));

        Classroom classroom = new Classroom();
        classroom.setClassName(className);
        classroom.setMentorId(mentorId);
        Classroom savedClassroom = classroomRepository.save(classroom);

        mentor.getClassroomIds().add(savedClassroom.getId());
        mentorRepository.save(mentor);

        return savedClassroom;
    }

    // 2. Add Student
    public Classroom addStudentToClassroom(String classroomId, String leetcodeUsername) {
        log.info("Adding student {} to classroom ID: {}", leetcodeUsername, classroomId);
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ClassroomNotFoundException("Classroom not found with ID: " + classroomId));

        Student student = studentRepository.findByLeetcodeUsername(leetcodeUsername)
                .orElseThrow(() -> new StudentNotFoundException("Student not found in database. Please add them first."));

        // Prevent duplicate enrollments in the same class
        if (classroom.getStudentIds().contains(student.getId())) {
            throw new StudentAlreadyEnrolledException("Student is already enrolled in this classroom.");
        }

        classroom.getStudentIds().add(student.getId());
        return classroomRepository.save(classroom);
    }

    // 3. Get Dashboard (with Sorting & Fetching restored!)
    public ClassroomDashboardDTO getClassroomDashboard(String classroomId, String sortBy) {
        log.info("Fetching dashboard data for classroom ID: {} sorted by: {}", classroomId, sortBy);

        // 1. Fetch the data we need from MongoDB
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ClassroomNotFoundException("Classroom not found with ID: " + classroomId));

        Mentor mentor = mentorRepository.findById(classroom.getMentorId())
                .orElseThrow(() -> new MentorNotFoundException("Mentor not found for this classroom."));

        List<Student> enrolledStudents = studentRepository.findAllById(classroom.getStudentIds());

        // 2. Map to DTOs and pass the classroom assignments for evaluation
        List<StudentSummaryDTO> studentSummaries = enrolledStudents.stream()
                .map(student -> studentMapper.toSummaryDTO(student, classroom.getAssignments()))
                .collect(Collectors.toList());

        // 3. Apply Dynamic Sorting
        if (sortBy != null && !sortBy.isBlank()) {
            switch (sortBy.toLowerCase()) {
                case "consistency":
                    studentSummaries.sort((s1, s2) -> Integer.compare(s2.getConsistencyStreak(), s1.getConsistencyStreak()));
                    break;
                case "rating":
                    studentSummaries.sort((s1, s2) -> Double.compare(s2.getCurrentContestRating(), s1.getCurrentContestRating()));
                    break;
                case "solved":
                    studentSummaries.sort((s1, s2) -> Integer.compare(s2.getTotalSolved(), s1.getTotalSolved()));
                    break;
                case "pending": // Sort by students who are falling behind!
                    studentSummaries.sort((s1, s2) -> Integer.compare(s2.getPendingAssignments(), s1.getPendingAssignments()));
                    break;
                case "completed": // Sort by students who finished the most assignments!
                    studentSummaries.sort((s1, s2) -> Integer.compare(s2.getCompletedAssignments(), s1.getCompletedAssignments()));
                    break;
                case "name":
                    studentSummaries.sort((s1, s2) -> s1.getName().compareToIgnoreCase(s2.getName()));
                    break;
                default:
                    log.warn("Unknown sort parameter: {}. Defaulting to unsorted.", sortBy);
            }
        }

        // 4. Return the fully built Dashboard
        return ClassroomDashboardDTO.builder()
                .classroomId(classroom.getId())
                .className(classroom.getClassName())
                .mentorName(mentor.getName())
                .enrolledStudents(studentSummaries)
                .build();
    }

    // 4. Assign a LeetCode problem to the classroom
    public Classroom assignQuestionToClassroom(String classroomId, Assignment assignment) {
        log.info("Assigning question '{}' to classroom ID: {}", assignment.getTitleSlug(), classroomId);

        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ClassroomNotFoundException("Classroom not found with ID: " + classroomId));

        // --- THE FIX: Ensure ID and questionLink are explicitly set before saving ---
        if (assignment.getId() == null) {
            assignment.setId(java.util.UUID.randomUUID().toString());
        }

        if (assignment.getQuestionLink() == null && assignment.getTitleSlug() != null) {
            assignment.setQuestionLink("https://leetcode.com/problems/" + assignment.getTitleSlug() + "/");
        }

        // Defensive programming: just in case assignments array is null in the DB
        if (classroom.getAssignments() == null) {
            classroom.setAssignments(new ArrayList<>());
        }

        classroom.getAssignments().add(assignment);
        return classroomRepository.save(classroom);
    }

    public Student validateManualSubmission(String classroomId,String leetcodeUsername,String assignmentId,String submissionUrl){

        log.info("Validating manual submission for {} on assignment {}", leetcodeUsername, assignmentId);

        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ClassroomNotFoundException("Classroom not found"));

        Assignment assignment = classroom.getAssignments().stream()
                .filter(a -> a.getId().equals(assignmentId))
                .findFirst()
                .orElseThrow(() -> new AssignmentNotFoundException("Assignment not found in this classroom."));


        Student student = studentRepository.findByLeetcodeUsername(leetcodeUsername)
                .orElseThrow(() -> new StudentNotFoundException("Student not found."));

        // If they already validated it, skip the network call and return
        if (student.getManuallyCompletedAssignments().contains(assignmentId)) {
            return student;
        }

        Pattern pattern = Pattern.compile("submissions/(?:detail/)?(\\d+)");
        Matcher matcher = pattern.matcher(submissionUrl);
        if (!matcher.find()){
            throw  new ValidationFailedException("Invalid LeetCode URL. It must contain the submission ID.");
        }

        String submissionId = matcher.group(1);

        boolean isValid = leetCodeApiClient.verifySubmission(submissionId,leetcodeUsername,assignment.getTitleSlug());

        if (!isValid) {
            throw new ValidationFailedException("Validation Failed! Ensure the submission is 'Accepted', belongs to you, and is the correct problem.");
        }

        // 5. Success! Permanently save it to the student's profile
        student.getManuallyCompletedAssignments().add(assignmentId);
        return studentRepository.save(student);

    }

    public Student validateSubmissionAsStudent(Student student,String classroomId,String assignmentId,String submissionUrl){

        log.info("Student {} is self-validating assignment {}",student.getLeetcodeUsername(),assignmentId);

        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ClassroomNotFoundException("Classroom not Found."));
        if (!classroom.getStudentIds().contains(student.getId())){
            throw new RuntimeException("Security Exception: You are not enrolled in this classroom.");
        }

        Assignment assignment = classroom.getAssignments().stream()
                .filter(a -> a.getId().equals(assignmentId))
                .findFirst()
                .orElseThrow(() -> new AssignmentNotFoundException("Assignment not found in this classroom."));


        // If they already validated it, skip the network call
        if (student.getManuallyCompletedAssignments().contains(assignmentId)){
            return student;
        }

        // 3. Extract the Submission ID from the URL using Regex
        Pattern pattern = Pattern.compile("submissions/(?:detail/)?(\\d+)");
        Matcher matcher = pattern.matcher(submissionUrl);

        if (!matcher.find()) {
            throw new ValidationFailedException("Invalid LeetCode URL. It must contain the submission ID.");
        }

        String submissionId = matcher.group(1);

        // 4. Verify with LeetCode API (using the student's exact username)
        boolean isValid = leetCodeApiClient.verifySubmission(submissionId, student.getLeetcodeUsername(), assignment.getTitleSlug());

        if (!isValid) {
            throw new ValidationFailedException("Validation Failed! Ensure the submission is 'Accepted', belongs to you, and is the correct problem.");
        }

        // 5. Success! Save it to the student's profile
        student.getManuallyCompletedAssignments().add(assignmentId);
        return studentRepository.save(student);

    }
}