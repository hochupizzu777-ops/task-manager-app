package ru.learning.taskmanager.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {
    private static final LocalDate TODAY =
            LocalDate.of(2026, 11, 4);

    @Test
    void shouldBeOverdueWhenDeadlineHasPassed() {
        Task task = new Task(1, "Изучить Java", Priority.HIGH);
        task.changeDueDate(TODAY.minusDays(1));

        assertTrue(task.isOverdue(TODAY));
    }

    @Test
    void shouldNotBeOverdueOnDeadline() {
        Task task = new Task(1,"Изучить Java", Priority.HIGH);
        task.changeDueDate(TODAY);

        assertFalse(task.isOverdue(TODAY));
    }

    @Test
    void completedTaskShouldNotBeOverdue(){
        Task task = new Task(1, "Изучить Java", Priority.HIGH);
        task.changeDueDate(TODAY.minusDays(1));
        task.changeStatus(TaskStatus.DONE);

        assertFalse(task.isOverdue(TODAY));
    }

    @Test
    void taskWithoutDeadlineShouldNotBeOverdue(){
        Task task = new Task(1, "Изучить Java", Priority.HIGH);

        assertFalse(task.isOverdue(TODAY));
    }

    @Test
    void renameShouldPreserveStatusAndDeadline(){
        Task task = new Task(1, "Изучить Java", Priority.HIGH);
        task.changeStatus(TaskStatus.IN_PROGRESS);
        task.changeDueDate(TODAY);

        task.rename("New Name");

        assertAll(
                () -> assertEquals("New Name", task.getTitle()),
                () -> assertEquals(
                        TaskStatus.IN_PROGRESS,
                        task.getStatus()
                ),
                () -> assertEquals(TODAY, task.getDueDate()),
                () -> assertEquals(1L, task.getId())
        );
    }


}
