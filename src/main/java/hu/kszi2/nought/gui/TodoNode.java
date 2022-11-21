package hu.kszi2.nought.gui;

import hu.kszi2.nought.core.Todo;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

public class TodoNode extends DefaultMutableTreeNode {
    protected TodoNode(Object obj) {
        super(obj);
        this.todo = null;
    }

    public TodoNode(Todo todo) {
        super(todo);
        this.todo = todo;
    }

    public void add(MutableTreeNode newChild, boolean linkToParent) {
        super.add(newChild);
        if (linkToParent && todo != null)
            todo.addChild(((TodoNode) newChild).getTodo());
    }

    public Todo getTodo() {
        return todo;
    }

    public boolean completedTodo() {
        return todo.isCompleted();
    }

    @Override
    public String toString() {
        if (todo == null) return "(Unknown todo)";
        return todo.getName();
    }

    private final Todo todo;
}
