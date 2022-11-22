package hu.kszi2.nought.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * <p>
 * The basic class of the Nought backend.
 * Contains the todo's id, which uniquely identifies it across the system,
 * along with other user facing values.
 * Since each Todo is uniquely part of one TodoStore, the todo stores this back
 * reference to be able to correctly perform actions which affect the store
 * object as well.
 * </p>
 * <p>
 * The todo object's values always hold the following properties:
 * <ul>
 *     <li>the id is globally unique</li>
 *     <li>due date references a valid date, or is null</li>
 *     <li>due time references a valid date, or is null</li>
 *     <li>if due date is null, so is due time</li>
 *     <li>if any of a todo's children are not completed,
 *     then the todo itself isn't</li>
 *     <li>if the todo's parent is completed, so is the todo</li>
 * </ul>
 * </p>
 *
 * @see TodoStore
 */
public class Todo implements Serializable {
    /**
     * Constructs a Todo object with all information.
     * This constructor is for internal use only, users outside the package
     * should use the {@code TodoBuilder} class.
     * The values which are not required may be null, these values are annotated
     * as such in their parameter description.
     *
     * @param store     The TodoStore object which this Todo is part of.
     * @param id        The unique identifier of the todo object.
     * @param name      The name of the todo object.
     *                  This may not be null, but it can be the empty string.
     * @param desc      The description of the todo object.
     *                  Like the name, this must be filled in, but can be the empty string.
     * @param completed Whether the todo object represents a completed todo.
     * @param dueDate   If the todo has an associated due completion date, this parameter is filled
     *                  in.
     *                  If there is no due date for the todo, this field can be {@literal null}.
     * @param dueTime   If the todo has an associated due completion time, this parameter is filled
     *                  in. This can only be set if the {@code dueDate} is not null.
     *                  If there is no due time for the todo, this field can be {@literal null}.
     * @param parentId  In case of a subtodo object, the outer todo object's identifier is to be
     *                  passed into this parameter.
     *                  This parameter can be {@literal null}, if this is a top-level todo object.
     * @throws BadTodoOperation       Thrown if a class invariant would be broken by the input
     *                                parameters.
     * @throws NoSuchElementException Thrown if a {@code parentId} references a todo which does
     *                                either not exist in the current {@code store}, or does not
     *                                exist in general.
     * @see TodoStore
     * @see TodoBuilder
     */
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

    /**
     * Checks if two todo objects are equal.
     * The equality check is based on the contained GUID.
     *
     * @param o The object to check against.
     * @return Whether the two objects are equal.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof Todo todo) {
            return todo.getId().equals(id);
        }
        return false;
    }

    /**
     * Generates a hash code for the todo object.
     * Hash code is based on the unique identifier of the todo object.
     *
     * @return A hash code.
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Returns the unique id of the todo object.
     *
     * @return The todo's id
     */
    public @NotNull UUID getId() {
        return id;
    }

    /**
     * Returns the name of the todo object.
     *
     * @return The todo's name
     */
    public @NotNull String getName() {
        return name;
    }

    /**
     * Returns the description of the todo object.
     *
     * @return The todo's description
     */
    public @NotNull String getDescription() {
        return description;
    }

    /**
     * Returns whether the current todo object is completed.
     *
     * @return Whether the todo is completed
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Returns the due date of the todo object, if it exists.
     * If the todo object does not have an associated due date, the function
     * returns {@literal null}.
     *
     * @return The due date, if it exists
     */
    public @Nullable Date getDueDate() {
        return dueDate;
    }

    /**
     * Returns the due time of the todo object, if it exists.
     * If the todo object does not have an associated due time, the function
     * returns {@literal null}.
     *
     * @return The due time, if it exists
     */
    public @Nullable LocalTime getDueTime() {
        return dueTime;
    }

    /**
     * Returns the parent of the todo object, if it exists.
     * If the todo object does not have an associated parent,
     * i.e. it is a top-level (root) todo, the function returns {@literal null}.
     *
     * @return The parent todo object, if it exists
     */
    public @Nullable Todo getParent() {
        if (parentId == null) return null;
        return store.findById(parentId);
    }

    /**
     * Returns the id of the parent of the todo object, if it exists.
     * If the todo object does not have an associated parent,
     * i.e. it is a top-level (root) todo, the function returns {@literal null}.
     *
     * @return The parent's id, if it exists
     */
    public @Nullable UUID getParentId() {
        return parentId;
    }

    /**
     * Returns the list of the todo's children's identifiers.
     *
     * @return A list of GUIDs, representing the todos children
     */
    public @NotNull List<UUID> getChildren() {
        return children;
    }

    /**
     * Sets the name of the todo object.
     *
     * @param name The new name
     */
    public void setName(@NotNull String name) {
        this.name = name;
    }

    /**
     * Sets the description of the todo object.
     *
     * @param description The new description
     */
    public void setDescription(@NotNull String description) {
        this.description = description;
    }

    /**
     * Sets the completion status of the todo object.
     * The completion can only be set to true, if all the todo's children are
     * also completed; and can only be set to false, if the todo is a top-level
     * todo object, or its parent is also not completed.
     *
     * @param completedParam The new completion status
     * @throws BadTodoOperation If a class invariant would be broken by setting the provided value
     */
    public void setCompleted(boolean completedParam) throws BadTodoOperation {
        if (completedParam && children.stream().map(store::findById).anyMatch(todo -> !todo.completed)) {
            throw new BadTodoOperation(this, "Children's completeness is not appropriate");
        }
        if (!completedParam && parentId != null && getParent().completed) {
            throw new BadTodoOperation(this, "Parent's completeness is not appropriate");
        }
        this.completed = completedParam;
    }

    /**
     * <p>
     * Sets the todo objects due date to the provided value.
     * If the parameter is null, the due date is removed from the object.
     * </p>
     * <p>
     * If the given string is null, and a due time is set on the todo object,
     * thus an invariant would be broken, if the setter succeeded,
     * a {@link BadTodoOperation} exception is thrown instead.
     * </p>
     * <p>
     * If the string is not of the format {@code "yyyy-MM-dd"}, then a {@link ParseException} is
     * thrown.
     * </p>
     *
     * @param dueDateStr The date to parse and set the value to.
     * @throws BadTodoOperation If a class invariant would be broken by setting the given value.
     * @throws ParseException   The provided string is not of the correct format.
     */
    public void setDueDate(@Nullable String dueDateStr) throws BadTodoOperation, ParseException {
        validateDueDateNullity(dueDateStr == null);
        if (dueDateStr == null) {
            setDueDate((Date) null);
            return;
        }
        var fmt = new SimpleDateFormat("yyyy-MM-dd");
        var date = fmt.parse(dueDateStr);
        setDueDate(date);
    }

    /**
     * <p>
     * Sets the todo objects due date to the provided value.
     * If the parameter is null, the due date is removed from the object.
     * </p>
     *
     * @param dueDate The date and set the value to.
     * @throws BadTodoOperation If a class invariant would be broken by setting the given value.
     */
    public void setDueDate(@Nullable Date dueDate) throws BadTodoOperation {
        validateDueDateNullity(dueDate == null);
        this.dueDate = dueDate;
    }

    /**
     * Verifies if setting the date value to a null/not null interface would
     * break a class invariant.
     * If it would, it throws {@link BadTodoOperation} exception.
     *
     * @param dateNull Whether the date is to be set to null or a valid value
     * @throws BadTodoOperation If the setting would break an invariant
     */
    private void validateDueDateNullity(boolean dateNull) throws BadTodoOperation {
        if (dateNull && dueTime != null)
            throw new BadTodoOperation(this, "Due date can not be unset if due time is set");
    }

    /**
     * <p>
     * Sets the todo objects due time to the provided value.
     * If the parameter is null, the due time is removed from the object.
     * </p>
     * <p>
     * If the given string is nto null, and a due date is set not on the todo object,
     * thus an invariant would be broken, if the setter succeeded,
     * a {@link BadTodoOperation} exception is thrown instead.
     * </p>
     * <p>
     * If the string is not of the format {@code "HH:mm[:ss]"}, where the part enclosed in square
     * brackets is optional, then a {@link ParseException} is thrown.
     * </p>
     *
     * @param dueTimeStr The time to parse and set the value to.
     * @throws BadTodoOperation If a class invariant would be broken by setting the given value.
     * @throws ParseException   The provided string is not of the correct format.
     */
    public void setDueTime(String dueTimeStr) throws BadTodoOperation, ParseException {
        validateDueTimeNullity(dueTimeStr == null);
        var fmt = DateTimeFormatter.ISO_TIME;
        try {
            if (dueTimeStr == null) {
                setDueTime((@Nullable LocalTime) null);
                return;
            }
            var time = LocalTime.parse(dueTimeStr, fmt);
            setDueTime(time);
        } catch (DateTimeParseException ex) {
            var pe = new ParseException(dueTimeStr, ex.getErrorIndex());
            pe.initCause(ex);
            throw pe;
        }
    }

    /**
     * <p>
     * Sets the todo objects due time to the provided value.
     * If the parameter is null, the due time is removed from the object.
     * </p>
     *
     * @param dueTime The time and set the value to.
     * @throws BadTodoOperation If a class invariant would be broken by setting the given value.
     */
    public void setDueTime(@Nullable LocalTime dueTime) throws BadTodoOperation {
        validateDueTimeNullity(dueTime == null);
        this.dueTime = dueTime;
    }

    /**
     * Verifies if setting the time value to a null/not null interface would
     * break a class invariant.
     * If it would, it throws {@link BadTodoOperation} exception.
     *
     * @param timeNull Whether the time is to be set to null or a valid value
     * @throws BadTodoOperation If the setting would break an invariant
     */
    private void validateDueTimeNullity(boolean timeNull) throws BadTodoOperation {
        if (!timeNull && dueDate == null)
            throw new BadTodoOperation(this, "Due time can only be set after a due date has been set");
    }

    /**
     * Ads a child sub-todo to the current todo object.
     * The child object's parent is set as the current object.
     *
     * @param child The todo object to be added as a child.
     * @throws IllegalArgumentException If the todo object would be its own child.
     */
    public void addChild(@NotNull Todo child) throws IllegalArgumentException {
        if (this.equals(child)) throw new IllegalArgumentException("Todo cannot be parent of itself");
        child.setParent(id);
        children.add(child.id);
    }

    /**
     * Ads a child sub-todo to the current todo object, referenced by its GUID.
     * The child object's parent is set as the current object.
     *
     * @param childId The todo object to be added as a child.
     * @throws IllegalArgumentException If the todo object would be its own child.
     */
    public void addChild(@NotNull UUID childId) throws IllegalArgumentException {
        var child = store.findById(childId);
        addChild(child);
    }

    /**
     * Sets the todo objects parent, referenced by its id.
     *
     * @param parentId The new parent's id
     */
    private void setParent(UUID parentId) {
        this.parentId = parentId;
    }

    /**
     * Destroy the current todo item, if it does not have any children.
     *
     * @throws BadTodoOperation If destroying this todo would lead to an orphaned todos.
     */
    public void destroy() throws BadTodoOperation {
        if (!children.isEmpty()) throw new BadTodoOperation(this, "cannot destroy todo with children");
        store.unlink(this);

        var parent = getParent();
        if (parent != null) {
            parent.removeChild(id);
        }
    }

    /**
     * Removes the child with {@code id} from the current todos children.
     * If it's not a child of this todo, nothing is done.
     *
     * @param id The child-to-remove's id
     */
    private void removeChild(UUID id) {
        children.remove(id);
    }

    /**
     * Returns the name of the todo object.
     *
     * @return The todo's name
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * Destroy this todo, and all its children along with it.
     */
    public void destroyTree() {
        for (var child : children) {
            store.removeBranchAtId(child);
        }
        store.unlink(this);
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
