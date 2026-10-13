package ru.learning.taskmanager.repository;

import ru.learning.taskmanager.model.Task;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {

    void save(Task task);
    Optional<Task> findById(long id);
    List<Task> findAll();
    boolean deleteBeId(long id);
}
