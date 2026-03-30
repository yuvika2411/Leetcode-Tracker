package com.tracker.leetcode.tracker.Controller;

import com.tracker.leetcode.tracker.DTO.SystemOverviewDTO;
import com.tracker.leetcode.tracker.Service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/overview")
    public ResponseEntity<SystemOverviewDTO> getSystemOverview(){
        return ResponseEntity.ok(adminService.getSystemOverview());
    }
}
