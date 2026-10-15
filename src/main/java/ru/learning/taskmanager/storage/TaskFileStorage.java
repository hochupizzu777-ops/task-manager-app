package ru.learning.taskmanager.storage;

import ru.learning.taskmanager.model.Priority;
import ru.learning.taskmanager.model.Task;
import ru.learning.taskmanager.model.TaskStatus;

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

public class TaskFileStorage {

    private final Path file;

    public TaskFileStorage(Path file){
        this.file = Objects.requireNonNull(
                file,
                "Путь к файлу не должен быть null"
        ).toAbsolutePath();
    }

    public void save(List<Task> tasks) throws IOException {
        Objects.requireNonNull(
                tasks,
                "Нельзя записать список задач равный null"
        );

        Properties data = new Properties();

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

    public List<Task> load() throws IOException {
        if(Files.notExists(file)){
            return new ArrayList<>();
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
                tasks.add(task);
            }

            return tasks;
        }catch (IllegalArgumentException exception){
            throw new IOException(
                    "некоректные данные задач" + exception.getMessage(),
                    exception
            );
        }
    }
}
