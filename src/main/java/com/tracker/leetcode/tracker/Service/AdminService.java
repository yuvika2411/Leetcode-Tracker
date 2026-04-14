package com.tracker.leetcode.tracker.Service;

import com.tracker.leetcode.tracker.DTO.ClassroomDashboardDTO;
import com.tracker.leetcode.tracker.DTO.MentorDTO;
import com.tracker.leetcode.tracker.DTO.SystemOverviewDTO;
import com.tracker.leetcode.tracker.Repository.ClassroomRepository;
import com.tracker.leetcode.tracker.Repository.MentorRepository;
import com.tracker.leetcode.tracker.Repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final StudentRepository studentRepository;
    private final MentorRepository mentorRepository;
    private final ClassroomRepository classroomRepository;
    private final MentorService mentorService;
    private final ClassroomService classroomService;

    public SystemOverviewDTO getSystemOverview(){
        log.info("Super Admin requested the master system overview.");

        try {
            long totalStudents = studentRepository.count();
            long totalMentors = mentorRepository.count();
            long totalClassrooms = classroomRepository.count();

            List<MentorDTO> mentorDTOS = mentorService.getAllMentors();
            List<ClassroomDashboardDTO> classroomDashboardDTOS = classroomRepository.findAll()
                    .stream()
                    .map(classroom -> classroomService.getClassroomDashboard(classroom.getId(), "name"))
                    .toList();

            return SystemOverviewDTO
                    .builder()
                    .totalStudents(totalStudents)
                    .totalMentors(totalMentors)
                    .totalClassrooms(totalClassrooms)
                    .allMentors(mentorDTOS)
                    .allClassrooms(classroomDashboardDTOS)
                    .build();
        } catch (Exception e) {
            log.error("Failed to generate system overview: {}", e.getMessage());
            throw new RuntimeException("Failed to generate system overview. Please try again later.");
        }
    }
}
