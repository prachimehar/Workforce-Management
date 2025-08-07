package com.railse.hiring.workforcemgmt.service;

import java.util.List;
import com.railse.hiring.workforcemgmt.dto.*;
import com.railse.hiring.workforcemgmt.model.enums.Priority;

public interface TaskManagementService {

    List<TaskManagementDto> findTaskByAll();
    List<TaskManagementDto> createTasks(TaskCreateRequest request);
    List<TaskManagementDto> updateTasks(UpdateTaskRequest request);
    String assignByReference(AssignByReferenceRequest request);
    List<TaskManagementDto> fetchTasksByDate(TaskFetchByDateRequest request);
    TaskManagementDto findTaskById(Long id);
    String updateTaskPriority(UpdatePriorityRequest request);
    List<TaskManagementDto> fetchTasksByPriority(Priority priority);
    void addComment(Long taskId, AddCommentRequest request);
    TaskDetailsDto getTaskDetails(Long taskId);

}
