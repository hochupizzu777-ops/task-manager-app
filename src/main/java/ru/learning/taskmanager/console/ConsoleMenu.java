package ru.learning.taskmanager.console;

import ru.learning.taskmanager.model.Priority;
import ru.learning.taskmanager.model.Task;
import ru.learning.taskmanager.service.TaskService;
import ru.learning.taskmanager.exception.TaskNotFoundException;
import ru.learning.taskmanager.model.TaskStatus;

import java.util.List;
import java.util.Objects;

public class ConsoleMenu {

    private final TaskService service;
    private final ConsoleInput input;

    public ConsoleMenu(TaskService service, ConsoleInput input){
        this.service = Objects.requireNonNull(
                service,
                "Сервис не может быть null"
        );
        this.input = Objects.requireNonNull(
                input,
                "Ввод не может быть null"
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
            System.out.println("0. Выйти");

            int choice = input.readInt("Ваш выбор", 0, 7);

            try{
                switch (choice){
                    case 1 -> createTask();
                    case 2 -> showAllTasks();
                    case 3 -> findTask();
                    case 4 -> renameTask();
                    case 5 -> changeTaskStatus();
                    case 6 -> changeTaskPriority();
                    case 7 -> deleteTask();
                    case 0 -> {
                        System.out.println("Программа завершена");
                        return;
                    }
                }
            }catch (TaskNotFoundException | IllegalArgumentException exception){
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
        List<Task> tasks = service.getAllTask();

        if (tasks.isEmpty()){
            System.out.println("Задач пока нет");
            return;
        }

        System.out.println("Ваши задачи:");

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

        System.out.println(
                "№" + task.getId()
                        + " | " + task.getTitle()
                        + " | Статус: " + status
                        + " | Приоритет: " + priority
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
}
