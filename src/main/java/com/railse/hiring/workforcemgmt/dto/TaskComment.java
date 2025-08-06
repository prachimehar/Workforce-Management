package com.railse.hiring.workforcemgmt.dto;

import lombok.Data;

@Data
public class TaskComment {
    private Long commentId;
    private Long taskId;
    private Long userId;
    private String commentText;
    private Long timestamp;
}