package com.celonis.challenge.services;

import com.celonis.challenge.exceptions.InternalException;
import com.celonis.challenge.exceptions.NotFoundException;
import com.celonis.challenge.model.ProjectGenerationTask;
import com.celonis.challenge.model.ProjectGenerationTaskRepository;
import com.celonis.challenge.model.TaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

@Service
public class TaskService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskService.class);
    @Autowired
    private AsyncService asyncService;
    private Map<String, CompletableFuture<ProjectGenerationTask>> completableFutureMap = new ConcurrentHashMap<>();

    @Autowired
    private ProjectGenerationTaskRepository projectGenerationTaskRepository;
    @Autowired
    private FileService fileService;

    public List<ProjectGenerationTask> listTasks() {
        return projectGenerationTaskRepository.findAll();
    }

    public ProjectGenerationTask createTask(ProjectGenerationTask projectGenerationTask) {
        projectGenerationTask.setId(null);
        projectGenerationTask.setCreationDate(new Date());
        return projectGenerationTaskRepository.save(projectGenerationTask);
    }

    public ProjectGenerationTask getTask(String taskId) {
        return get(taskId);
    }

    public ProjectGenerationTask update(String taskId, ProjectGenerationTask projectGenerationTask) {
        ProjectGenerationTask existing = get(taskId);
        existing.setCreationDate(projectGenerationTask.getCreationDate());
        existing.setName(projectGenerationTask.getName());
        existing.setX(projectGenerationTask.getX());
        existing.setY(projectGenerationTask.getY());
        existing.setTaskStatus(projectGenerationTask.getTaskStatus());
        return projectGenerationTaskRepository.save(existing);
    }

    public void delete(String taskId) {
        projectGenerationTaskRepository.deleteById(taskId);
    }

    public void executeTask(String taskId) {
        URL url = Thread.currentThread().getContextClassLoader().getResource("challenge.zip");
        if (url == null) {
            throw new InternalException("Zip file not found");
        }
        try {
            fileService.storeResult(taskId, url);
        } catch (Exception e) {
            throw new InternalException(e);
        }
    }

    private ProjectGenerationTask get(String taskId) {
        Optional<ProjectGenerationTask> projectGenerationTask = projectGenerationTaskRepository.findById(taskId);
        return projectGenerationTask.orElseThrow(NotFoundException::new);
    }

    public CompletableFuture<ProjectGenerationTask> triggerTaskExecution(String taskId) {
        try {
            ProjectGenerationTask task = get(taskId);
            LOGGER.info("Task submitted for execution with uuid '{}' started with x= {} and y= {} ",
                    task.getId(), task.getX(), task.getY());
            saveExecutionStatusInDB(task);
            CompletableFuture<ProjectGenerationTask> taskCompletableFuture = asyncService.triggerTaskExecution(task);
            completableFutureMap.putIfAbsent(taskId, taskCompletableFuture);
            Optional.ofNullable(taskCompletableFuture.get())
                    .ifPresentOrElse(taskGet -> projectGenerationTaskRepository.save(taskGet), NotFoundException::new);
            return taskCompletableFuture;
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Task Execution API error: The task with uuid '{}' was interrupted", taskId);
            throw new RuntimeException(e);
        }
    }

    private void saveExecutionStatusInDB(ProjectGenerationTask generationTask) {
        generationTask.setTaskStatus(TaskStatus.IN_EXECUTION);
        projectGenerationTaskRepository.save(generationTask);
    }

    public ProjectGenerationTask getExecutionStatus(String taskId) {
        ProjectGenerationTask enquiredTask = null;
        try {
            if(completableFutureMap.containsKey(taskId)) {
                LOGGER.info("Running Task status is requested with uuid '{}' ", taskId);
                CompletableFuture<ProjectGenerationTask> submittedThreadpoolTask = completableFutureMap.get(taskId);
                enquiredTask = submittedThreadpoolTask.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Task ExecutionStatus API error: The task with uuid '{}' was interrupted", taskId);
            throw new RuntimeException(e);
        }
        return enquiredTask;
    }

    public void cancelTaskExecution(String taskId) {
        try {
            if(completableFutureMap.containsKey(taskId)) {
                LOGGER.info("Task requested to be cancelled uuid '{}' ", taskId);
                CompletableFuture<ProjectGenerationTask> submittedTask = completableFutureMap.get(taskId);
                submittedTask.cancel(true);
                Optional.ofNullable(submittedTask.get()).ifPresentOrElse(task -> {
                    task.setTaskStatus(TaskStatus.CANCELLED);
                    update(taskId, task);
                }, NotFoundException::new);
            }
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Task Cancel API error: The task with uuid '{}' could not be cancelled", taskId);
            throw new RuntimeException(e);
        }
    }
}
