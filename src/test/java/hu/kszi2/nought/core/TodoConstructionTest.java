package hu.kszi2.nought.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.Calendar;
import java.util.UUID;

class TodoConstructionTest {
    @BeforeEach
    void setCalendarTo2038() {
        calendar.set(2038, Calendar.JANUARY, 19);
    }

    @BeforeEach
    void createStore() {
        store = new TodoStore();
    }

    @Test
    void testTodoCanBeConstructedWithAllTodoDataWithoutParent() throws Exception {
        var todo = new Todo(store,
                UUID.randomUUID(),
                "TodoName",
                "Todo\nDesc",
                false,
                calendar.getTime(),
                LocalTime.of(4, 20),
                null);

        assertNotNull(todo.getId());
        assertEquals("TodoName", todo.getName());
        assertEquals("Todo\nDesc", todo.getDescription());
        assertFalse(todo.isCompleted());
        assertEquals(calendar.getTime(), todo.getDueDate());
        assertEquals(LocalTime.of(4, 20), todo.getDueTime());
        assertNull(todo.getParentId());
    }

    @Test
    void testTodoCanBeConstructedWithParent() throws Exception {
        var parent = new Todo(store,
                UUID.randomUUID(),
                "Parent",
                "ParentDesc",
                false,
                null,
                null,
                null);
        var todo = new Todo(store,
                UUID.randomUUID(),
                "TodoName",
                "Todo\nDesc",
                false,
                calendar.getTime(),
                LocalTime.of(4, 20),
                parent.getId());

        assertNull(parent.getParentId());
        assertNotNull(todo.getParentId());
        assertSame(todo.getParentId(), parent.getId());
    }

    private final Calendar calendar = Calendar.getInstance();
    private TodoStore store;
}
