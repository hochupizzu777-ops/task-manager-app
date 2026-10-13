package ru.learning.taskmanager.exception;

public class TaskNotFoundException extends RuntimeException{

    public TaskNotFoundException(long id){
        super("Задача с инентификатором " + id + " не найдена");
    }
}
