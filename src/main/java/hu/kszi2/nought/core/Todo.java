package hu.kszi2.nought.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalTime;
import java.util.*;

public class Todo {
    Todo(@NotNull TodoStore store,
         @NotNull UUID id,
         @NotNull String name,
         @NotNull String desc,
         boolean completed,
         @Nullable Date dueDate,
         @Nullable LocalTime dueTime,
         @Nullable UUID parentId)
            throws BadTodoOperation, NoSuchElementException {
        this.store = store;
        this.id = id;
        this.name = name;
        this.description = desc;
        this.completed = completed;
        setDueDate(dueDate);
        setDueTime(dueTime);
        this.parentId = parentId;
        if (parentId != null) {
            var parent = store.findById(parentId);
            parent.addChild(this);
        }
        store.add(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Todo todo) {
            return todo.getId().equals(id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public @NotNull UUID getId() {
        return id;
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull String getDescription() {
        return description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public @Nullable Date getDueDate() {
        return dueDate;
    }

    public @Nullable LocalTime getDueTime() {
        return dueTime;
    }

    public @Nullable UUID getParentId() {
        return parentId;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    public void setDescription(@NotNull String description) {
        this.description = description;
    }

    public void setCompleted(boolean completedParam) throws BadTodoOperation {
        if (completedParam && children.stream().map(store::findById).anyMatch(todo -> !todo.completed)) {
            throw new BadTodoOperation(this, "Children's completeness is not appropriate");
        }
        if (!completedParam && parentId != null && getParent().completed) {
            throw new BadTodoOperation(this, "Parent's completeness is not appropriate");
        }
        this.completed = completedParam;
    }

    public void setDueDate(@Nullable Date dueDate) throws BadTodoOperation {
        if (dueDate == null && dueTime != null) {
            throw new BadTodoOperation(this, "Due date can not be unset if due time is set");
        }
        this.dueDate = dueDate;
    }

    public void setDueTime(@Nullable LocalTime dueTime) throws BadTodoOperation {
        if (dueTime != null && dueDate == null) {
            throw new BadTodoOperation(this, "Due time can only be set after a due date has been set");
        }
        this.dueTime = dueTime;
    }

    public void addChild(@NotNull Todo child) throws IllegalArgumentException {
        if (this.equals(child)) throw new IllegalArgumentException("Todo cannot be parent of itself");
        child.setParent(id);
        children.add(child.id);
    }

    public void addChild(@NotNull UUID childId) throws IllegalArgumentException {
        var child = store.findById(childId);
        addChild(child);
    }

    private void setParent(UUID parentId) {
        this.parentId = parentId;
    }

    public Todo getParent() {
        if (parentId == null) return null;
        return store.findById(parentId);
    }

    public void destroy() throws BadTodoOperation {
        if (!children.isEmpty()) throw new BadTodoOperation(this, "cannot destroy todo with children");
        store.unlink(this);
    }

    public void destroyTree() {
        for (var child : children) {
            store.removeBranchAtId(child);
        }
        store.unlink(this);
    }

    public @NotNull List<UUID> getChildren() {
        return children;
    }

    @NotNull
    private final UUID id;
    @NotNull
    private String name;
    @NotNull
    private String description;
    private boolean completed;
    @Nullable
    private Date dueDate;
    @Nullable
    private LocalTime dueTime;
    @Nullable
    private UUID parentId;
    @NotNull
    private final List<UUID> children = new ArrayList<>();
    @NotNull
    private final TodoStore store;
}
