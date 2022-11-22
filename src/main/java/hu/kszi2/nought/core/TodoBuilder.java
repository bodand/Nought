package hu.kszi2.nought.core;

import org.jetbrains.annotations.Contract;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * A class used to create objects of the Todo class.
 * Each field can be set separately, allowing incremental construction, for example when reading
 * the todo's data from a file.
 * Since a todo object is bound to a {@link TodoStore}, this builder is also: all created objects
 * will be inserted into the store associated at construction time.
 * The store object can create an appropriate builder object for itself.
 *
 * @see Todo
 * @see TodoStore
 */
public class TodoBuilder implements Serializable {
    /**
     * Creates a new TodoBuilder object and associates it with the given store.
     *
     * @param store The store to create the todo objects in
     * @see TodoStore
     */
    TodoBuilder(TodoStore store) {
        this.store = store;
    }

    /**
     * Constructs the Todo object from the previously provided values.
     * When this method is called, at least the {@code setName}, {@code setDescription}, and
     * {@code setId} methods need to have been called.
     * The {@code setId} can be replaced by the {@code newId} convenience wrapper.
     *
     * @return The new todo object.
     * @throws IllegalArgumentException If not all the required methods were called on this object
     *                                  before this method.
     * @throws BadTodoOperation         If a todo object were to be constructed using the provided
     *                                  arguments, it would violate the {@link Todo} class'
     *                                  invariants.
     * @see Todo
     */
    @Contract("->new")
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

    /**
     * Generates a random GUID for the todo to be built.
     * A convenience wrapper around setId.
     *
     * @return The builder object.
     */
    @Contract("->this")
    public TodoBuilder newId() {
        return setId(UUID.randomUUID());
    }

    /**
     * <p>
     * Sets the todo object's that is to be built id.
     * This should be unique in a program, and must be unique within a todo store.
     * Behavior of programs passing the same id to different todo objects in the same store is
     * undefined.
     * </p>
     * This method must be called at least once, before building the todo object.
     *
     * @param id The new GUID
     * @return The builder object.
     */
    @Contract("_->this")
    public TodoBuilder setId(UUID id) {
        this.id = id;
        return this;
    }

    /**
     * <p>
     * Sets the object to be built's name.
     * </p>
     * This method must be called at least once, before building the todo object.
     *
     * @param name The new name
     * @return The builder object.
     */
    @Contract("_->this")
    public TodoBuilder setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * <p>
     * Sets the object to be built's description.
     * </p>
     * This method must be called at least once, before building the todo object.
     *
     * @param desc The new description
     * @return The builder object.
     */
    @Contract("_->this")
    public TodoBuilder setDescription(String desc) {
        this.desc = desc;
        return this;
    }

    /**
     * Sets the object to be built's completion status.
     * True represents completed, while false is incomplete.
     *
     * @param completed The new completion status.
     * @return The builder object.
     */
    @Contract("_->this")
    public TodoBuilder setCompleted(boolean completed) {
        this.completed = completed;
        return this;
    }


    /**
     * Sets the object to be built's due date.
     *
     * @param dueDate The new due date
     * @return The builder object.
     */
    @Contract("_->this")
    public TodoBuilder setDueDate(Date dueDate) {
        this.dueDate = dueDate;
        return this;
    }

    /**
     * Sets the object to be built's due time.
     *
     * @param dueTime The new due time
     * @return The builder object.
     */
    @Contract("_->this")
    public TodoBuilder setDueTime(LocalTime dueTime) {
        this.dueTime = dueTime;
        return this;
    }

    /**
     * Adds a child to the todo object to be built.
     * This child is identified by its id, and must already be part of the object store when
     * the current todo object is built.
     *
     * @param cid The child's id to be added
     * @return The builder object.
     */
    @Contract("_->this")
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
