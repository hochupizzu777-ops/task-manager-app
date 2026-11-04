package ru.learning.taskmanager.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(long id) {
        super(
                "Пользователь с идентификатором "
                        + id + " не найден"
        );
    }
}
