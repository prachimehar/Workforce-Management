package com.railse.hiring.workforcemgmt.repository;


import com.railse.hiring.workforcemgmt.dto.TaskActivity;
import com.railse.hiring.workforcemgmt.dto.TaskComment;
import com.railse.hiring.workforcemgmt.model.TaskManagement;
import com.railse.hiring.workforcemgmt.model.enums.Priority;
import com.railse.hiring.workforcemgmt.model.enums.TaskStatus;


import java.util.List;
import java.util.Optional;


public interface TaskRepository {
    Optional<TaskManagement> findById(Long id);
    TaskManagement save(TaskManagement task);
    List<TaskManagement> findAll();
    List<TaskManagement> findByReferenceIdAndReferenceType(Long referenceId, com.railse.hiring.workforcemgmt.common.model.enums.ReferenceType referenceType);
    List<TaskManagement> findByAssigneeIdIn(List<Long> assigneeIds);

    List<TaskManagement> findByPriority(Priority priority);
    void addComment(Long taskId, TaskComment comment);

    void logActivity(Long taskId, TaskActivity activity);

    void saveComment(TaskComment comment);

    void saveActivity(Long taskId, List<TaskActivity> activities);

    List<TaskComment> getCommentsForTask(Long taskId);

    List<TaskActivity> getActivityForTask(Long taskId);
}
