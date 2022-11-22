package hu.kszi2.nought.gui;

import hu.kszi2.nought.core.TodoBuilder;
import hu.kszi2.nought.core.TodoStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TodoTreeTest {
    @BeforeEach
    void setUp() throws Exception {
        store = new TodoStore();
        builder = store.newBuilder();
        store.add(builder.newId().setName("A").setDescription("B").build());
        tree = new TodoTree(store);
    }

    @Test
    void addRootTodoAddsRootTodoToStore() throws Exception {
        var todo = builder.newId().setName("A").setDescription("B").build();
        tree.addRootTodo(todo);

        var it = store.iterator();
        while (it.hasNext()) {
            var stored = it.next();
            assertTrue(stored.getChildren().isEmpty());
        }
    }

    TodoTree tree;
    TodoStore store;
    TodoBuilder builder;
}
