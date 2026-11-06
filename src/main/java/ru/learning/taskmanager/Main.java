package ru.learning.taskmanager;

import ru.learning.taskmanager.console.ConsoleInput;
import ru.learning.taskmanager.console.ConsoleMenu;
import ru.learning.taskmanager.model.Project;
import ru.learning.taskmanager.model.Task;
import ru.learning.taskmanager.model.User;
import ru.learning.taskmanager.repository.InMemoryProjectRepository;
import ru.learning.taskmanager.repository.InMemoryTaskRepository;
import ru.learning.taskmanager.repository.InMemoryUserRepository;
import ru.learning.taskmanager.repository.ProjectRepository;
import ru.learning.taskmanager.repository.TaskRepository;
import ru.learning.taskmanager.repository.UserRepository;
import ru.learning.taskmanager.service.ProjectService;
import ru.learning.taskmanager.service.TaskAssignmentService;
import ru.learning.taskmanager.service.TaskService;
import ru.learning.taskmanager.service.UserService;
import ru.learning.taskmanager.storage.ApplicationData;
import ru.learning.taskmanager.storage.TaskFileStorage;

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

        ProjectRepository projectRepository =
                new InMemoryProjectRepository();

        try {
            ApplicationData data = storage.load();

            for (User user : data.users()) {
                userRepository.save(user);
            }

            for (Project project : data.projects()) {
                projectRepository.save(project);
            }

            for (Task task : data.tasks()) {
                taskRepository.save(task);
            }

            TaskService taskService = new TaskService(
                    taskRepository
            );

            UserService userService = new UserService(
                    userRepository,
                    taskRepository,
                    projectRepository
            );

            TaskAssignmentService assignmentService =
                    new TaskAssignmentService(
                            taskRepository,
                            userRepository,
                            projectRepository
                    );

            ProjectService projectService = new ProjectService(
                    projectRepository,
                    userRepository,
                    taskRepository
            );

            try (Scanner scanner = new Scanner(System.in)) {
                ConsoleInput input = new ConsoleInput(scanner);

                ConsoleMenu menu = new ConsoleMenu(
                        taskService,
                        input,
                        userService,
                        assignmentService,
                        projectService
                );

                menu.run();
            }

            storage.save(
                    taskRepository.findAll(),
                    userRepository.findAll(),
                    projectRepository.findAll()
            );

            System.out.println("Данные сохранены.");
        } catch (IOException exception) {
            System.out.println(
                    "Ошибка работы с файлом: "
                            + exception.getMessage()
            );
        }
    }
}