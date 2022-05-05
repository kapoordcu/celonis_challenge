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

    @Value("${celonis.time.increment.frequency}")
    private int timeIncrementFrequency;

    @Async("asyncExecutor")
    public CompletableFuture<ProjectGenerationTask> triggerTaskExecution(ProjectGenerationTask submittedTask) throws InterruptedException {
        startCounterByTimer(submittedTask);
        return CompletableFuture.completedFuture(submittedTask);
    }

    private void startCounterByTimer(ProjectGenerationTask submittedTask) {
        AtomicInteger variableX = new AtomicInteger(submittedTask.getX());
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            int counter = variableX.get();
            @Override
            public void run() {
                submittedTask.setX(variableX.getAndIncrement());
                counter++;
                if (counter >= (submittedTask.getY())- variableX.get()){
                    submittedTask.setX(submittedTask.getY());
                    submittedTask.setTaskStatus(TaskStatus.COMPLETED);
                    timer.cancel();
                }
            }
        }, 0, timeIncrementFrequency);
    }

}
