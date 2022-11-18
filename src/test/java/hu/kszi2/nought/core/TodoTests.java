package hu.kszi2.nought.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import java.time.LocalTime;
import java.util.Calendar;
import java.util.UUID;
import java.util.stream.Stream;

class TodoTests {
    @BeforeEach
    void setUpTodo() throws Exception {
        TodoStore store = new TodoStore();

        var cal = Calendar.getInstance();
        cal.set(2038, Calendar.JANUARY, 19);

        todo = new Todo(store,
                UUID.fromString("6a98fbfc-dfad-42b2-922e-3859c4064c55"),
                "TodoName",
                "Todo\nDesc",
                false,
                null,
                null,
                null);
        child = new Todo(store,
                UUID.randomUUID(),
                "ChildName",
                "Child\nDesc",
                false,
                cal.getTime(),
                LocalTime.of(4, 20),
                todo.getId());
    }

    @Test
    void childCanBeSetCompletedWhileParentIsIncomplete() {
        assertAll(
                () -> assertDoesNotThrow(() -> child.setCompleted(true)),
                () -> assertTrue(child.isCompleted()));
    }

    @Test
    void parentCannotBeSetCompletedWhileChildIsIncomplete() {
        var caught = assertThrows(BadTodoOperation.class, () -> todo.setCompleted(true));

        assertTrue(caught.getMessage().contains(todo.getName()));
    }

    @Test
    void childCannotBeUnsetCompletedIfParentIsComplete() throws Exception {
        child.setCompleted(true);
        todo.setCompleted(true);

        var caught = assertThrows(BadTodoOperation.class, () -> child.setCompleted(false));

        assertTrue(caught.getMessage().contains(todo.getName()));
        assertTrue(caught.getMessage().contains(child.getName()));
    }

    @Test
    void dueTimeCannotBeSetWithoutDate() {
        var caught = assertThrows(BadTodoOperation.class,
                () -> todo.setDueTime(LocalTime.MIDNIGHT));

        assertTrue(caught.getMessage().contains(todo.getName()));
        assertTrue(caught.getMessage().contains("date"));
    }

    @Test
    void dueDateCannotBeUnsetIfTimeIsSet() throws Exception {
        todo.setDueDate(Calendar.getInstance().getTime());
        todo.setDueTime(LocalTime.NOON);

        var caught = assertThrows(BadTodoOperation.class,
                () -> todo.setDueDate(null));

        assertTrue(caught.getMessage().contains(todo.getName()));
        assertTrue(caught.getMessage().contains("date"));
    }

    @ParameterizedTest
    @NullSource
    @MethodSource("equalityTodoProvider")
    void equality(Object compareTo) {
        if (compareTo instanceof Todo compareTodo) {
            if (compareTodo.isCompleted()) {
                assertEquals(compareTodo, todo);
            } else {
                assertNotEquals(compareTodo, todo);
            }
        } else {
            assertNotEquals(compareTo, todo);
        }
    }

    @Test
    void hashOfTodoIsEqualToThatOfItsGUID() {
        assertEquals(todo.getId().hashCode(), todo.hashCode());
    }

    static Stream<Object> equalityTodoProvider() throws Exception {
        var store = new TodoStore();
        var builder = store.newBuilder();
        builder.setName("random").setDescription("text");
        return Stream.of(
                builder.newId().build(),
                builder.setId(UUID.fromString("6a98fbfc-dfad-42b2-922e-3859c4064c55")).setCompleted(true).build(),
                42,
                "DEADBEEF"
        );
    }

    private Todo todo;
    private Todo child;
}
