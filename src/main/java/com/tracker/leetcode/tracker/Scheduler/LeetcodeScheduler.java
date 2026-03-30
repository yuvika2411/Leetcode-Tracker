package com.tracker.leetcode.tracker.Scheduler;

import com.tracker.leetcode.tracker.Models.Student;
import com.tracker.leetcode.tracker.Repository.StudentRepository;
import com.tracker.leetcode.tracker.Service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class LeetcodeScheduler {

    private final StudentService studentService;
    private final StudentRepository studentRepository;

    @Scheduled(cron = "0 55 23 * * ?")
//    @Scheduled(fixedRate = 30000) // Runs every 30,000 milliseconds
    public void updateAllStudentsDaily(){
        log.info("Starting daily LeetCode data fetch for all students...");
        List<Student> classroom = studentRepository.findAll();
        if (classroom.isEmpty()){
            System.out.println("No Student found in the database. Scheduler aborting.");
            return;
        }
        for (Student student : classroom){
            log.info("Fetching data for: {}" , student.getLeetcodeUsername());
            try {
                studentService.fetchAndUpdateStudentProgress(student.getLeetcodeUsername());
                Thread.sleep(2000);
            } catch (InterruptedException e){
                System.err.println("Scheduler Interrupted during sleep: " + e.getMessage());
            }
        }
        System.out.println("Daily LeetCode Fetch Completed Successfully!");
    }
}
