package com.celonis.challenge.controllers;

import com.celonis.challenge.model.ProjectGenerationTask;
import com.celonis.challenge.model.TaskStatus;
import com.celonis.challenge.services.FileService;
import com.celonis.challenge.services.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskController.class);
    private final TaskService taskService;
    private final FileService fileService;

    @Autowired
    public TaskController(TaskService taskService,
                          FileService fileService) {
        this.taskService = taskService;
        this.fileService = fileService;
    }

    @GetMapping("/")
    public List<ProjectGenerationTask> listTasks() {
        return taskService.listTasks();
    }

    @PostMapping("/")
    public ProjectGenerationTask createTask(@RequestBody @Valid ProjectGenerationTask projectGenerationTask) {
        return taskService.createTask(projectGenerationTask);
    }

    @GetMapping("/{taskId}")
    public ProjectGenerationTask getTask(@PathVariable String taskId) {
        return taskService.getTask(taskId);
    }

    @PutMapping("/{taskId}")
    public ProjectGenerationTask updateTask(@PathVariable String taskId,
                                            @RequestBody @Valid ProjectGenerationTask projectGenerationTask) {
        return taskService.update(taskId, projectGenerationTask);
    }

    @DeleteMapping("/{taskId}/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable String taskId) {
        taskService.delete(taskId);
    }

    @PostMapping("/{taskId}/execute")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void executeTask(@PathVariable String taskId) {
        taskService.executeTask(taskId);
    }

    @PostMapping("/{taskId}/triggerExecution")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void triggerTaskExecution(@PathVariable String taskId) {
        try {
            CompletableFuture<ProjectGenerationTask> projectGenerationTaskCompletableFuture = taskService.triggerTaskExecution(taskId);
            CompletableFuture.allOf(projectGenerationTaskCompletableFuture).join();
            projectGenerationTaskCompletableFuture.get().setTaskStatus(TaskStatus.COMPLETED);
            taskService.update(taskId, projectGenerationTaskCompletableFuture.get());
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("The task with uuid '{}' was interrupted", taskId);
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping("/{taskId}/cancelTask")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelTask(@PathVariable String taskId) {
        taskService.cancel(taskId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @GetMapping("/{taskId}/executionStatus")
    public ProjectGenerationTask getExecutionStatus(@PathVariable String taskId) {
        return taskService.getExecutionStatus(taskId);
    }

    @GetMapping("/{taskId}/result")
    public ResponseEntity<FileSystemResource> getResult(@PathVariable String taskId) {
        return fileService.getTaskResult(taskId);
    }

}
