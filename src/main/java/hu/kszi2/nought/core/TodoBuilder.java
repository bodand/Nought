package hu.kszi2.nought.core;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class TodoBuilder implements Serializable {
    TodoBuilder(TodoStore store) {
        this.store = store;
    }

    public Todo build() throws IllegalArgumentException, BadTodoOperation {
        if (id == null
                || name == null
                || desc == null) {
            throw new IllegalArgumentException("required setters have not been called");
        }

        var todo = new Todo(store,
                id,
                name,
                desc,
                completed,
                dueDate,
                dueTime,
                null);
        for (var child : children) {
            todo.addChild(child);
        }
        return todo;
    }

    public TodoBuilder newId() {
        return setId(UUID.randomUUID());
    }

    public TodoBuilder setId(UUID id) {
        this.id = id;
        return this;
    }

    public TodoBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public TodoBuilder setDescription(String desc) {
        this.desc = desc;
        return this;
    }

    public TodoBuilder setCompleted(boolean done) {
        completed = done;
        return this;
    }

    public TodoBuilder setDueDate(Date dueDate) {
        this.dueDate = dueDate;
        return this;
    }

    public TodoBuilder setDueTime(LocalTime time) {
        this.dueTime = time;
        return this;
    }

    public TodoBuilder addChild(UUID cid) {
        children.add(cid);
        return this;
    }

    private UUID id;
    private String name;
    private String desc;
    private boolean completed;
    private Date dueDate;
    private LocalTime dueTime;
    private final List<UUID> children = new ArrayList<>();
    private final TodoStore store;
}
