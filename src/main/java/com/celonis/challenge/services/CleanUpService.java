package com.celonis.challenge.services;

import com.celonis.challenge.model.TaskStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.Calendar;

@Service
public class CleanUpService {
    private TaskService taskService;

    @Scheduled(cron = "0 0 1 * * *")
    public void cleanupOldTasks() {
        Calendar c= Calendar.getInstance();
        c.add(Calendar.DATE, -30);
        taskService.listTasks()
                .stream()
                .filter(task -> (task.getCreationDate().before(c.getTime())) && TaskStatus.CREATED==task.getTaskStatus())
                .forEach(task ->
                taskService.delete(task.getId()));
    }
}
