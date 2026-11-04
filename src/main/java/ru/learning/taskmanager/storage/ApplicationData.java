package ru.learning.taskmanager.storage;

import ru.learning.taskmanager.model.Task;
import ru.learning.taskmanager.model.User;

import java.util.List;

public record ApplicationData(
        List<Task> tasks,
        List<User> users
) {

}
