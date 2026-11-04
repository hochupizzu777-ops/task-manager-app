package ru.learning.taskmanager.model;

import java.util.Objects;
import java.time.LocalDate;

public class Task {

    private final long id;
    private String title;
    private TaskStatus status;
    private Priority priority;
    private LocalDate dueDate;
    private Long assigneeId;

    public Task(
            long id,
            String title,
            Priority priority
    ){
        if (id <= 0){
            throw new IllegalArgumentException(
                    "Id не может быть меньше или равен нуля"
            );
        }
        if (title == null || title.isBlank()){
            throw new IllegalArgumentException(
                    "Название не может отсутствовать"
            );
        }
        if (priority == null){
            throw new IllegalArgumentException(
                    "Приоритет не может отсутствовать"
            );
        }

        this.id = id;
        rename(title);
        changePriority(priority);
        status = TaskStatus.TODO;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Priority getPriority() {
        return priority;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public LocalDate getDueDate(){
        return dueDate;
    }

    public Long getAssigneeId(){
        return assigneeId;
    }

    public void rename(String newTitle) {
        if (newTitle == null || newTitle.isBlank()) {
            throw new IllegalArgumentException(
                    "Название не должно быть пустым"
            );
        }

        this.title = newTitle.strip();
    }

    public void changePriority(Priority priority){
        this.priority = Objects.requireNonNull(
                priority,
                "Приоритет не должен быть null"
        );
    }

    public void changeStatus(TaskStatus status){
        this.status = Objects.requireNonNull(
                status,
                "Статус не должен быть null"
        );
    }
    public void changeDueDate(LocalDate dueDate){
        this.dueDate = dueDate;
    }

    public boolean isOverdue(LocalDate today){
        Objects.requireNonNull(
                today,
                "Текущая дата не может быть null"
        );

        return dueDate != null
                && status != TaskStatus.DONE
                && dueDate.isBefore(today);
    }

    public void assignTo(long userId){
        if (id <= 0){
            throw new IllegalArgumentException(
                    "Идентификатор исполнителя должен быть положительным"
            );
        }

        this.assigneeId = userId;
    }

    public void unassign(){
        this.assigneeId = null;
    }

    @Override
    public String toString(){
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", priority=" + priority +
                '}';
    }
}
