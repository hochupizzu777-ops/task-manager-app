package ru.learning.taskmanager.repository;

import ru.learning.taskmanager.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class InMemoryTaskRepository  implements TaskRepository{

    private final Map<Long, Task> tasks = new HashMap<>();

    @Override
    public void save(Task task){
        Objects.requireNonNull(
                task,
                "Задача не может быть null"
        );

        tasks.put(task.getId(), task);
    }

    @Override
    public Optional<Task> findById(long id) {
        validateId(id);

        return Optional.ofNullable(tasks.get(id));
    }

    @Override
    public List<Task> findAll(){

        return new ArrayList<>(tasks.values());
    }

    @Override
    public boolean deleteBeId(long id){
        validateId(id);

        return tasks.remove(id) != null;
    }

    public void validateId(long id){
        if (id <= 0){
            throw new IllegalArgumentException(
                    "Id не может быть отрицательным или равным нулю"
            );
        }
    }
}
