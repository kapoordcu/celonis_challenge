# Celonis Programming Challenge

## WHAT

- While running this project , A user of this application (lets call him AU) can create, retrieve, update, delete the tasks
- AU can also trigger execution of a task,  monitor the execution status and cancel the running execution
- The task can be in many status i.e. `CREATED`, `IN_EXECUTION`, `COMPLETED`, `CANCELLED`


### v1 APIs

| API       | HTTP METHOD |                                   URI | Description |
|-----------|:-----------:|--------------------------------------:|-----------:|
| listTasks  |     GET     |                       `/v1/api/tasks` | |
| createTask  |    POST     |                       `/v1/api/tasks` | |
| getTask  |     GET     |              `/v1/api/tasks/{taskId}` | |
| updateTask  |     PUT     |              `/v1/api/tasks/{taskId}` | |
| deleteTask  |   DELETE    |              `/v1/api/tasks/{taskId}` | |
| getResult  |   GET    |       `/v1/api/tasks/result/{taskId}` | |
| executeTask  |   POST    |       `/v1/api/tasks/execute/{taskId}` | |

### v2 APIs

| API       | HTTP METHOD |                                   URI | Description |
|-----------|:-----------:|--------------------------------------:|-----------:|
| triggerExecution  |    POST     |                       `/v2/api/tasks/execute/{taskId}` | |
| cancelExecution  |    POST     |                       `/v2/api/tasks/cancel/{taskId}` | |
| getExecutionStatus  |      GET      |                       `/v2/api/tasks/status/{taskId}` | |



## HOW

### Run Locally
- Just run ChallengeApplication 


### How can someone monitor the progress of the task
Whenever the task is running , the x parameter is being incremented every second , so an api call to `/v2/api/tasks/status/{taskId}` will return the status
### Cleanup Job
Scheduled task run every month 1st day at 12 AM using cron expression
### Demo
Stay tuned !!

### IMPORTANT
A detailed feedback is requested about the thinks you liked/disliked in the application

Happy Coding !!