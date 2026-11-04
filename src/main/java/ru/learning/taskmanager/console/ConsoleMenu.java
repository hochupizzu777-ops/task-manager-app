package ru.learning.taskmanager.console;

import ru.learning.taskmanager.model.Priority;
import ru.learning.taskmanager.model.Task;
import ru.learning.taskmanager.service.TaskService;
import ru.learning.taskmanager.exception.TaskNotFoundException;
import ru.learning.taskmanager.model.TaskStatus;
import java.time.LocalDate;
import ru.learning.taskmanager.exception.UserNotFoundException;
import ru.learning.taskmanager.model.User;
import ru.learning.taskmanager.service.UserService;
import ru.learning.taskmanager.service.TaskAssignmentService;

import java.util.Comparator;

import java.util.List;
import java.util.Objects;

public class ConsoleMenu {

    private final TaskService service;
    private final ConsoleInput input;
    private final UserService userService;
    private final TaskAssignmentService assignmentService;

    public ConsoleMenu(TaskService service,
                       ConsoleInput input,
                       UserService userService,
                       TaskAssignmentService assignmentService
    ){
        this.service = Objects.requireNonNull(
                service,
                "Сервис задач не может быть null"
        );
        this.input = Objects.requireNonNull(
                input,
                "Ввод не может быть null"
        );
        this.userService = Objects.requireNonNull(
                userService,
                "Сервис пользователей не должен быть null"
        );
        this.assignmentService = Objects.requireNonNull(
                assignmentService,
                "Сервис назначений не должен быть null"
        );
    }

    public void run(){
        while(true){
            System.out.println();
            System.out.println("=== Управление задачами ===");
            System.out.println("1. Создать задачу");
            System.out.println("2. Показать все задачи");
            System.out.println("3. Найти задачу");
            System.out.println("4. Переименовать задачу");
            System.out.println("5. Изменить статус");
            System.out.println("6. Изменить приоритет");
            System.out.println("7. Удалить задачу");
            System.out.println("8. Поиск по названию");
            System.out.println("9. Фильтр по статусу");
            System.out.println("10. Сортировка по приоритету");
            System.out.println("11. Назначить или убрать срок");
            System.out.println("12. Показать просроченные задачи");
            System.out.println("13. Пользователи");
            System.out.println("14. Назначить исполнителя");
            System.out.println("15. Снять исполнителя");
            System.out.println("16. Показать задачи пользователя");
            System.out.println("0. Выйти");

            int choice = input.readInt("Ваш выбор", 0, 16);

            try{
                switch (choice){
                    case 1 -> createTask();
                    case 2 -> showAllTasks();
                    case 3 -> findTask();
                    case 4 -> renameTask();
                    case 5 -> changeTaskStatus();
                    case 6 -> changeTaskPriority();
                    case 7 -> deleteTask();
                    case 8 -> searchTasksByTitle();
                    case 9 -> filterTasksByStatus();
                    case 10 -> showTasksSortedByPriority();
                    case 11 -> changeTaskDueDate();
                    case 12 -> showOverdueTasks();
                    case 13 -> runUsersMenu();
                    case 14 -> assignTask();
                    case 15 -> unassignTask();
                    case 16 -> showUserTasks();
                    case 0 -> {
                        System.out.println("Программа завершена");
                        return;
                    }
                }
            }catch (TaskNotFoundException
                    | UserNotFoundException
                    | IllegalStateException
                    | IllegalArgumentException exception){
                System.out.println("Ошибка: " + exception.getMessage());
            }
        }
    }

    private void createTask(){
        String title = input.readRequiredLine(
                "Название задачи: "
        );

        Priority priority = readPriority();

        Task task = service.createTask(title, priority);

        System.out.println(
                "Создана задача №" + task.getId()
                        + ": " + task.getTitle()
        );
    }

    private Priority readPriority(){
        System.out.println("Выберите приоритет:");
        System.out.println("1. Низкий");
        System.out.println("2. Средний");
        System.out.println("3. Высокий");

        int choice = input.readInt("Ваш выбор: ", 1, 3);

        return switch (choice){
            case 1 -> Priority.LOW;
            case 2 -> Priority.MEDIUM;
            case 3 -> Priority.HIGH;
            default -> throw new IllegalArgumentException(
                    "Неизвестный приоритет"
            );
        };
    }

    private void showAllTasks(){
        showTasks(service.getAllTask());
    }

    private void showTasks(List<Task> tasks){
        if (tasks.isEmpty()){
            System.out.println("Список задач пустой");
            return;
        }

        for (Task task : tasks){
            printTask(task);
        }
    }

    public void findTask(){
        long id = readTaskId();

        Task task = service.getTask(id);

        printTask(task);
    }

    public void renameTask(){
        long id = readTaskId();

        String newTitle = input.readRequiredLine(
                "Новое название задачи: "
        );

        service.renameTask(id, newTitle);

        System.out.println("Название изменено");
    }

    public void changeTaskStatus(){
        long id = readTaskId();

        TaskStatus status = readStatus();

        service.changeTaskStatus(id, status);

        System.out.println("Статус изменён.");
    }
    public void changeTaskPriority(){
        long id = readTaskId();

        Priority priority = readPriority();

        service.changeTaskPriority(id, priority);

        System.out.println("Приоритет изменён.");
    }

    private void deleteTask(){
        long id = readTaskId();

        service.deleteTask(id);

        System.out.println("Задача удалена.");
    }

    private long readTaskId(){
        return input.readPositiveLong("Идентификатор задачи: ");
    }

    private void printTask(Task task){
        String status = switch (task.getStatus()) {
            case TODO -> "Нужно выполнить";
            case IN_PROGRESS -> "В работе";
            case DONE -> "Завершена";
        };

        String priority = switch (task.getPriority()) {
            case LOW -> "Низкий";
            case MEDIUM -> "Средний";
            case HIGH -> "Высокий";
        };

        String dueDate = task.getDueDate() == null
                ? "не задана"
                : task.getDueDate().toString();

        System.out.println(
                "№" + task.getId()
                        + " | " + task.getTitle()
                        + " | Статус: " + status
                        + " | Приоритет: " + priority
                        + " | Срок: " + dueDate
                        + " | Исполнитель: " + getAssigneeText(task)
        );
    }

    private TaskStatus readStatus() {
        System.out.println("Выберите статус:");
        System.out.println("1. Нужно выполнить");
        System.out.println("2. В работе");
        System.out.println("3. Завершена");

        int choice = input.readInt("Ваш выбор: ", 1, 3);

        return switch (choice) {
            case 1 -> TaskStatus.TODO;
            case 2 -> TaskStatus.IN_PROGRESS;
            case 3 -> TaskStatus.DONE;
            default -> throw new IllegalArgumentException(
                    "Неизвестный статус"
            );
        };
    }

    private void searchTasksByTitle(){
        String query = input.readRequiredLine(
                "Введите часть названия: "
        );

        List<Task> tasks = service.getTaskByTitle(query);

        showTasks(tasks);
    }

    private void filterTasksByStatus(){
        TaskStatus status = readStatus();

        List<Task> tasks = service.getTasksByStatus(status);

        showTasks(tasks);
    }

    private void showTasksSortedByPriority(){
        List<Task> tasks = service.getTaskByPriority();

        showTasks(tasks);
    }

    private void changeTaskDueDate(){
        long id = readTaskId();

        LocalDate dueDate = input.readOptionalDate(
                "Срок ГГГГ-ММ-ДД (Enter — убрать срок): "
        );

        service.changeTaskDueDate(id, dueDate);

        System.out.println("Срок задачи обновлён.");
    }

    private void showOverdueTasks(){
        showTasks(service.getOverdueTask());
    }

    private void runUsersMenu(){
        while(true){
            System.out.println();
            System.out.println("=== Пользователи ===");
            System.out.println("1. Создать пользователя");
            System.out.println("2. Показать всех пользователей");
            System.out.println("3. Переименовать пользователя");
            System.out.println("4. Удалить пользователя");
            System.out.println("0. Вернуться в главное меню");

            int choice = input.readInt("Ваш выбор: ", 0, 4);

            try{
                switch (choice){
                    case 1 -> createUser();
                    case 2 -> showAllUser();
                    case 3 -> renameUser();
                    case 4 -> deleteUser();
                    case 0 -> {
                        return;
                    }
                }
            } catch (IllegalStateException
                | IllegalArgumentException
                | UserNotFoundException exception
            ) {
                System.out.println("Ошибка: " + exception.getMessage());
            }
        }
    }

    private long readUserId(){
        long id = input.readPositiveLong(
                "Введи id исполнителя"
        );
        return id;
    }

    private void createUser(){
        String name = input.readRequiredLine(
                "Введите имя нового исполнителя"
        );

        User user = userService.createUser(name);

        System.out.println(
                "Создан пользователь №" + user.getId()
                        + ": " + user.getName()
        );
    }
    private void showAllUser(){
        List<User> users = userService.getAllUsers();

        if (users.isEmpty()) {
            System.out.println("Пользователей пока нет.");
            return;
        }

        for(User user : users
                .stream()
                .sorted(Comparator.comparingLong(User::getId))
                .toList()){
            System.out.println(user);
        }
    }

    private void renameUser(){
        long id = readUserId();

        String newName = input.readRequiredLine(
                "Введите новое имя для исполнителя"
        );

        userService.renameUser(id, newName);

        System.out.println("Имя пользователя изменено.");
    }

    private void deleteUser(){
        long id = readUserId();

        userService.deleteUser(id);

        System.out.println("Пользователь удалён.");
    }

    private void assignTask(){
        if (userService.getAllUsers().isEmpty()){
            System.out.println("Сначала создайте исполнителей");
            return;
        }

        long userId = readUserId();
        long taskId = readTaskId();

        assignmentService.assignTask(userId, taskId);

        System.out.println("Исполнитель назначен.");
    }

    private void unassignTask(){
        long id = readTaskId();

        assignmentService.unassignTask(id);

        System.out.println("Назначение снято.");
    }

    private void showUserTasks(){
        long id = readUserId();

        List<Task> tasks = assignmentService.getTasksByUser(id);

        showTasks(tasks);
    }

    private String getAssigneeText(Task task) {
        Long assigneeId = task.getAssigneeId();

        if (assigneeId == null) {
            return "не назначен";
        }

        User user = userService.getUser(assigneeId);

        return user.getName() + " (№" + user.getId() + ")";
    }
}
