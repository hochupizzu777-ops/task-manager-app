package ru.learning.taskmanager.model;

import java.util.HashSet;
import java.util.Set;

public class Project {
    private final long id;
    private String name;
    private final Set<Long> memberIds = new HashSet<>();

    public Project(long id, String name){
        validateIds(id);

        this.id = id;

        rename(name);
    }

    public long getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public Set<Long> getMemberIds(){
        return Set.copyOf(memberIds);
    }

    public boolean addMember(long userId){
        validateIds(userId);

        return memberIds.add(userId);
    }

    public boolean removeMember(long userId){
        validateIds(userId);

        return memberIds.remove(userId);
    }

    public boolean hasMember(long userId){
        validateIds(userId);

        return memberIds.contains(userId);
    }

    private void validateIds(long id){
        if (id <= 0){
            throw new IllegalArgumentException(
                    "Недопустимое значение индентификатора"
            );
        }
    }

    public void rename(String name){
        if (name == null || name.isBlank()){
            throw new IllegalArgumentException(
                    "Недопустимое название проекта"
            );
        }

        this.name = name.strip();
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", memberIds=" + memberIds +
                '}';
    }
}
