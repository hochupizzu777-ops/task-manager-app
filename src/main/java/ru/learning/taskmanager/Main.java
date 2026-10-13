package ru.learning.taskmanager;

import ru.learning.taskmanager.console.ConsoleInput;
import ru.learning.taskmanager.console.ConsoleMenu;
import ru.learning.taskmanager.repository.InMemoryTaskRepository;
import ru.learning.taskmanager.repository.TaskRepository;
import ru.learning.taskmanager.service.TaskService;

import java.util.Optional;
import java.util.Scanner;

public class Main {

    public static void main(String[] args){
        TaskRepository repository = new InMemoryTaskRepository();
        TaskService service = new TaskService(repository);

        try(Scanner scanner = new Scanner(System.in)){
            ConsoleInput input = new ConsoleInput(scanner);
            ConsoleMenu menu = new ConsoleMenu(service, input);

            menu.run();
        }
    }
}