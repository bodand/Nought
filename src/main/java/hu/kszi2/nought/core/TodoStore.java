package hu.kszi2.nought.core;

import hu.kszi2.nought.io.TodoExporter;
import hu.kszi2.nought.io.TodoImporter;

import java.io.Serializable;
import java.util.*;

/**
 * The class responsible for managing a set of todo objects during runtime.
 * Each todo object is associated with exactly one store.
 */
public class TodoStore implements Serializable {
    /**
     * Returns a {@link TodoBuilder} object associated with this store object.
     *
     * @return A new {@link TodoBuilder}
     * @see TodoBuilder
     */
    public TodoBuilder newBuilder() {
        return new TodoBuilder(this);
    }

    /**
     * Returns a {@link TodoImporter} object associated with this store object, using the default
     * implementation used by the store.
     *
     * @return A new importer
     * @see TodoImporter
     */
    public TodoImporter newImporter() {
        return TodoImporter.newDefault(this);
    }

    /**
     * Returns a {@link TodoExporter} object associated with this store object, using the default
     * implementation used by the store.
     *
     * @return A new exporter
     * @see TodoExporter
     */
    public TodoExporter newExporter() {
        return TodoExporter.newDefault(this);
    }

    /**
     * Provides an iterator to the todo objects stored within the store.
     *
     * @return An iterator over the todos stored
     */
    public Iterator<Todo> iterator() {
        return todos.iterator();
    }

    /**
     * Inserts a todo into the store.
     * If the todo already stored in the store, the function is nop.
     *
     * @param todo The todo to insert
     * @throws IllegalArgumentException Trying to add {@code null} into the store.
     */
    public void add(Todo todo) throws IllegalArgumentException {
        if (todo == null) throw new IllegalArgumentException("added todo cannot be null");

        if (todos.stream().noneMatch(elem -> elem.getId().equals(todo.getId()))) {
            todos.add(todo);
        }
    }

    /**
     * Searches for a todo object in the store based on its unique id.
     *
     * @param id The id to search for.
     * @return The todo element in the store with the given id
     * @throws NoSuchElementException The store does not contain an element with the given id
     */
    public Todo findById(UUID id) throws NoSuchElementException {
        var todo = todos
                .stream()
                .filter(elem -> elem.getId().equals(id))
                .findAny(); // there can only be one elem with wanted GUID

        if (todo.isEmpty()) throw new NoSuchElementException();
        return todo.get();
    }

    /**
     * Removes a todo specified by its id.
     * If the todo object has children, the function fails.
     *
     * @param id The id to search for and delete
     * @throws NoSuchElementException The store does not contain an element with the given id
     * @throws BadTodoOperation       The todo with the given id had children
     */
    public void removeById(UUID id) throws NoSuchElementException, BadTodoOperation {
        var todo = findById(id);
        todo.destroy();
    }

    /**
     * Removes a todo and all its children specified by the branch's root todo's id.
     * This is a really destructive operation without any verification,
     * handle with extreme care.
     *
     * @param id The id to search for and delete
     * @throws NoSuchElementException The store does not contain an element with the given id
     */
    public void removeBranchAtId(UUID id) {
        var branchRoot = findById(id);
        branchRoot.destroyTree();
    }

    /**
     * Unlinks a todo object from this store.
     * This should only be used when deleting the todo object.
     * If the todo is not part of the current store, the function is nop.
     *
     * @param todo The todo to remove.
     */
    void unlink(Todo todo) {
        todos.remove(todo);
    }

    private final List<Todo> todos = new ArrayList<>();
}
