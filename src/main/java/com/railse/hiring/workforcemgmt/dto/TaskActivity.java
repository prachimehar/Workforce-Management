package com.railse.hiring.workforcemgmt.dto;

import lombok.Data;

@Data
public class TaskActivity {
    private Long activityId;
    private Long taskId;
    private String action;
    private Long userId;
    private Long timestamp;
}