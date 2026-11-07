package ru.learning.taskmanager.exception;

public class ProjectNotFoundException extends RuntimeException {
    public ProjectNotFoundException(long id) {
        super(
                "Проект с идентификатором "
                        + id + " не найден"
        );
    }
}
