package com.railse.hiring.workforcemgmt.controller;

import com.railse.hiring.workforcemgmt.common.model.response.Response;
import com.railse.hiring.workforcemgmt.dto.*;
import com.railse.hiring.workforcemgmt.model.enums.Priority;
import com.railse.hiring.workforcemgmt.service.TaskManagementService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;


@RestController
@RequestMapping("/task-mgmt")
public class TaskManagementController {

    private final TaskManagementService taskManagementService;

    private final AtomicLong commentIdCounter = new AtomicLong(1000);
    private final AtomicLong activityIdCounter = new AtomicLong(2000);

    public TaskManagementController(TaskManagementService taskManagementService) {
        this.taskManagementService = taskManagementService;
    }

    @GetMapping("/{id}")
    public Response<TaskManagementDto> getTaskById(@PathVariable Long id) {
        return new Response<>(taskManagementService.findTaskById(id));
    }

    @GetMapping("/all")
    public Response<List<TaskManagementDto>> getTaskByAll() {
        return new Response<>(taskManagementService.findTaskByAll());
    }

    @PostMapping("/create")
    public Response<List<TaskManagementDto>> createTasks(@RequestBody TaskCreateRequest request) {
        return new Response<>(taskManagementService.createTasks(request));
    }

    @PostMapping("/update")
    public Response<List<TaskManagementDto>> updateTasks(@RequestBody UpdateTaskRequest request) {
        return new Response<>(taskManagementService.updateTasks(request));
    }

    @PostMapping("/assign-by-ref")
    public Response<String> assignByReference(@RequestBody AssignByReferenceRequest request) {
        return new Response<>(taskManagementService.assignByReference(request));
    }

    @PostMapping("/fetch-by-date/v2")
    public Response<List<TaskManagementDto>> fetchByDate(@RequestBody TaskFetchByDateRequest request) {
        return new Response<>(taskManagementService.fetchTasksByDate(request));
    }

    @RequestMapping(value = "/priority", method = RequestMethod.POST)
    public Response<String> updatePriority(@RequestBody UpdatePriorityRequest request) {
        String result = taskManagementService.updateTaskPriority(request);
        return new Response<>(result);
    }

    @GetMapping("/priority/{priority}")
    public Response<List<TaskManagementDto>> getTasksByPriority(@PathVariable Priority priority) {
        List<TaskManagementDto> tasks = taskManagementService.fetchTasksByPriority(priority);
        return new Response<>(tasks);
    }

    @PostMapping("/task/{taskId}/comments")
    public Response<String> addComment(@PathVariable Long taskId, @RequestBody AddCommentRequest request) {
        taskManagementService.addComment(taskId, request);
        return new Response<>("Comment added successfully.");
    }

    @GetMapping("/task/{taskId}")
    public Response<TaskDetailsDto> getTaskDetails(@PathVariable Long taskId) {
        TaskDetailsDto dto = taskManagementService.getTaskDetails(taskId);
        return new Response<>(dto);
    }
}
