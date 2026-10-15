package ru.learning.taskmanager;

import ru.learning.taskmanager.console.ConsoleInput;
import ru.learning.taskmanager.console.ConsoleMenu;
import ru.learning.taskmanager.model.Task;
import ru.learning.taskmanager.repository.InMemoryTaskRepository;
import ru.learning.taskmanager.repository.TaskRepository;
import ru.learning.taskmanager.service.TaskService;
import ru.learning.taskmanager.storage.TaskFileStorage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        TaskFileStorage storage = new TaskFileStorage(
                Path.of("data", "tasks.properties")
        );

        TaskRepository repository = new InMemoryTaskRepository();

        try {
            List<Task> loadedTasks = storage.load();

            for (Task task : loadedTasks) {
                repository.save(task);
            }

            TaskService service = new TaskService(repository);

            try (Scanner scanner = new Scanner(System.in)) {
                ConsoleInput input = new ConsoleInput(scanner);
                ConsoleMenu menu = new ConsoleMenu(service, input);

                menu.run();
            }

            storage.save(repository.findAll());

            System.out.println("Задачи сохранены.");
        } catch (IOException exception) {
            System.out.println(
                    "Ошибка работы с файлом: "
                            + exception.getMessage()
            );
        }
    }
}