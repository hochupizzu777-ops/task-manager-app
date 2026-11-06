package ru.learning.taskmanager.console;

import ru.learning.taskmanager.exception.ProjectNotFoundException;
import ru.learning.taskmanager.exception.TaskNotFoundException;
import ru.learning.taskmanager.exception.UserNotFoundException;
import ru.learning.taskmanager.model.Project;
import ru.learning.taskmanager.model.Task;
import ru.learning.taskmanager.model.User;
import ru.learning.taskmanager.service.ProjectService;

import java.util.List;
import java.util.Objects;

public class ProjectConsoleMenu {

    private final ProjectService service;
    private final ConsoleInput input;

    public ProjectConsoleMenu(
            ProjectService service,
            ConsoleInput input
    ) {
        this.service = Objects.requireNonNull(
                service,
                "Сервис проектов не должен быть null"
        );

        this.input = Objects.requireNonNull(
                input,
                "Объект ввода не должен быть null"
        );
    }

    public void run() {
        while (true) {
            System.out.println();
            System.out.println("=== Проекты ===");
            System.out.println("1. Создать проект");
            System.out.println("2. Показать все проекты");
            System.out.println("3. Переименовать проект");
            System.out.println("4. Добавить участника");
            System.out.println("5. Показать участников");
            System.out.println("6. Исключить участника");
            System.out.println("7. Добавить или перенести задачу в проект");
            System.out.println("8. Убрать задачу из проекта");
            System.out.println("9. Показать задачи проекта");
            System.out.println("10. Удалить проект");
            System.out.println("0. Вернуться в главное меню");

            int choice = input.readInt("Ваш выбор: ", 0, 10);

            try {
                switch (choice) {
                    case 1 -> createProject();
                    case 2 -> showAllProjects();
                    case 3 -> renameProject();
                    case 4 -> addMember();
                    case 5 -> showMembers();
                    case 6 -> removeMember();
                    case 7 -> addTask();
                    case 8 -> removeTask();
                    case 9 -> showTasks();
                    case 10 -> deleteProject();
                    case 0 -> {
                        return;
                    }
                }
            } catch (
                    ProjectNotFoundException
                    | TaskNotFoundException
                    | UserNotFoundException
                    | IllegalArgumentException
                    | IllegalStateException exception
            ) {
                System.out.println(
                        "Ошибка: " + exception.getMessage()
                );
            }
        }
    }

    private long readProjectId() {
        return input.readPositiveLong(
                "Идентификатор проекта: "
        );
    }

    private long readUserId() {
        return input.readPositiveLong(
                "Идентификатор пользователя: "
        );
    }

    private long readTaskId() {
        return input.readPositiveLong(
                "Идентификатор задачи: "
        );
    }

    private void createProject() {
        String name = input.readRequiredLine(
                "Название проекта: "
        );

        Project project = service.createProject(name);

        System.out.println(
                "Создан проект №" + project.getId()
                        + ": " + project.getName()
        );
    }

    private void showAllProjects() {
        List<Project> projects = service.getAllProjects();

        if (projects.isEmpty()) {
            System.out.println("Проектов пока нет.");
            return;
        }

        for (Project project : projects) {
            System.out.println(
                    "№" + project.getId()
                            + " | " + project.getName()
                            + " | Участников: "
                            + project.getMemberIds().size()
            );
        }
    }

    private void renameProject() {
        long projectId = readProjectId();

        String name = input.readRequiredLine(
                "Новое название: "
        );

        service.renameProject(projectId, name);

        System.out.println("Название проекта изменено.");
    }

    private void addMember() {
        long projectId = readProjectId();
        long userId = readUserId();

        boolean added = service.addMember(projectId, userId);

        System.out.println(
                added
                        ? "Участник добавлен."
                        : "Пользователь уже состоит в проекте."
        );
    }

    private void showMembers() {
        long projectId = readProjectId();

        List<User> members = service.getMembers(projectId);

        if (members.isEmpty()) {
            System.out.println("В проекте пока нет участников.");
            return;
        }

        for (User user : members) {
            System.out.println(
                    "№" + user.getId() + ": " + user.getName()
            );
        }
    }

    private void removeMember() {
        long projectId = readProjectId();
        long userId = readUserId();

        boolean removed = service.removeMember(projectId, userId);

        System.out.println(
                removed
                        ? "Участник исключён."
                        : "Пользователь не состоит в проекте."
        );
    }

    private void addTask() {
        long projectId = readProjectId();
        long taskId = readTaskId();

        service.addTaskToProject(projectId, taskId);

        System.out.println("Задача добавлена в проект.");
    }

    private void removeTask() {
        long taskId = readTaskId();

        service.removeTaskFromProject(taskId);

        System.out.println("Задача теперь самостоятельная.");
    }

    private void showTasks() {
        long projectId = readProjectId();

        List<Task> tasks = service.getTasksByProject(projectId);

        if (tasks.isEmpty()) {
            System.out.println("В проекте пока нет задач.");
            return;
        }

        for (Task task : tasks) {
            System.out.println(
                    "№" + task.getId() + ": " + task.getTitle()
            );
        }
    }

    private void deleteProject() {
        long projectId = readProjectId();

        service.deleteProject(projectId);

        System.out.println("Проект удалён.");
    }
}
