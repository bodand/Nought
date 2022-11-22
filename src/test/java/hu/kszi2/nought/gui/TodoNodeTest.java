package hu.kszi2.nought.gui;

import hu.kszi2.nought.core.BadTodoOperation;
import hu.kszi2.nought.core.Todo;
import hu.kszi2.nought.core.TodoStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TodoNodeTest {
    @BeforeEach
    void setUp() throws Exception {
        var store = new TodoStore();
        var builder = store.newBuilder();
        todo = builder.newId().setName("A").setDescription("DESC").build();
        child = builder.newId().setName("B").build();
        node = new TodoNode(todo);
    }

    @Test
    void addWithLinkingEnabledAddsChildToStoredTodo() {
        node.add(new TodoNode(child), true);
        assertTrue(todo.getChildren().contains(child.getId()));
    }

    @Test
    void addWithLinkingDisabledDoesNotAddChildToStoredTodo() {
        node.add(new TodoNode(child), false);
        assertFalse(todo.getChildren().contains(child.getId()));
    }

    @Test
    void getTodoReturnsSameTodoObjectAsWasPassed() {
        assertSame(node.getTodo(), todo);
    }

    @Test
    void completedTodoReturnsFalseForIncompleteTodo() {
        assertFalse(node.completedTodo());
    }

    @Test
    void completedTodoReturnsTrueForCompleteTodo() throws Exception {
        todo.setCompleted(true);
        assertTrue(node.completedTodo());
    }

    @Test
    void testToStringIsEqualToTodoObjectsName() {
        assertEquals(node.toString(), todo.getName());
    }

    @Test
    void todoNodeWithNonTodoObjectReturnsNullForGetTodo() {
        var susNode = new TodoNode("sus");
        assertNull(susNode.getTodo());
    }

    Todo todo;
    Todo child;
    TodoNode node;
}