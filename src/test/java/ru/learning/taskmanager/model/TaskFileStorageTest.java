package ru.learning.taskmanager.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.learning.taskmanager.model.Priority;
import ru.learning.taskmanager.model.Task;
import ru.learning.taskmanager.model.TaskStatus;
import ru.learning.taskmanager.storage.ApplicationData;
import ru.learning.taskmanager.storage.TaskFileStorage;
import ru.learning.taskmanager.model.User;
import ru.learning.taskmanager.model.Project;

import java.util.Set;
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

        List<Task> tasks = storage.load().tasks();

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
        storage.save(
                List.of(task),
                List.of(),
                List.of()
        );

        TaskFileStorage anotherStorage = new TaskFileStorage(file);
        List<Task> loadedTasks = anotherStorage.load().tasks();

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
        storage.save(
                List.of(firstTask),
                List.of(),
                List.of()
        );

        Task secondTask = new Task(2, "Вторая задача", Priority.HIGH);
        storage.save(
                List.of(secondTask),
                List.of(),
                List.of()
        );

        List<Task> loadedTasks = storage.load().tasks();

        assertEquals(1, loadedTasks.size());
        assertEquals(2L, loadedTasks.get(0).getId());
        assertEquals("Вторая задача", loadedTasks.get(0).getTitle());
    }

    @Test
    void shouldRestoreUsersAndAssignments() throws IOException {
        Path file = directory.resolve("tasks.properties");

        User user = new User(5, "Анна");

        Task task = new Task(7, "Изучить Java", Priority.HIGH);
        task.assignTo(user.getId());

        TaskFileStorage storage = new TaskFileStorage(file);

        storage.save(
                List.of(task),
                List.of(user),
                List.of()
        );

        ApplicationData loaded =
                new TaskFileStorage(file).load();

        assertEquals(1, loaded.users().size());
        assertEquals(1, loaded.tasks().size());

        assertAll(
                () -> assertEquals(5L, loaded.users().get(0).getId()),
                () -> assertEquals(
                        "Анна",
                        loaded.users().get(0).getName()
                ),
                () -> assertEquals(
                        Long.valueOf(5),
                        loaded.tasks().get(0).getAssigneeId()
                )
        );
    }

    @Test
    void shouldRestoreProjectsAndMemberships() throws IOException {
        Path file = directory.resolve("tasks.properties");

        User user = new User(5, "Анна");

        Project project = new Project(9, "Изучение Java");
        project.addMember(user.getId());

        Task task = new Task(7, "Повторить коллекции", Priority.HIGH);
        task.attachToProject(project.getId());
        task.assignTo(user.getId());

        TaskFileStorage storage = new TaskFileStorage(file);

        storage.save(
                List.of(task),
                List.of(user),
                List.of(project)
        );

        ApplicationData loaded =
                new TaskFileStorage(file).load();

        assertEquals(1, loaded.projects().size());
        assertEquals(1, loaded.tasks().size());

        assertAll(
                () -> assertEquals(
                        "Изучение Java",
                        loaded.projects().get(0).getName()
                ),
                () -> assertEquals(
                        Set.of(5L),
                        loaded.projects().get(0).getMemberIds()
                ),
                () -> assertEquals(
                        Long.valueOf(9),
                        loaded.tasks().get(0).getProjectId()
                ),
                () -> assertEquals(
                        Long.valueOf(5),
                        loaded.tasks().get(0).getAssigneeId()
                )
        );
    }

    @Test
    void shouldRejectAssigneeOutsideProject() {
        Path file = directory.resolve("tasks.properties");

        User user = new User(5, "Анна");
        Project project = new Project(9, "Изучение Java");

        Task task = new Task(7, "Повторить коллекции", Priority.HIGH);
        task.attachToProject(project.getId());
        task.assignTo(user.getId());

        TaskFileStorage storage = new TaskFileStorage(file);

        assertThrows(
                IllegalArgumentException.class,
                () -> storage.save(
                        List.of(task),
                        List.of(user),
                        List.of(project)
                )
        );
    }
}
