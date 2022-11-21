package hu.kszi2.nought.core;

import hu.kszi2.nought.io.TodoExporter;
import hu.kszi2.nought.io.TodoImporter;
import hu.kszi2.nought.io.TodoXMLExporter;
import hu.kszi2.nought.io.TodoXMLImporter;

import java.io.Serializable;
import java.util.*;

public class TodoStore implements Serializable {
    public TodoBuilder newBuilder() {
        return new TodoBuilder(this);
    }

    public TodoImporter newImporter() {
        return new TodoXMLImporter(this);
    }

    public TodoExporter newExporter() {
        return new TodoXMLExporter(this);
    }

    public Iterator<Todo> iterator() {
        return todos.iterator();
    }

    public void add(Todo todo) throws IllegalArgumentException {
        if (todo == null) throw new IllegalArgumentException("added todo cannot be null");

        if (todos.stream().noneMatch(elem -> elem.getId().equals(todo.getId()))) {
            todos.add(todo);
        }
    }

    public Todo findById(UUID id) throws NoSuchElementException {
        var todo = todos
                .stream()
                .filter(elem -> elem.getId().equals(id))
                .findAny(); // there can only be one elem with wanted GUID

        if (todo.isEmpty()) throw new NoSuchElementException();
        return todo.get();
    }

    public void removeById(UUID id) throws NoSuchElementException, BadTodoOperation {
        var todo = findById(id);
        todo.destroy();
    }

    public void removeBranchAtId(UUID id) {
        var branchRoot = findById(id);
        branchRoot.destroyTree();
    }

    void unlink(Todo todo) {
        todos.remove(todo);
    }

    private final List<Todo> todos = new ArrayList<>();
}
