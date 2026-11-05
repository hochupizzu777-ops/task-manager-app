package ru.learning.taskmanager.service;

import ru.learning.taskmanager.exception.TaskNotFoundException;
import ru.learning.taskmanager.exception.UserNotFoundException;
import ru.learning.taskmanager.model.Task;
import ru.learning.taskmanager.model.User;
import ru.learning.taskmanager.repository.TaskRepository;
import ru.learning.taskmanager.repository.UserRepository;
import ru.learning.taskmanager.model.Project;
import ru.learning.taskmanager.repository.ProjectRepository;
import ru.learning.taskmanager.exception.ProjectNotFoundException;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class TaskAssignmentService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public TaskAssignmentService(
            TaskRepository taskRepository,
            UserRepository userRepository,
            ProjectRepository projectRepository
    ){
        this.taskRepository =
                Objects.requireNonNull(
                        taskRepository,
                        "Хранилище задач не может быть null"
                );

        this.userRepository =
                Objects.requireNonNull(
                        userRepository,
                        "Хранилище пользователей не может быть null"
                );

        this.projectRepository = Objects.requireNonNull(
                projectRepository,
                "Хранилище проектов не должно быть null"
        );
    }

    public void assignTask(long taskId, long userId){
        Task task = taskRepository.findById(taskId)
                .orElseThrow(
                        () -> new TaskNotFoundException(taskId)
                );

        User user = userRepository.findById(userId)
                .orElseThrow(
                        () -> new UserNotFoundException(userId)
                );

        Long projectId = task.getProjectId();

        if (projectId != null) {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(
                            () -> new ProjectNotFoundException(projectId)
                    );

            if (!project.hasMember(userId)) {
                throw new IllegalStateException(
                        "Исполнитель должен быть участником проекта"
                );
            }
        }

        task.assignTo(user.getId());

        taskRepository.save(task);
    }

    public void unassignTask(long taskId){
        Task task = taskRepository.findById(taskId)
                .orElseThrow(
                        () -> new TaskNotFoundException(taskId)
                );

        task.unassign();

        taskRepository.save(task);
    }

    public List<Task> getTasksByUser(long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(
                        () -> new UserNotFoundException(userId)
                );

        return taskRepository.findAll().stream()
                .filter(task -> Objects.equals(
                        task.getAssigneeId(),
                        user.getId()
                ))
                .sorted(Comparator.comparingLong(Task::getId))
                .toList();

    }
}
