package ru.learning.taskmanager.model;

public class User {
    private final long id;
    private String name;

    public User(long id, String name){
        if (id <= 0){
            throw new IllegalArgumentException(
                    "Идентификатор должен быть положительным"
            );
        }
        this.id = id;
        rename(name);
    }

    public long getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public void rename(String name){
        if (name == null || name.isBlank()){
            throw new IllegalArgumentException(
                    "Имя пользователя не должно быть пустым"
            );
        }

        this.name = name.strip();
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
