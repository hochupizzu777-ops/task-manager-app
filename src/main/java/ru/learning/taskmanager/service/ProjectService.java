package ru.learning.taskmanager.service;

import ru.learning.taskmanager.exception.ProjectNotFoundException;
import ru.learning.taskmanager.exception.TaskNotFoundException;
import ru.learning.taskmanager.exception.UserNotFoundException;
import ru.learning.taskmanager.model.Project;
import ru.learning.taskmanager.model.User;
import ru.learning.taskmanager.repository.ProjectRepository;
import ru.learning.taskmanager.repository.UserRepository;
import ru.learning.taskmanager.model.Task;
import ru.learning.taskmanager.exception.TaskNotFoundException;
import ru.learning.taskmanager.repository.TaskRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private long nextId;
    private final TaskRepository taskRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            UserRepository userRepository,
            TaskRepository taskRepository
    ){
        this.projectRepository = Objects.requireNonNull(
                projectRepository,
                "Хранилище проектов не может быть null"
        );

        this.userRepository = Objects.requireNonNull(
                userRepository,
                "Хранилище исполнителей не иожет быть null"
        );

        this.taskRepository = Objects.requireNonNull(
                taskRepository,
                "Хранилище исполнителей не иожет быть null"
        );

        long maxId = 0;

        for (Project project : projectRepository.findAll()){
            if (project.getId() > maxId){
                maxId = project.getId();
            }
        }
        nextId = maxId + 1;
    }

    public Project createProject(String name){
        Project project = new Project(nextId, name);

        projectRepository.save(project);
        nextId++;

        return project;
    }

    public Project getProject(long id){
        Project project = projectRepository.findById(id)
                .orElseThrow(
                        () -> new ProjectNotFoundException(id)
                );

        return project;
    }

    public boolean addMember(long projectId, long userId){
        Project project = getProject(projectId);

        User user = userRepository.findById(userId)
                .orElseThrow(
                        () -> new UserNotFoundException(userId)
                );

        boolean added = project.addMember(user.getId());

        projectRepository.save(project);

        return added;
    }

    public List<Project> getAllProjects(){
        List<Project> projects = projectRepository.findAll();

        return projects.stream()
                .sorted(Comparator.comparingLong(Project::getId))
                .toList();
    }

    public void renameProject(long id, String newName){
        Project project = getProject(id);

        project.rename(newName);

        projectRepository.save(project);
    }

    public List<User> getMembers(long projectId){
        Project project = getProject(projectId);

        Set<Long> usersIds = project.getMemberIds();

        return usersIds.stream()
                .map(userId -> userRepository.findById(userId)
                        .orElseThrow(
                                () -> new UserNotFoundException(userId)
                        ))
                .sorted(Comparator.comparingLong(User::getId))
                .toList();
    }

    private Task getTask(long taskId){
        return taskRepository.findById(taskId)
                .orElseThrow(
                        () -> new TaskNotFoundException(taskId)
                );
    }

    public void addTaskToProject(long projectId, long taskId){
        Task task = getTask(taskId);
        Project project = getProject(projectId);

        Long assigneeId = task.getAssigneeId();

        if (assigneeId != null && !project.hasMember(assigneeId)) {
            throw new IllegalStateException(
                    "Исполнитель задачи должен быть участником проекта"
            );
        }

        task.attachToProject(projectId);
        taskRepository.save(task);
    }

    public void removeTaskFromProject(long taskId){
        Task task = getTask(taskId);

        task.detachFromProject();

        taskRepository.save(task);
    }

    public List<Task> getTasksByProject(long projectId){
        getProject(projectId);

        return taskRepository.findAll().stream()
                .filter(task -> Objects.equals(
                        task.getProjectId(),
                        projectId
                ))
                .sorted(Comparator.comparingLong(Task::getId))
                .toList();
    }

    public boolean removeMember(long projectId, long userId) {
        Project project = getProject(projectId);

        if (!project.hasMember(userId)) {
            return false;
        }

        boolean hasAssignedTasks = taskRepository.findAll().stream()
                .anyMatch(task ->
                        Objects.equals(task.getProjectId(), projectId)
                                && Objects.equals(
                                task.getAssigneeId(),
                                userId
                        )
                );

        if (hasAssignedTasks) {
            throw new IllegalStateException(
                    "Сначала снимите с участника назначения в этом проекте"
            );
        }

        project.removeMember(userId);
        projectRepository.save(project);

        return true;
    }

    public void deleteProject(long projectId) {
        getProject(projectId);

        boolean hasTasks = taskRepository.findAll().stream()
                .anyMatch(task -> Objects.equals(
                        task.getProjectId(),
                        projectId
                ));

        if (hasTasks) {
            throw new IllegalStateException(
                    "Сначала удалите или перенесите задачи проекта"
            );
        }

        if (!projectRepository.deleteById(projectId)) {
            throw new ProjectNotFoundException(projectId);
        }
    }
}
