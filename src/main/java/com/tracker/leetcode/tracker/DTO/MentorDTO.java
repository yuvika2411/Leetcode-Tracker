package com.tracker.leetcode.tracker.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

//Git check

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MentorDTO {
    private String id;
    private String name;
    private String email;
    private List<String > classroomIds;
}
