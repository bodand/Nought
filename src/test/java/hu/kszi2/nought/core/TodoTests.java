package hu.kszi2.nought.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.text.ParseException;
import java.time.LocalTime;
import java.time.temporal.TemporalField;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    void dueTimeStringUnsetsTimeIfPassedNull() throws Exception {
        child.setDueTime(((String) null));
        assertNull(child.getDueTime());
    }

    @Test
    void dueTimeStringThrowsIfIsNotOfCorrectFormat() {
        assertThrows(ParseException.class, () -> child.setDueTime("almafa"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"16:20:13", "06:09"})
    void dueTimeStringSetsValueToCorrectIfIsNotOfCorrectFormat(String time) {
        assertDoesNotThrow(() -> child.setDueTime(time));
        assertEquals(time, child.getDueTime().toString());
    }

    @Test
    void dueTimeCannotBeSetWithoutDate() {
        var caught = assertThrows(BadTodoOperation.class,
                () -> todo.setDueTime(LocalTime.MIDNIGHT));

        assertTrue(caught.getMessage().contains(todo.getName()));
        assertTrue(caught.getMessage().contains("date"));
    }

    @Test
    void dueDateStringUnsetsTimeIfPassedNull() throws Exception {
        child.setDueTime(((String) null));
        child.setDueDate(((String) null));
        assertNull(child.getDueDate());
    }

    @Test
    void dueDateStringThrowsIfIsNotOfCorrectFormat() {
        assertThrows(ParseException.class, () -> child.setDueDate("almafa"));
    }

    @SuppressWarnings("deprecation")
    @Test
    void dueDateStringSetsDateIfIsOfCorrectFormat() {
        assertDoesNotThrow(() -> child.setDueDate("2022-12-02"));
        assertEquals(2022, child.getDueDate().getYear() + 1900);
        assertEquals(11, child.getDueDate().getMonth());
        assertEquals(2, child.getDueDate().getDate());
    }

    @Test
    void dueDateCannotBeUnsetIfTimeIsSet() throws Exception {
        todo.setDueDate(Calendar.getInstance().getTime());
        todo.setDueTime(LocalTime.NOON);

        var caught = assertThrows(BadTodoOperation.class,
                () -> todo.setDueDate(((Date) null)));

        assertTrue(caught.getMessage().contains(todo.getName()));
        assertTrue(caught.getMessage().contains("date"));
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {"value", "@&đäˇ$Łäđ[Đ", "<TODO>"})
    void todoSetNameSetsAllowedStringNames(String data) {
        todo.setName(data);
        assertEquals(data, todo.getName());
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {"value", "@&đäˇłÄ@|\n\n$Łäđ[Đ$Ł[$ä", "<TODO>"})
    void todoSetNameSetsAllowedStringDescription(String data) {
        todo.setDescription(data);
        assertEquals(data, todo.getDescription());
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
    void deletingChildTodoRemovesItFromParentTodo() throws Exception {
        child.destroy();
        assertEquals(new ArrayList<UUID>(), todo.getChildren());
    }

    @Test
    void todoComparesFalseToNotTodoObject() {
        assertNotEquals(todo, (Object) "thing");
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
                builder.setId(UUID.fromString("6a98fbfc-dfad-42b2-922e-3859c4064c55")).setCompleted(true).build()
        );
    }

    private Todo todo;
    private Todo child;
}
