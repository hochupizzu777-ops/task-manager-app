package ru.learning.taskmanager.model.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.learning.taskmanager.exception.ProjectNotFoundException;
import ru.learning.taskmanager.exception.UserNotFoundException;
import ru.learning.taskmanager.model.Priority;
import ru.learning.taskmanager.model.Project;
import ru.learning.taskmanager.model.Task;
import ru.learning.taskmanager.model.User;
import ru.learning.taskmanager.repository.InMemoryProjectRepository;
import ru.learning.taskmanager.repository.InMemoryTaskRepository;
import ru.learning.taskmanager.repository.InMemoryUserRepository;
import ru.learning.taskmanager.repository.ProjectRepository;
import ru.learning.taskmanager.repository.TaskRepository;
import ru.learning.taskmanager.repository.UserRepository;
import ru.learning.taskmanager.service.ProjectService;
import ru.learning.taskmanager.service.TaskAssignmentService;
import ru.learning.taskmanager.service.TaskService;
import ru.learning.taskmanager.service.UserService;

import static org.junit.jupiter.api.Assertions.*;

class ProjectRulesTest {

    private ProjectService projects;
    private UserService users;
    private TaskAssignmentService assignments;

    private Project project;
    private Task task;
    private User member;
    private User outsider;

    @BeforeEach
    void setUp() {
        TaskRepository taskRepository =
                new InMemoryTaskRepository();

        UserRepository userRepository =
                new InMemoryUserRepository();

        ProjectRepository projectRepository =
                new InMemoryProjectRepository();

        projects = new ProjectService(
                projectRepository,
                userRepository,
                taskRepository
        );

        users = new UserService(
                userRepository,
                taskRepository,
                projectRepository
        );

        assignments = new TaskAssignmentService(
                taskRepository,
                userRepository,
                projectRepository
        );

        member = users.createUser("Анна");
        outsider = users.createUser("Максим");

        project = projects.createProject("Изучение Java");
        projects.addMember(project.getId(), member.getId());

        TaskService taskService = new TaskService(taskRepository);

        task = taskService.createTask(
                "Повторить коллекции",
                Priority.HIGH
        );

        projects.addTaskToProject(
                project.getId(),
                task.getId()
        );
    }

    @Test
    void rejectedAssignmentShouldPreserveCurrentAssignee() {
        assignments.assignTask(task.getId(), member.getId());

        assertThrows(
                IllegalStateException.class,
                () -> assignments.assignTask(
                        task.getId(),
                        outsider.getId()
                )
        );

        assertEquals(
                Long.valueOf(member.getId()),
                task.getAssigneeId()
        );
    }

    @Test
    void rejectedMoveShouldPreserveOriginalProject() {
        assignments.assignTask(task.getId(), member.getId());

        Project anotherProject =
                projects.createProject("Другой проект");

        assertThrows(
                IllegalStateException.class,
                () -> projects.addTaskToProject(
                        anotherProject.getId(),
                        task.getId()
                )
        );

        assertEquals(
                Long.valueOf(project.getId()),
                task.getProjectId()
        );
    }

    @Test
    void shouldRejectRemovingMemberWithAssignedTasks() {
        assignments.assignTask(task.getId(), member.getId());

        assertThrows(
                IllegalStateException.class,
                () -> projects.removeMember(
                        project.getId(),
                        member.getId()
                )
        );

        assertTrue(project.hasMember(member.getId()));
    }

    @Test
    void shouldRejectDeletingProjectWithTasks() {
        assertThrows(
                IllegalStateException.class,
                () -> projects.deleteProject(project.getId())
        );

        assertEquals(
                project.getId(),
                projects.getProject(project.getId()).getId()
        );
    }

    @Test
    void shouldRejectDeletingMemberWithoutAssignments() {
        assertNull(task.getAssigneeId());

        assertThrows(
                IllegalStateException.class,
                () -> users.deleteUser(member.getId())
        );

        assertEquals(
                member.getId(),
                users.getUser(member.getId()).getId()
        );
    }

    @Test
    void shouldAllowCleanupInCorrectOrder() {
        assignments.assignTask(task.getId(), member.getId());

        assignments.unassignTask(task.getId());

        assertTrue(
                projects.removeMember(
                        project.getId(),
                        member.getId()
                )
        );

        users.deleteUser(member.getId());

        projects.removeTaskFromProject(task.getId());
        projects.deleteProject(project.getId());

        assertNull(task.getAssigneeId());
        assertNull(task.getProjectId());

        assertThrows(
                UserNotFoundException.class,
                () -> users.getUser(member.getId())
        );

        assertThrows(
                ProjectNotFoundException.class,
                () -> projects.getProject(project.getId())
        );

        assignments.assignTask(task.getId(), outsider.getId());

        assertEquals(
                Long.valueOf(outsider.getId()),
                task.getAssigneeId()
        );
    }
}