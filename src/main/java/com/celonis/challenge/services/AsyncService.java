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
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class AsyncService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncService.class);

    @Value("${celonis.time.increment.frequency}")
    private int timeIncrementFrequency;

    @Async("asyncExecutor")
    public CompletableFuture<ProjectGenerationTask> triggerTaskExecution(ProjectGenerationTask generationTask) throws InterruptedException {
        LOGGER.info("Task submitted for execution with uuid '{}' started with x= {} and y= {} ",
                generationTask.getId(), generationTask.getX(), generationTask.getY());
        AtomicInteger variableX = new AtomicInteger(generationTask.getX());
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            int counter = variableX.get();
            @Override
            public void run() {
                generationTask.setX(variableX.getAndIncrement());
                counter++;
                if (counter >= (generationTask.getY())- variableX.get()){
                    generationTask.setX(generationTask.getY());
                    generationTask.setTaskStatus(TaskStatus.COMPLETED);
                    timer.cancel();
                }
            }
        }, 0, timeIncrementFrequency);
        return CompletableFuture.completedFuture(generationTask);
    }

}
