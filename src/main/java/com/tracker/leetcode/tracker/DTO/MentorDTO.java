package com.tracker.leetcode.tracker.DTO;

import lombok.Builder;
import lombok.Data;

import java.util.List;

//Git check

@Data
@Builder
public class MentorDTO {
    private String id;
    private String name;
    private String email;
    private List<String > classroomIds;
}
