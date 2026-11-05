package ru.learning.taskmanager.service;

import ru.learning.taskmanager.exception.UserNotFoundException;
import ru.learning.taskmanager.model.Task;
import ru.learning.taskmanager.model.User;
import ru.learning.taskmanager.repository.UserRepository;
import ru.learning.taskmanager.repository.TaskRepository;
import ru.learning.taskmanager.repository.ProjectRepository;

import java.util.List;
import java.util.Objects;

public class UserService {

    private final UserRepository repository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private long nextId;

    public UserService(
            UserRepository repository,
            TaskRepository taskRepository,
            ProjectRepository projectRepository
    ){
        Objects.requireNonNull(
                repository,
                "Репозиторий не может быть null"
        );

        this.projectRepository = Objects.requireNonNull(
                projectRepository,
                "Хранилище проектов не должно быть null"
        );

        this.taskRepository = Objects.requireNonNull(
                taskRepository
        );

        this.repository = repository;

        long maxId = 0L;

        for (User user : repository.findAll()){
            if (user.getId() > maxId){
                maxId = user.getId();
            }
        }

        nextId = maxId + 1;
    }

    public User createUser(String name){
        User user = new User(nextId, name);

        repository.save(user);

        nextId++;

        return user;
    }

    public User getUser(long id){
        return repository.findById(id)
                .orElseThrow(
                        () -> new UserNotFoundException(id)
                );
    }

    public List<User> getAllUsers(){
        return repository.findAll();
    }

    public void renameUser(long id, String newName){
        User user = getUser(id);

        user.rename(newName);

        repository.save(user);
    }

    public void deleteUser(long id){
        getUser(id);

        boolean participatesInProjects = projectRepository.findAll().stream()
                .anyMatch(project -> project.hasMember(id));

        if (participatesInProjects) {
            throw new IllegalStateException(
                    "Сначала исключите пользователя из всех проектов"
            );
        }

        boolean hasAssignedTasks = taskRepository
                .findAll()
                .stream()
                .anyMatch(task ->
                        Objects.equals(
                                task.getAssigneeId(),
                                id));

        if (hasAssignedTasks){
            throw new IllegalStateException(
                    "Сначала снимите с пользователя все назначения"
            );
        }

        boolean deleted = repository.deleteById(id);

        if (!deleted){
            throw new UserNotFoundException(id);
        }
    }
}
