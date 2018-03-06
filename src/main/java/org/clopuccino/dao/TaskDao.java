package org.clopuccino.dao;

import ch.qos.logback.classic.Logger;
import org.clopuccino.db.DatabaseAccess;
import org.clopuccino.db.DatabaseConstants;
import org.clopuccino.domain.Task;
import org.clopuccino.domain.TaskStatus;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.StringTokenizer;

/**
 * <code>TaskDao</code>
 *
 * @author masonhsieh
 * @version 1.0
 */
public class TaskDao extends AbstractDao {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(TaskDao.class.getSimpleName());


    public TaskDao() {
        super();
    }

    public TaskDao(DatabaseAccess dbAccess) {
        super(dbAccess);
    }

    public boolean isTableExists() {
        boolean exists = true;

        Connection conn = null;
        ResultSet rs = null;

        try {
            conn = dbAccess.getConnection();

            DatabaseMetaData dbMetaData = conn.getMetaData();

            /* check if table exists */
            rs = dbMetaData.getTables(null, null, DatabaseConstants.TABLE_NAME_TASK, new String[]{"TABLE"});

            exists = rs.next();
        } catch (Exception e) {
            exists = false;

            LOGGER.error(String.format("Error on checking if table '%s' exists.\nerror message:\n%s", DatabaseConstants.TABLE_NAME_TASK, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(rs, null, null, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return exists;
    }

    public boolean createTableIfNotExists() {
        boolean success = true;

        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;

        try {
            conn = dbAccess.getConnection();

            DatabaseMetaData dbMetaData = conn.getMetaData();

            /* check if table exists */
            rs = dbMetaData.getTables(null, null, DatabaseConstants.TABLE_NAME_TASK, new String[]{"TABLE"});

            if (!rs.next()) {
                statement = conn.createStatement();

                statement.executeUpdate(DatabaseConstants.SQL_CREATE_TABLE_TASK);
            }
        } catch (Exception e) {
            success = false;

            LOGGER.error(String.format("Error on creating table '%s'\nerror message:\n%s", DatabaseConstants.TABLE_NAME_TASK, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(rs, statement, null, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return success;
    }

    public boolean recreateDefaultTasks() {
        boolean success = true;

        Connection conn = null;
        Statement statement = null;
        PreparedStatement pStatement = null;

        BufferedReader reader = null;

        try {
            ClassLoader classLoader = getClass().getClassLoader();

            InputStream fileInputStream = classLoader.getResourceAsStream("/" + DatabaseConstants.FILE_NAME_DEFAULT_TASKS);

            if (fileInputStream == null) {
                fileInputStream = classLoader.getResourceAsStream(DatabaseConstants.FILE_NAME_DEFAULT_TASKS);
            }

            if (fileInputStream == null) {
                throw new FileNotFoundException("Task file not found: " + DatabaseConstants.FILE_NAME_DEFAULT_TASKS);
            }

            conn = dbAccess.getConnection();

            statement = conn.createStatement();

            /* delete all tasks */
            statement.executeUpdate(DatabaseConstants.SQL_TRUNCATE_TABLE_TASK);

            LOGGER.debug("Truncate table " + DatabaseConstants.TABLE_NAME_TASK + " successfully before re-creating data.");

            /* create defaults */
            reader = new BufferedReader(new InputStreamReader(fileInputStream, "UTF-8"));

            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                if (line.trim().length() > 0 && !line.trim().startsWith(DatabaseConstants.DEFAULT_PLAIN_TEXT_FILE_COMMENT_CHARACTER)) {
                    /* tab delimiters */
                    StringTokenizer tokenizer = new StringTokenizer(line, DatabaseConstants.FILE_DEFAULT_TASKS_DELIMITERS);

                    pStatement = conn.prepareStatement(DatabaseConstants.SQL_CREATE_TASK);

                    /*
                     * Given the format of each line:
                     * COLUMN_NAME_TASK_ID
                     * COLUMN_NAME_TASK_INTERVAL
                     * COLUMN_NAME_TASK_INITIAL_DELAY
                     */
                    int parameterIndex = 0;
                    String taskId = null;

                    do {
                        String token = tokenizer.nextToken().trim();

                        // DEBUG
//                        LOGGER.info("Task token: " + token);

                        if (++parameterIndex == 1) {
                            // task id
                            taskId = token;
                            pStatement.setString(parameterIndex, taskId);
                        } else if (parameterIndex == 2) {
                            // task interval
                            pStatement.setLong(parameterIndex, Long.valueOf(token));
                        } else if (parameterIndex == 3) {
                            // task initial delay to start
                            pStatement.setLong(parameterIndex, Long.valueOf(token));
                        }
                    } while (tokenizer.hasMoreTokens());

                    // default values for other columns

                    // task status
                    pStatement.setString(4, TaskStatus.TASK_STATUS_NONE.toString());

                    // start timestamp
                    pStatement.setLong(5, 0L);

                    // end timestamp
                    pStatement.setLong(6, 0L);

                    // error message
                    pStatement.setString(7, "");

                    if (pStatement.executeUpdate() > 0) {
                        LOGGER.debug("Task: " + taskId + " created successfully.");
                    } else {
                        LOGGER.warn("Failure to create task: " + taskId);
                    }

                    pStatement.close();
                }
            }
        } catch (Exception e) {
            success = false;

            LOGGER.error(String.format("Error on creating or updating default tasks data.\nerror message:\n%s", e.getMessage()), e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    /* ignored */
                }
            }

            if (dbAccess != null) {
                try {
                    dbAccess.close(null, statement, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return success;
    }

    public Task findTaskById(String taskId) {
        Task task = null;

        Connection conn = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_FIND_TASK_BY_ID);

            pStatement.setString(1, taskId);

            resultSet = pStatement.executeQuery();

            if (resultSet.next()) {
                task = new Task();

                task.setTaskId(resultSet.getString(DatabaseConstants.COLUMN_NAME_TASK_ID));
                task.setTaskInterval(resultSet.getLong(DatabaseConstants.COLUMN_NAME_TASK_INTERVAL));
                task.setTaskInitialDelay(resultSet.getLong(DatabaseConstants.COLUMN_NAME_TASK_INITIAL_DELAY));

                // deal with TaskStatus

                String taskStatusValue = resultSet.getString(DatabaseConstants.COLUMN_NAME_TASK_LATEST_STATUS);
                TaskStatus taskStatus = TaskStatus.taskStatusFrom(taskStatusValue);

                if (taskStatus == null) {
                    taskStatus = TaskStatus.TASK_STATUS_NONE;

                    LOGGER.error("'" + taskStatusValue + "' is not a legal value for TaskStatus. Use default value instead. Correct the value in DB NOW!");
                }

                task.setLatestTaskStatus(taskStatus);

                task.setLatestTaskStartTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_TASK_LATEST_START_TIMESTAMP));
                task.setLatestTaskEndTimestamp(resultSet.getLong(DatabaseConstants.COLUMN_NAME_TASK_LATEST_END_TIMESTAMP));
                task.setLatestTaskErrorMessage(resultSet.getString(DatabaseConstants.COLUMN_NAME_TASK_LATEST_ERROR_MESSAGE));
            }
        } catch (Exception e) {
            task = null;

            LOGGER.error(String.format("Error on finding task by task id '%s'\nerror message:\n%s", taskId, e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(resultSet, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        return task;
    }

    public Task createTask(Task task) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        boolean success = true;

        String taskId = task.getTaskId();
        Long taskInterval = task.getTaskInterval();
        Long taskInitialDelay = task.getTaskInitialDelay();
        TaskStatus latestTaskStatus = task.getLatestTaskStatus();
        Long latestTaskStartTimestamp = task.getLatestTaskStartTimestamp();
        Long latestTaskEndTimestamp = task.getLatestTaskEndTimestamp();
        String latestTaskErrorMessage = task.getLatestTaskErrorMessage();

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_CREATE_TASK);

            pStatement.setString(1, taskId);
            pStatement.setLong(2, taskInterval);
            pStatement.setLong(3, taskInitialDelay);
            pStatement.setString(4, latestTaskStatus.toString());
            pStatement.setLong(5, latestTaskStartTimestamp);
            pStatement.setLong(6, latestTaskEndTimestamp);
            pStatement.setString(7, latestTaskErrorMessage);

            pStatement.executeUpdate();
        } catch (Exception e) {
            success = false;

            LOGGER.error(String.format("Error on creating task: '%s'\nerror message:\n%s", task.toString(), e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(null, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }

        if (success) {
            return findTaskById(taskId);
        } else {
            return null;
        }
    }

    /**
     * Updates Task without interal nor initial delay.
     */
    public void updateTask(Task task) {
        Connection conn = null;
        PreparedStatement pStatement = null;

        String taskId = task.getTaskId();

        // Skip update interval and initial delay？

        TaskStatus latestTaskStatus = task.getLatestTaskStatus();
        Long latestTaskStartTimestamp = task.getLatestTaskStartTimestamp();
        Long latestTaskEndTimestamp = task.getLatestTaskEndTimestamp();
        String latestTaskErrorMessage = task.getLatestTaskErrorMessage();

        try {
            conn = dbAccess.getConnection();

            pStatement = conn.prepareStatement(DatabaseConstants.SQL_UPDATE_TASK);

            pStatement.setString(1, latestTaskStatus.toString());
            pStatement.setLong(2, latestTaskStartTimestamp);
            pStatement.setLong(3, latestTaskEndTimestamp);
            pStatement.setString(4, latestTaskErrorMessage);
            pStatement.setString(5, taskId);

            pStatement.executeUpdate();
        } catch (Exception e) {
            LOGGER.error(String.format("Error on updating task: '%s'\nerror message:\n%s", task.toString(), e.getMessage()), e);
        } finally {
            if (dbAccess != null) {
                try {
                    dbAccess.close(null, null, pStatement, conn);
                } catch (Exception e) {
                    /* ignored */
                }
            }
        }
    }
}
