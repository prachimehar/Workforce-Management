package com.railse.hiring.workforcemgmt.service.impl;

import com.railse.hiring.workforcemgmt.common.exception.ResourceNotFoundException;
import com.railse.hiring.workforcemgmt.dto.*;
import com.railse.hiring.workforcemgmt.mapper.ITaskManagementMapper;
import com.railse.hiring.workforcemgmt.model.TaskManagement;
import com.railse.hiring.workforcemgmt.model.enums.Priority;
import com.railse.hiring.workforcemgmt.model.enums.Task;
import com.railse.hiring.workforcemgmt.model.enums.TaskStatus;
import com.railse.hiring.workforcemgmt.repository.TaskRepository;
import com.railse.hiring.workforcemgmt.service.TaskManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;


@Service
@Slf4j
public class TaskManagementServiceImpl implements TaskManagementService {


    private final TaskRepository taskRepository;
    private final ITaskManagementMapper taskMapper;

    private final Map<Long, List<TaskActivity>> activityStore = new HashMap<>();
    private final AtomicLong commentIdCounter = new AtomicLong(1000);
    private final AtomicLong activityIdCounter = new AtomicLong(2000);


    // Logging method
    private void logActivity(Long taskId, Long userId, String action) {
        TaskActivity activity = new TaskActivity();
        activity.setActivityId(activityIdCounter.incrementAndGet());
        activity.setTaskId(taskId);
        activity.setUserId(userId);
        activity.setAction(action);
        activity.setTimestamp(System.currentTimeMillis());

        activityStore.computeIfAbsent(taskId, k -> new ArrayList<>()).add(activity);
    }


    public TaskManagementServiceImpl(TaskRepository taskRepository, ITaskManagementMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
    }


    @Override
    public TaskManagementDto findTaskById(Long id) {
        TaskManagement task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return taskMapper.modelToDto(task);
    }

    @Override
    public List<TaskManagementDto> findTaskByAll() {
        List<TaskManagement> tasks = taskRepository.findAll();
        return taskMapper.modelListToDtoList(tasks); // ✅ correct mapping
    }

    @Override
    public List<TaskManagementDto> createTasks(TaskCreateRequest createRequest) {
        List<TaskManagement> createdTasks = new ArrayList<>();
        for (TaskCreateRequest.RequestItem item : createRequest.getRequests()) {
            TaskManagement newTask = new TaskManagement();
            newTask.setReferenceId(item.getReferenceId());
            newTask.setReferenceType(item.getReferenceType());
            newTask.setTask(item.getTask());
            newTask.setAssigneeId(item.getAssigneeId());
            newTask.setPriority(item.getPriority());
            newTask.setTaskDeadlineTime(item.getTaskDeadlineTime());
            newTask.setStatus(TaskStatus.ASSIGNED);
            newTask.setDescription("New task created.");
            createdTasks.add(taskRepository.save(newTask));
        }
        return taskMapper.modelListToDtoList(createdTasks);
    }


    @Override
    public List<TaskManagementDto> updateTasks(UpdateTaskRequest updateRequest) {
        List<TaskManagement> updatedTasks = new ArrayList<>();

        for (UpdateTaskRequest.RequestItem item : updateRequest.getRequests()) {
            if (item.getTaskId() == null) {
                throw new IllegalArgumentException("Task ID must not be null in request");
            }

            TaskManagement task = taskRepository.findById(item.getTaskId())
                    .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + item.getTaskId()));

            if (item.getTaskStatus() != null) {
                task.setStatus(item.getTaskStatus());
            }

            if (item.getDescription() != null) {
                task.setDescription(item.getDescription());
            }
            System.out.println("Updating task: " + task);
            System.out.println("Task ID before save: " + task.getId());
            updatedTasks.add(taskRepository.save(task));  // ID is already present
        }

        return taskMapper.modelListToDtoList(updatedTasks);
    }


    @Override
    public String assignByReference(AssignByReferenceRequest request) {
        List<Task> applicableTasks = Task.getTasksByReferenceType(request.getReferenceType());

        List<TaskManagement> existingTasks = taskRepository.findByReferenceIdAndReferenceType(
                request.getReferenceId(), request.getReferenceType()
        );

        for (Task taskType : applicableTasks) {

            List<TaskManagement> activeTasks = existingTasks.stream()
                    .filter(t -> t.getTask() == taskType &&
                            t.getStatus() != TaskStatus.COMPLETED &&
                            t.getStatus() != TaskStatus.CANCELLED)
                    .collect(Collectors.toList());

            Optional<TaskManagement> existingSameAssignee = activeTasks.stream()
                    .filter(t -> t.getAssigneeId().equals(request.getAssigneeId()) &&
                            t.getStatus() == TaskStatus.ASSIGNED)
                    .findFirst();

            if (existingSameAssignee.isPresent()) {
                // Task is already assigned to the same user and active — do nothing
                continue;
            }

            // Cancel all existing active tasks (different assignees or duplicate)
            for (TaskManagement task : activeTasks) {
                task.setStatus(TaskStatus.CANCELLED);
                taskRepository.save(task);
            }

            // Assign new task
            TaskManagement newTask = new TaskManagement();
            newTask.setReferenceId(request.getReferenceId());
            newTask.setReferenceType(request.getReferenceType());
            newTask.setTask(taskType);
            newTask.setAssigneeId(request.getAssigneeId());
            newTask.setStatus(TaskStatus.ASSIGNED);
            taskRepository.save(newTask);
        }

        return "Tasks assigned successfully for reference " + request.getReferenceId();
    }

    @Override
    public List<TaskManagementDto> fetchTasksByDate(TaskFetchByDateRequest request) {
        List<TaskManagement> tasks = taskRepository.findByAssigneeIdIn(request.getAssigneeIds());

        List<TaskManagement> filteredTasks = tasks.stream()
                .filter(task -> {
                    // Skip CANCELLED or COMPLETED
                    if (task.getStatus() == TaskStatus.CANCELLED || task.getStatus() == TaskStatus.COMPLETED) {
                        return false;
                    }

                    if (task.getTaskDeadlineTime() == null) {
                        return false;
                    }

                    long taskTime = task.getTaskDeadlineTime();

                    // Case 1: Task within range
                    boolean inRange = taskTime >= request.getStartDate() && taskTime <= request.getEndDate();

                    // Case 2: Task before range but still active
                    boolean beforeRangeAndActive = taskTime < request.getStartDate();

                    return inRange || beforeRangeAndActive;
                })
                .collect(Collectors.toList());

        return taskMapper.modelListToDtoList(filteredTasks);
    }

    @Override
    public String updateTaskPriority(UpdatePriorityRequest request) {
        TaskManagement task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + request.getTaskId()));

        task.setPriority(request.getNewPriority());
        taskRepository.save(task);
        return "Task priority updated successfully.";
    }

    @Override
    public List<TaskManagementDto> fetchTasksByPriority(Priority priority) {
        List<TaskManagement> tasks = taskRepository.findByPriority(priority);
        return taskMapper.modelListToDtoList(tasks);
    }

    public void addCommentToTask(Long taskId, AddCommentRequest request) {
        TaskComment comment = new TaskComment();
        comment.setCommentId(commentIdCounter.incrementAndGet());
        comment.setTaskId(taskId);
        comment.setUserId(request.getUserId());
        comment.setCommentText(request.getCommentText());
        comment.setTimestamp(System.currentTimeMillis());

        taskRepository.addComment(taskId, comment);

        TaskActivity activity = new TaskActivity();
        activity.setActivityId(activityIdCounter.incrementAndGet());
        activity.setTaskId(taskId);
        activity.setUserId(request.getUserId());
        activity.setAction("User " + request.getUserId() + " commented on the task");
        activity.setTimestamp(System.currentTimeMillis());

        taskRepository.logActivity(taskId, activity);
    }

    public TaskDetailsDto getTaskDetails(Long taskId) {
        TaskManagementDto task = taskMapper.modelToDto(taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found")));

        List<TaskComment> comments = taskRepository.getCommentsForTask(taskId);
        List<TaskActivity> activities = taskRepository.getActivityForTask(taskId);

        comments.sort(Comparator.comparing(TaskComment::getTimestamp));
        activities.sort(Comparator.comparing(TaskActivity::getTimestamp));

        TaskDetailsDto dto = new TaskDetailsDto();
        dto.setTask(task);
        dto.setComments(comments);
        dto.setActivityHistory(activities);

        return dto;
    }

    @Override
    public void addComment(Long taskId, AddCommentRequest request) {
        // Ensure task exists
        taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        // Create comment
        TaskComment comment = new TaskComment();
        comment.setCommentId(commentIdCounter.incrementAndGet());
        comment.setTaskId(taskId);
        comment.setUserId(request.getUserId());
        comment.setCommentText(request.getCommentText());
        comment.setTimestamp(System.currentTimeMillis());

        // Save comment
        taskRepository.saveComment(comment);

        // Log activity
        String action = "User " + request.getUserId() + " commented on the task";
        logActivity(taskId, request.getUserId(), action);

        // Save activity
        taskRepository.saveActivity(taskId, activityStore.get(taskId));
    }

}
