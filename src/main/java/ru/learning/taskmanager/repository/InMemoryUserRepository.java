package ru.learning.taskmanager.repository;

import ru.learning.taskmanager.model.User;

import java.util.*;

public class InMemoryUserRepository implements UserRepository{

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public void save(User user){
        Objects.requireNonNull(
                user,
                "Пользователь не может быть null"
        );

        users.put(user.getId(), user);
    }

    @Override
    public Optional<User> findById(long id){
        validateId(id);

        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> findAll(){
        return new ArrayList<>(users.values());
    }

    @Override
    public boolean deleteById(long id){
        validateId(id);

        return users.remove(id) != null;
    }

    private void validateId(long id){
        if (id <= 0){
            throw new IllegalArgumentException(
                    "Индентификатор должен быть положительным"
            );
        }
    }
}
