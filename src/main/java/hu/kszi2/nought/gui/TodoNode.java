package hu.kszi2.nought.gui;

import hu.kszi2.nought.core.Todo;
import hu.kszi2.nought.core.TodoStore;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.Enumeration;
import java.util.UUID;
import java.util.Vector;

public class TodoNode extends DefaultMutableTreeNode {
    protected TodoNode(Object obj) {
        super(obj);
        this.todo = null;
    }

    public TodoNode(Todo todo) {
        super(todo);
        this.todo = todo;
    }

    public Todo getTodo() {
        return todo;
    }

    @Override
    public String toString() {
        return todo.getName();
    }

    private final Todo todo;
}
