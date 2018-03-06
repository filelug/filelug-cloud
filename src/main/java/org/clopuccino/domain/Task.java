package org.clopuccino.domain;

/**
 * <code>Task</code> models a task object.
 *
 * @author masonhsieh
 * @version 1.0
 */
public class Task implements Cloneable {

    private String taskId;

    // in seconds
    private Long taskInterval;

    // in seconds
    private Long taskInitialDelay;

    private TaskStatus latestTaskStatus;

    private Long latestTaskStartTimestamp;

    private Long latestTaskEndTimestamp;

    private String latestTaskErrorMessage;

    public Task() {
    }

    public Task(String taskId, Long taskInterval, Long taskInitialDelay, TaskStatus latestTaskStatus, Long latestTaskStartTimestamp, Long latestTaskEndTimestamp, String latestTaskErrorMessage) {
        this.taskId = taskId;
        this.taskInterval = taskInterval;
        this.taskInitialDelay = taskInitialDelay;
        this.latestTaskStatus = latestTaskStatus;
        this.latestTaskStartTimestamp = latestTaskStartTimestamp;
        this.latestTaskEndTimestamp = latestTaskEndTimestamp;
        this.latestTaskErrorMessage = latestTaskErrorMessage;
    }

    public Long getLatestTaskEndTimestamp() {
        return latestTaskEndTimestamp;
    }

    public void setLatestTaskEndTimestamp(Long latestTaskEndTimestamp) {
        this.latestTaskEndTimestamp = latestTaskEndTimestamp;
    }

    public String getLatestTaskErrorMessage() {
        return latestTaskErrorMessage;
    }

    public void setLatestTaskErrorMessage(String latestTaskErrorMessage) {
        this.latestTaskErrorMessage = latestTaskErrorMessage;
    }

    public Long getLatestTaskStartTimestamp() {
        return latestTaskStartTimestamp;
    }

    public void setLatestTaskStartTimestamp(Long latestTaskStartTimestamp) {
        this.latestTaskStartTimestamp = latestTaskStartTimestamp;
    }

    public TaskStatus getLatestTaskStatus() {
        return latestTaskStatus;
    }

    public void setLatestTaskStatus(TaskStatus latestTaskStatus) {
        this.latestTaskStatus = latestTaskStatus;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Long getTaskInterval() {
        return taskInterval;
    }

    public void setTaskInterval(Long taskInterval) {
        this.taskInterval = taskInterval;
    }

    public Long getTaskInitialDelay() {
        return taskInitialDelay;
    }

    public void setTaskInitialDelay(Long taskInitialDelay) {
        this.taskInitialDelay = taskInitialDelay;
    }

    @Override
    public String toString() {
        return "Task{" +
               "latestTaskEndTimestamp=" + latestTaskEndTimestamp +
               ", taskId='" + taskId + '\'' +
               ", taskInterval=" + taskInterval +
               ", taskInitialDelay=" + taskInitialDelay +
               ", latestTaskStatus=" + latestTaskStatus +
               ", latestTaskStartTimestamp=" + latestTaskStartTimestamp +
               ", latestTaskErrorMessage='" + latestTaskErrorMessage + '\'' +
               '}';
    }
}
