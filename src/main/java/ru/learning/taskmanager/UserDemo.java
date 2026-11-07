package ru.learning.taskmanager;

import ru.learning.taskmanager.model.Priority;
import ru.learning.taskmanager.model.Task;
import ru.learning.taskmanager.model.User;
import ru.learning.taskmanager.repository.*;
import ru.learning.taskmanager.service.TaskAssignmentService;
import ru.learning.taskmanager.service.TaskService;
import ru.learning.taskmanager.service.UserService;

public class UserDemo {

    public static void main(String[] args) {
        TaskRepository taskRepository =
                new InMemoryTaskRepository();

        UserRepository userRepository =
                new InMemoryUserRepository();

        ProjectRepository projectRepository =
                new InMemoryProjectRepository();

        TaskService taskService =
                new TaskService(taskRepository);

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

        User user = userService.createUser("Максим");

        Task task = taskService.createTask(
                "Изучить связи между объектами",
                Priority.HIGH
        );

        assignmentService.assignTask(task.getId(), user.getId());

        System.out.println(
                "ID исполнителя: " + task.getAssigneeId()
        );

        System.out.println("Задачи пользователя:");

        assignmentService.getTasksByUser(user.getId())
                .forEach(System.out::println);

        try {
            userService.deleteUser(user.getId());
        } catch (IllegalStateException exception) {
            System.out.println(exception.getMessage());
        }

        assignmentService.unassignTask(task.getId());

        System.out.println(
                "После снятия назначения: " + task.getAssigneeId()
        );

        userService.deleteUser(user.getId());

        System.out.println(
                "Пользователей осталось: "
                        + userService.getAllUsers().size()
        );
    }
}