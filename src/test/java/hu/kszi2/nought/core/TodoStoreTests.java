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
    void storeCanCreateTodoImporter() {
        var in = store.newImporter();

        assertNotNull(in);
    }

    @Test
    void storeCanCreateIterator() {
        var it = store.iterator();

        assertNotNull(it);
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

    @Test
    void removeSingleTodoRemovesChildlessTodo() throws Exception {
        var id = UUID.randomUUID();
        var builder = store.newBuilder();
        var todo = builder
                .setId(id)
                .setName("what")
                .setDescription("ever")
                .build();

        store.add(todo);
        assertDoesNotThrow(() -> store.removeById(id));
        assertThrows(NoSuchElementException.class,
                () -> store.findById(id));
    }

    @Test
    void removeSingleTodoThrowsForTodoWithChildren() throws Exception {
        var id = UUID.randomUUID();
        var builder = store.newBuilder();
        var todoChild = builder
                .newId()
                .setName("child")
                .setDescription("desc")
                .build();
        var todo = builder
                .setId(id)
                .setName("what")
                .setDescription("ever")
                .addChild(todoChild.getId())
                .build();

        store.add(todoChild);
        store.add(todo);
        assertThrows(BadTodoOperation.class, () -> store.removeById(id));
    }

    @Test
    void removeBranchRemovesChildlessTodo() throws Exception {
        var id = UUID.randomUUID();
        var builder = store.newBuilder();
        var todo = builder
                .setId(id)
                .setName("what")
                .setDescription("ever")
                .build();

        store.add(todo);
        assertDoesNotThrow(() -> store.removeBranchAtId(id));
        assertThrows(NoSuchElementException.class,
                () -> store.findById(id));
    }

    @Test
    void removeBranchRemovesForTodoWithChildren() throws Exception {
        var id = UUID.randomUUID();
        var builder = store.newBuilder();
        var todoChild = builder
                .newId()
                .setName("child")
                .setDescription("desc")
                .build();
        var todo = builder
                .setId(id)
                .setName("what")
                .setDescription("ever")
                .addChild(todoChild.getId())
                .build();
        var cid = todoChild.getId();
        store.add(todoChild);
        store.add(todo);

        assertDoesNotThrow(() -> store.removeBranchAtId(id));

        assertThrows(NoSuchElementException.class,
                () -> store.findById(id));
        assertThrows(NoSuchElementException.class,
                () -> store.findById(cid));
    }

    private TodoStore store;
}
