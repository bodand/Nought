package hu.kszi2.nought.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TodoStoreTests {
    @BeforeEach
    void makeEmptyTodoStore() {
        store = new TodoStore();
    }

    @Test
    void storeCanCreateTodoBuilder() {
        var builder = store.newBuilder();

        assertNotNull(builder);
    }

    @Test
    void storeCanAddTodo() throws Exception {
        var builder = store.newBuilder();
        var todo = builder
                .newId()
                .setName("what")
                .setDescription("ever")
                .build();

        assertDoesNotThrow(() -> store.add(todo));
    }

    @Test
    void storeThrowsOnFindingNonexistentTodo() {
        var badId = UUID.randomUUID();
        assertThrows(NoSuchElementException.class,
                () -> store.findById(badId));
    }

    @Test
    void storeReturnsExistingTodo() throws Exception {
        var id = UUID.randomUUID();
        var builder = store.newBuilder();
        var todo = builder
                .setId(id)
                .setName("what")
                .setDescription("ever")
                .build();

        assertDoesNotThrow(() -> store.add(todo));
        var found = assertDoesNotThrow(() -> store.findById(id));
        assertEquals("what", found.getName());
        assertEquals("ever", found.getDescription());
    }

    private TodoStore store;
}
