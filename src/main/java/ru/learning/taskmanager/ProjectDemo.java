package ru.learning.taskmanager;

import ru.learning.taskmanager.exception.UserNotFoundException;
import ru.learning.taskmanager.model.Project;
import ru.learning.taskmanager.model.User;
import ru.learning.taskmanager.repository.*;
import ru.learning.taskmanager.service.ProjectService;

public class ProjectDemo {

    public static void main(String[] args) {
        ProjectRepository projectRepository =
                new InMemoryProjectRepository();

        UserRepository userRepository =
                new InMemoryUserRepository();

        TaskRepository taskRepository =
                new InMemoryTaskRepository();


        userRepository.save(new User(10, "Максим"));
        userRepository.save(new User(20, "Анна"));

        ProjectService service = new ProjectService(
                projectRepository,
                userRepository,
                taskRepository
        );

        Project project = service.createProject(
                "Подготовка к собеседованию"
        );

        System.out.println(
                service.addMember(project.getId(), 10)
        ); // true

        System.out.println(
                service.addMember(project.getId(), 10)
        ); // false

        service.addMember(project.getId(), 20);

        System.out.println("Участники проекта:");

        service.getMembers(project.getId())
                .forEach(System.out::println);

        service.renameProject(
                project.getId(),
                "Подготовка к Java Junior"
        );

        System.out.println(service.getProject(project.getId()));

        try {
            service.addMember(project.getId(), 999);
        } catch (UserNotFoundException exception) {
            System.out.println(exception.getMessage());
        }

        System.out.println(
                "Участников осталось: "
                        + service.getMembers(project.getId()).size()
        ); // 2
    }
}