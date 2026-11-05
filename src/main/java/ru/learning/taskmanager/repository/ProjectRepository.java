package ru.learning.taskmanager.repository;

import ru.learning.taskmanager.model.Project;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository {

    void save(Project project);

    Optional<Project> findById(long id);

    List<Project> findAll();

    boolean deleteById(long id);
}
