package com.tracker.leetcode.tracker.Service;

import com.tracker.leetcode.tracker.Exception.LeetCodeApiException;
import com.tracker.leetcode.tracker.Exception.StudentNotFoundException;
import com.tracker.leetcode.tracker.Models.DailyProgress;
import com.tracker.leetcode.tracker.Models.ProblemStats;
import com.tracker.leetcode.tracker.Models.Student;
import com.tracker.leetcode.tracker.Repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeetCodeService {

    private final StudentRepository studentRepository;
    private final String LEETCODE_API_URL = "https://leetcode.com/graphql";

    // 1. Heatmap Data Fetch (Now returns Student and handles exceptions)
    public Student fetchAndUpdateStudentProgress(String leetcodeUsername) {
        log.info("Starting calendar heatmap fetch for user: {}", leetcodeUsername);

        // Fail Fast using custom exception
        Student student = studentRepository.findByLeetcodeUsername(leetcodeUsername)
                .orElseThrow(() -> new StudentNotFoundException("Student '" + leetcodeUsername + "' not found in database."));

        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();

        String query = """
                {"query":"query userProfileCalendar($username: String!) { matchedUser(username: $username) { userCalendar { submissionCalendar } } }","variables":{"username":"%s"}}
                """.formatted(leetcodeUsername);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(query, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(LEETCODE_API_URL, request, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());

            // Fixed the JSON path to correctly include .path("userCalendar")
            JsonNode calendarNode = root.path("data").path("matchedUser").path("userCalendar").path("submissionCalendar");

            if (calendarNode.isMissingNode() || calendarNode.isNull()) {
                throw new LeetCodeApiException("LeetCode returned no calendar data. Is the username correct?");
            }

            // Using standard Jackson .asText()
            String calendarJsonString = calendarNode.asString();
            Map<String, Integer> submissionMap = objectMapper.readValue(calendarJsonString, new TypeReference<Map<String, Integer>>() {});

            List<DailyProgress> progressList = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : submissionMap.entrySet()) {
                long unixTimeStamp = Long.parseLong(entry.getKey());
                int questionsSolvedCount = entry.getValue();

                LocalDate date = Instant.ofEpochSecond(unixTimeStamp)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                progressList.add(new DailyProgress(date, questionsSolvedCount));
            }

            student.setProgressHistory(progressList);
            Student savedStudent = studentRepository.save(student);

            log.info("Successfully updated calendar heatmap for: {}", leetcodeUsername);
            return savedStudent;

        } catch (LeetCodeApiException e) {
            throw e; // Rethrow custom API exceptions to the Global Handler
        } catch (Exception e) {
            log.error("Failed to parse LeetCode Calendar response for user {}: {}", leetcodeUsername, e.getMessage());
            throw new LeetCodeApiException("Error communicating with LeetCode API for calendar data.");
        }
    }


    // 2. Problem Stats Fetch (Refactored for custom exceptions and logging)
    public Student fetchAndUpdateProblemStats(String leetcodeUsername) {
        log.info("Starting problem stats fetch for user: {}", leetcodeUsername);

        Student student = studentRepository.findByLeetcodeUsername(leetcodeUsername)
                .orElseThrow(() -> new StudentNotFoundException("Student '" + leetcodeUsername + "' not found in database. Please add them first!"));

        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();

        String query = """
                {"query":"query userProblemsSolved($username: String!) { allQuestionsCount { difficulty count } matchedUser(username: $username) { problemsSolvedBeatsStats { difficulty percentage } submitStatsGlobal { acSubmissionNum { difficulty count } } } }","variables":{"username":"%s"}}
                """.formatted(leetcodeUsername);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(query, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(LEETCODE_API_URL, request, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode matchedUser = root.path("data").path("matchedUser");

            if (matchedUser.isMissingNode() || matchedUser.isNull()) {
                throw new LeetCodeApiException("LeetCode returned no stats data. Is the username correct?");
            }

            JsonNode submissionStats = matchedUser.path("submitStatsGlobal").path("acSubmissionNum");
            JsonNode beatsStats = matchedUser.path("problemsSolvedBeatsStats");

            List<ProblemStats> statsList = new ArrayList<>();

            if (submissionStats.isArray()) {
                for (JsonNode statNode : submissionStats) {
                    String difficulty = statNode.path("difficulty").asString(); // Changed to .asText()
                    int count = statNode.path("count").asInt();
                    double percentage = 0.0;

                    if (beatsStats.isArray()) {
                        for (JsonNode beatNode : beatsStats) {
                            if (beatNode.path("difficulty").asString().equals(difficulty)) {
                                if (!beatNode.path("percentage").isNull()) {
                                    percentage = beatNode.path("percentage").asDouble();
                                }
                                break;
                            }
                        }
                    }
                    statsList.add(new ProblemStats(difficulty, count, percentage));
                }
            }

            student.setProblemStats(statsList);
            Student savedStudent = studentRepository.save(student);

            log.info("Successfully updated problem stats for: {}", leetcodeUsername);
            return savedStudent;

        } catch (LeetCodeApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse LeetCode Stats API response for user {}: {}", leetcodeUsername, e.getMessage());
            throw new LeetCodeApiException("Error communicating with LeetCode API for stats data.");
        }
    }
}