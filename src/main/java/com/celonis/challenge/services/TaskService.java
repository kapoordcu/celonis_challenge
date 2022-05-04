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
            ProjectGenerationTask generationTask = get(taskId);
            generationTask.setTaskStatus(TaskStatus.IN_EXECUTION);
            projectGenerationTaskRepository.save(generationTask);

            CompletableFuture<ProjectGenerationTask> projectGenerationTaskCompletableFuture = asyncService.triggerTaskExecution(generationTask);
            completableFutureMap.putIfAbsent(taskId, projectGenerationTaskCompletableFuture);
            try {
                if(projectGenerationTaskCompletableFuture.get() != null) {
                    projectGenerationTaskRepository.save(projectGenerationTaskCompletableFuture.get());
                }
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
            return projectGenerationTaskCompletableFuture;
        } catch (InterruptedException e) {
            LOGGER.error("The task with uuid '{}' was interrupted", taskId);
            throw new RuntimeException(e);
        }
    }

    public ProjectGenerationTask getExecutionStatus(String taskId) {
        ProjectGenerationTask inquiredTask = null;
        try {
            if(completableFutureMap.containsKey(taskId)) {
                CompletableFuture<ProjectGenerationTask> submittedThreadpoolTask = completableFutureMap.get(taskId);
                inquiredTask = submittedThreadpoolTask.get();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        return inquiredTask;
    }

    public void cancel(String taskId) {
        try {
            if(completableFutureMap.containsKey(taskId)) {
                CompletableFuture<ProjectGenerationTask> submittedThreadpoolTask = completableFutureMap.get(taskId);
                submittedThreadpoolTask.cancel(true);
                ProjectGenerationTask cancellableTask = submittedThreadpoolTask.get();
                cancellableTask.setTaskStatus(TaskStatus.CANCELLED);
                update(taskId, cancellableTask);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

    }
}
