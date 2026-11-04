package ru.learning.taskmanager;

import ru.learning.taskmanager.console.ConsoleInput;
import ru.learning.taskmanager.console.ConsoleMenu;
import ru.learning.taskmanager.model.Task;
import ru.learning.taskmanager.model.User;
import ru.learning.taskmanager.repository.InMemoryTaskRepository;
import ru.learning.taskmanager.repository.InMemoryUserRepository;
import ru.learning.taskmanager.repository.TaskRepository;
import ru.learning.taskmanager.repository.UserRepository;
import ru.learning.taskmanager.service.TaskService;
import ru.learning.taskmanager.storage.ApplicationData;
import ru.learning.taskmanager.storage.TaskFileStorage;
import ru.learning.taskmanager.service.UserService;
import ru.learning.taskmanager.service.TaskAssignmentService;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        TaskFileStorage storage = new TaskFileStorage(
                Path.of("data", "tasks.properties")
        );

        TaskRepository taskRepository =
                new InMemoryTaskRepository();

        UserRepository userRepository =
                new InMemoryUserRepository();

        TaskService service = new TaskService(taskRepository);

        UserService userService = new UserService(
                userRepository,
                taskRepository
        );

        TaskAssignmentService assignmentService =
                new TaskAssignmentService(
                        taskRepository,
                        userRepository
                );

        try (Scanner scanner = new Scanner(System.in)) {
            ConsoleInput input = new ConsoleInput(scanner);

            ConsoleMenu menu = new ConsoleMenu(
                    service,
                    input,
                    userService,
                    assignmentService
            );

            menu.run();
        }
    }
}