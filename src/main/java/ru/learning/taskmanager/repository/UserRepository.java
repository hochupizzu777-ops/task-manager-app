package ru.learning.taskmanager.repository;

import ru.learning.taskmanager.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    void save(User user);

    Optional<User> findById(long id);

    List<User> findAll();

    boolean deleteById(long id);
}
