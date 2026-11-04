package ru.learning.taskmanager.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.learning.taskmanager.model.Priority;
import ru.learning.taskmanager.model.Task;
import ru.learning.taskmanager.model.TaskStatus;
import ru.learning.taskmanager.storage.TaskFileStorage;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TaskFileStorageTest {

    @TempDir
    Path directory;

    @Test
    void missingFileShouldReturnEmptyList() throws IOException {
        Path file = directory.resolve("missing.properties");
        TaskFileStorage storage = new TaskFileStorage(file);

        List<Task> tasks = storage.load();

        assertTrue(tasks.isEmpty());
    }

    @Test
    void shouldRestoreAllTaskFields() throws IOException {
        Path file = directory.resolve("data")
                .resolve("tasks.properties");

        String title = "Java = коллекции \\ практика";

        Task task = new Task(7, title, Priority.HIGH);
        task.changeStatus(TaskStatus.DONE);
        task.changeDueDate(LocalDate.of(2026, 12, 10));

        TaskFileStorage storage = new TaskFileStorage(file);
        storage.save(List.of(task));

        TaskFileStorage anotherStorage = new TaskFileStorage(file);
        List<Task> loadedTasks = anotherStorage.load();

        assertEquals(1, loadedTasks.size());

        Task loadedTask = loadedTasks.get(0);

        assertAll(
                () -> assertEquals(7L, loadedTask.getId()),
                () -> assertEquals(title, loadedTask.getTitle()),
                () -> assertEquals(
                        Priority.HIGH,
                        loadedTask.getPriority()
                ),
                () -> assertEquals(
                        TaskStatus.DONE,
                        loadedTask.getStatus()
                ),
                () -> assertEquals(
                        LocalDate.of(2026, 12, 10),
                        loadedTask.getDueDate()
                )
        );
    }

    @Test
    void secondSaveShouldReplacePreviousData() throws IOException {
        Path file = directory.resolve("data")
                .resolve("tasks.properties");

        TaskFileStorage storage = new TaskFileStorage(file);

        Task firstTask = new Task(1, "Первая задача", Priority.LOW);
        storage.save(List.of(firstTask));

        Task secondTask = new Task(2, "Вторая задача", Priority.HIGH);
        storage.save(List.of(secondTask));

        List<Task> loadedTasks = storage.load();

        assertEquals(1, loadedTasks.size());
        assertEquals(2L, loadedTasks.get(0).getId());
        assertEquals("Вторая задача", loadedTasks.get(0).getTitle());
    }
}
