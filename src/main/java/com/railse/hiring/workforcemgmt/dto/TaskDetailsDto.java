package com.railse.hiring.workforcemgmt.dto;

import lombok.Data;
import java.util.List;

@Data
public class TaskDetailsDto {
    private TaskManagementDto task;
    private List<TaskComment> comments;
    private List<TaskActivity> activityHistory;
}