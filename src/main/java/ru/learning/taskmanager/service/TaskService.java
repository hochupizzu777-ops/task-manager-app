package ru.learning.taskmanager.service;

import ru.learning.taskmanager.exception.TaskNotFoundException;
import ru.learning.taskmanager.model.Priority;
import ru.learning.taskmanager.model.Task;
import ru.learning.taskmanager.model.TaskStatus;
import ru.learning.taskmanager.repository.TaskRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Comparator;
import java.util.Locale;

public class TaskService {

    private final TaskRepository repository;
    private long nextId;

    public TaskService(TaskRepository repository){
        this.repository = Objects.requireNonNull(
                repository,
                "Хранилище не может быть пустым"
        );

        long maxId = 0;

        for (Task task : repository.findAll()){
            if (task.getId() > maxId){
                maxId = task.getId();
            }
        }

        this.nextId = maxId + 1;
    }

    public Task createTask(String title, Priority priority){
        Task task = new Task(nextId, title, priority);

        repository.save(task);
        nextId++;

        return task;
    }

    public Task getTask(long id){
        return repository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    public List<Task> getAllTask(){
        return repository.findAll();
    }

    public void renameTask (long id, String newTitle){
        Task task = getTask(id);

        task.rename(newTitle);

        repository.save(task);
    }

    public void changeTaskStatus(long id, TaskStatus status){
        Task task = getTask(id);

        task.changeStatus(status);

        repository.save(task);
    }

    public void changeTaskPriority(long id, Priority priority){
        Task task = getTask(id);

        task.changePriority(priority);

        repository.save(task);
    }

    public void deleteTask(long id){
        boolean delete = repository.deleteBeId(id);

        if(!delete){
            throw new TaskNotFoundException(
                    id
            );
        }
    }

    public List<Task> getTaskByTitle(String query){
        if (query == null || query.isBlank()){
            throw new IllegalArgumentException(
                    "Нельзя найти задачи через null или пустоту"
            );
        }

        String normalizedQuery = query.strip()
                .toLowerCase(Locale.ROOT);

        return repository.findAll().stream()
                .filter(task ->
                        task.getTitle()
                        .strip()
                        .toLowerCase(Locale.ROOT)
                        .contains(normalizedQuery))
                .sorted(Comparator.comparingLong(Task::getId))
                .toList();
    }

    public List<Task> getTasksByStatus(TaskStatus status) {
        Objects.requireNonNull(
                status,
                "Статус не должен быть null"
        );

        return repository.findAll().stream()
                .filter(task -> task.getStatus() == status)
                .sorted(Comparator.comparingLong(Task::getId))
                .toList();
    }

    public List<Task> getTaskByPriority(){
        return repository.findAll().stream()
                .sorted(Comparator.comparing(Task::getPriority)
                        .reversed()
                        .thenComparing(Task::getId))
                .toList();
    }
}
