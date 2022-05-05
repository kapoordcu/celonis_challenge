package com.celonis.challenge.controllers;

import com.celonis.challenge.model.ProjectGenerationTask;
import com.celonis.challenge.services.FileService;
import com.celonis.challenge.services.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping
public class TaskController {
    private final TaskService taskService;
    private final FileService fileService;

    @Autowired
    public TaskController(TaskService taskService,
                          FileService fileService) {
        this.taskService = taskService;
        this.fileService = fileService;
    }

    @GetMapping("/v1/api/tasks")
    public List<ProjectGenerationTask> listTasks() {
        return taskService.listTasks();
    }

    @PostMapping("/v1/api/tasks")
    public ProjectGenerationTask createTask(@RequestBody @Valid ProjectGenerationTask projectGenerationTask) {
        return taskService.createTask(projectGenerationTask);
    }

    @GetMapping("/v1/api/tasks/{taskId}")
    public ProjectGenerationTask getTask(@PathVariable String taskId) {
        return taskService.getTask(taskId);
    }

    @PutMapping("/v1/api/tasks/{taskId}")
    public ProjectGenerationTask updateTask(@PathVariable String taskId,
                                            @RequestBody @Valid ProjectGenerationTask projectGenerationTask) {
        return taskService.update(taskId, projectGenerationTask);
    }

    @DeleteMapping("/v1/api/tasks/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable String taskId) {
        taskService.delete(taskId);
    }

    @GetMapping("/v1/api/tasks/result/{taskId}")
    public ResponseEntity<FileSystemResource> getResult(@PathVariable String taskId) {
        return fileService.getTaskResult(taskId);
    }

    @PostMapping("/v1/api/tasks/execute/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void executeTask(@PathVariable String taskId) {
        taskService.executeTask(taskId);
    }

    @PostMapping("/v2/api/tasks/execute/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void triggerExecution(@PathVariable String taskId) {
        taskService.triggerTaskExecution(taskId);
    }

    @PostMapping("/v2/api/tasks/cancel/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelTaskExecution(@PathVariable String taskId) {
        taskService.cancelTaskExecution(taskId);
    }

    @GetMapping("/v2/api/tasks/status/{taskId}")
    public ProjectGenerationTask getExecutionStatus(@PathVariable String taskId) {
        return taskService.getExecutionStatus(taskId);
    }
}
