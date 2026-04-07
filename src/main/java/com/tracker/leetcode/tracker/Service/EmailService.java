package com.tracker.leetcode.tracker.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Async // Send emails in the background so the mentor's dashboard doesn't freeze!
    public void sendNudgeEmail(String toEmail, String studentName, String assignmentName, String className) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Reminder: Pending LeetCode Assignment for " + className);
            message.setText("Hi " + studentName + ",\n\n" +
                    "This is a gentle reminder from your mentor that you have a pending assignment: '" + assignmentName + "'.\n\n" +
                    "Please complete it on LeetCode and validate it on your LeetTracker dashboard as soon as possible.\n\n" +
                    "Happy Coding!");

            mailSender.send(message);
            log.info("Nudge email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send email.");
        }
    }
}