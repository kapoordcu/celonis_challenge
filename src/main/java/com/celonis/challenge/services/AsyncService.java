package com.celonis.challenge.services;

import com.celonis.challenge.model.ProjectGenerationTask;
import com.celonis.challenge.model.TaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

@Service
public class AsyncService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncService.class);

    @Value("${celonis.time.increment.frequency}")
    private int timeIncrementFrequency;

    @Async("asyncExecutor")
    public CompletableFuture<ProjectGenerationTask> triggerTaskExecution(ProjectGenerationTask generationTask) throws InterruptedException {
        LOGGER.info("Task submitted for execution with uuid '{}' started with x= {} and y= {} ",
                generationTask.getId(), generationTask.getX(), generationTask.getY());
        generationTask.setTaskStatus(TaskStatus.IN_EXECUTION);
        while (generationTask.getX() != generationTask.getY()) {
            new Timer().scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    generationTask.setX(generationTask.getX() + 1);
                }
            }, 0, timeIncrementFrequency);
        }
        return CompletableFuture.completedFuture(generationTask);
    }

}
