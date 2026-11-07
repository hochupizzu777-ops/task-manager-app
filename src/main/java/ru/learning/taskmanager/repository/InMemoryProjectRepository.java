package ru.learning.taskmanager.repository;

import ru.learning.taskmanager.model.Project;

import java.util.*;

public class InMemoryProjectRepository implements ProjectRepository {

    private final Map<Long, Project> projects =  new HashMap<>();

    @Override
    public void save(Project project) {
        Objects.requireNonNull(
                project,
                "Проект не может быть null"
        );

        projects.put(project.getId(), project);
    }

    @Override
    public Optional<Project> findById(long id){
        validateId(id);

        return Optional.ofNullable(projects.get(id));
    }

    @Override
    public List<Project> findAll(){
        return new ArrayList<>(projects.values());
    }

    @Override
    public boolean deleteById(long id){
        validateId(id);

        return projects.remove(id) != null;
    }

    private void validateId(long id){
        if(id <= 0){
            throw new IllegalArgumentException(
                    "Недопустимое значение индентификатора"
            );
        }
    }
}
