package org.clopuccino.domain;

/**
 * <code>TaskStatus</code> represents the status of a task.
 *
 * @author masonhsieh
 * @version 1.0
 */
public enum TaskStatus {
    TASK_STATUS_NONE("none"),
    TASK_STATUS_PROCESS("process"),
    TASK_STATUS_SUCCESS("success"),
    TASK_STATUS_FAILURE("failure");

    private final String text;

    private TaskStatus(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }

    public String getText() {
        return text;
    }

    public static TaskStatus taskStatusFrom(String from) {
        TaskStatus result = null;

        TaskStatus[] allValues = TaskStatus.values();

        for (TaskStatus theStatus : allValues) {
            if (theStatus.getText().equals(from)) {
                result = theStatus;

                break;
            }
        }

        return result;
    }
}