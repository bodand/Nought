package hu.kszi2.nought.gui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RootTodoNodeTest {
    @BeforeEach
    void setUp() {
        node = new RootTodoNode();
    }

    @Test
    void completedTodoReturnsFalse() {
        assertFalse(node.completedTodo());
    }

    @Test
    void testToStringReturnsTodos() {
        assertEquals("Todos", node.toString());
    }

    RootTodoNode node;
}