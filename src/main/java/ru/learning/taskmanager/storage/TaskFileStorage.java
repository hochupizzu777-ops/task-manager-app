package ru.learning.taskmanager.storage;

import ru.learning.taskmanager.model.Priority;
import ru.learning.taskmanager.model.Task;
import ru.learning.taskmanager.model.TaskStatus;
import ru.learning.taskmanager.model.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class TaskFileStorage {

    private final Path file;

    public TaskFileStorage(Path file){
        this.file = Objects.requireNonNull(
                file,
                "Путь к файлу не должен быть null"
        ).toAbsolutePath();
    }

    public void save(List<Task> tasks, List<User> users) throws IOException {
        Objects.requireNonNull(
                tasks,
                "Нельзя записать список задач равным null"
        );

        Objects.requireNonNull(
                users,
                "Нельзя записать список пользователей равным null"
        );

        validateAssignments(tasks, users);

        Properties data = new Properties();

        data.setProperty(
                "user.count",
                Integer.toString(users.size())
        );

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            String prefix = "user." + i + ".";

            data.setProperty(
                    prefix + "id",
                    Long.toString(user.getId())
            );

            data.setProperty(
                    prefix + "name",
                    user.getName()
            );
        }

        data.setProperty(
                "count",
                Integer.toString(tasks.size())
        );

        for (int i = 0; i < tasks.size(); i++){
            Task task = tasks.get(i);
            String prefix = "task." + i + ".";

            data.setProperty(
                    prefix + "id",
                    Long.toString(task.getId())
            );

            data.setProperty(
                    prefix + "title",
                    task.getTitle()
            );

            data.setProperty(
                    prefix + "priority",
                    task.getPriority().name()
            );

            data.setProperty(
                    prefix + "status",
                    task.getStatus().name()
            );

            if (task.getAssigneeId() != null) {
                data.setProperty(
                        prefix + "assigneeId",
                        Long.toString(task.getAssigneeId())
                );
            }

            if (task.getDueDate() != null){
                data.setProperty(
                        prefix + "dueData",
                        task.getDueDate().toString()
                );
            }
        }

        Path directory = file.getParent();

        Files.createDirectories(directory);

        Path temporaryFile = Files.createTempFile(
                directory,
                "tasks-",
                ".tmp"
        );

        try{
            try(BufferedWriter writer
                        = Files.newBufferedWriter(
                                temporaryFile,
                                StandardCharsets.UTF_8
            )){
                data.store(
                        writer,
                        "Task manager data"
                );
            }
            try {
                Files.move(
                        temporaryFile,
                        file,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );
            }catch (AtomicMoveNotSupportedException exception){
                Files.move(
                        temporaryFile,
                        file,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }
        }finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    private String requiredProperty(Properties data, String key){
        String value = data.getProperty(key);

        if(value == null){
            throw new IllegalArgumentException(
                    "Отсутствует поле " + key
            );
        }

        return value;
    }

    public ApplicationData load() throws IOException {
        if (Files.notExists(file)) {
            return new ApplicationData(
                    new ArrayList<>(),
                    new ArrayList<>()
            );
        }

        Properties data = new Properties();

        try(BufferedReader reader = Files.newBufferedReader(
                file,
                StandardCharsets.UTF_8
        )){
            data.load(reader);

            int count = Integer.parseInt(requiredProperty(data, "count"));

            if (count < 0){
                throw new IllegalArgumentException(
                        "Количество задач не может быть отрицательным"
                );
            }

            List<Task> tasks = new ArrayList<>();
            Set<Long> ids =new HashSet<>();

            for (int i = 0; i < count; i++){
                String prefix = "task." + i + ".";

                long id = Long.parseLong(
                        requiredProperty(
                                data,
                                prefix + "id"
                        )
                );

                if (!ids.add(id)){
                    throw new IllegalArgumentException(
                            "Повторяется идентификатор" + id
                    );
                }

                String title = requiredProperty(
                        data,
                        prefix + "title"
                );

                Priority priority = Priority.valueOf(
                        requiredProperty(
                                data,
                                prefix + "priority"
                        )
                );

                TaskStatus status = TaskStatus.valueOf(
                        requiredProperty(
                                data,
                                prefix + "status"
                        )
                );

                Task task = new Task(
                        id,
                        title,
                        priority
                );
                task.changeStatus(status);

                String dueDataText = data.getProperty(prefix + "dueData");
                if (dueDataText != null){
                    task.changeDueDate(LocalDate.parse(dueDataText));
                }

                String assigneeText = data.getProperty(
                        prefix + "assigneeId"
                );

                if (assigneeText != null) {
                    task.assignTo(Long.parseLong(assigneeText));
                }

                tasks.add(task);
            }

            List<User> users = loadUsers(data);

            validateAssignments(tasks, users);

            return new ApplicationData(tasks, users);
        }catch (IllegalArgumentException | DateTimeParseException exception){
            throw new IOException(
                    "некоректные данные задач" + exception.getMessage(),
                    exception
            );
        }
    }

    private void validateAssignments(
            List<Task> tasks,
            List<User> users
    ){
        Set<Long> userIds = new HashSet<>();

        for (User user : users){
            if (!userIds.add(user.getId())){
                throw new IllegalArgumentException(
                        "Повторяется идентификатор пользователя "
                                + user.getId()
                );
            }
        }

        for (Task task : tasks){
            Long assigneeId = task.getAssigneeId();

            if (assigneeId != null
                    && !userIds.contains(assigneeId))
            {
                throw new IllegalArgumentException(
                        "У задачи " + task.getId()
                                + " указан отсутствующий исполнитель "
                                + assigneeId
                );
            }
        }
    }

    private List<User> loadUsers(Properties data) {
        int count = Integer.parseInt(
                data.getProperty("user.count", "0")
        );

        if (count < 0) {
            throw new IllegalArgumentException(
                    "Количество пользователей не должно быть отрицательным"
            );
        }

        List<User> users = new ArrayList<>();

        for (int index = 0; index < count; index++) {
            String prefix = "user." + index + ".";

            long id = Long.parseLong(
                    requiredProperty(data, prefix + "id")
            );

            String name = requiredProperty(
                    data,
                    prefix + "name"
            );

            users.add(new User(id, name));
        }

        return users;
    }
}
