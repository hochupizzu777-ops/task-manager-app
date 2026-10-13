package ru.learning.taskmanager;

import ru.learning.taskmanager.model.Priority;
import ru.learning.taskmanager.model.Task;
import ru.learning.taskmanager.repository.InMemoryTaskRepository;
import ru.learning.taskmanager.repository.TaskRepository;

import java.util.Optional;

public class Main {

    public static void main(String[] args) {
        TaskRepository tasks = new InMemoryTaskRepository();

        Task firstTask = new Task(
                1,
                "Изучить коллекции",
                Priority.HIGH
        );

        Task secondTask = new Task(
                2,
                "Повторить исключения",
                Priority.MEDIUM
        );

        System.out.println("Система управления задачами запущена!");
    }
}