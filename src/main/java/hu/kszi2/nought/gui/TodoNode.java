package hu.kszi2.nought.gui;

import hu.kszi2.nought.core.Todo;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

/**
 * A node in the TodoTree GUI element showing a specific todo object.
 */
public class TodoNode extends DefaultMutableTreeNode {
    /**
     * Constructor passing arbitrary object back to the super constructor.
     * Is only available to subclasses, to allow them to pass arbitrary objects
     * to our superclass, while outside code should only pass us Todo objects.
     *
     * @param obj The object to pass back up to the superclass
     */
    protected TodoNode(Object obj) {
        super(obj);
        this.todo = null;
    }

    /**
     * Constructs a todo node with the provided todo object.
     * This will be shown on the tree GUI component.
     *
     * @param todo The todo to show on the GUI
     */
    public TodoNode(Todo todo) {
        super(todo);
        this.todo = todo;
    }

    /**
     * Special add function which adds the component under this in the GUI,
     * and if linkToParent is true, also links the given child's todo object
     * to that of ours.
     * This maintains the tree structure provided in the GUI layer in the backend
     * layer.
     *
     * @param newChild     The child to add under this
     * @param linkToParent Whether to link the child's todo under ours
     */
    public void add(MutableTreeNode newChild, boolean linkToParent) {
        super.add(newChild);
        if (linkToParent && todo != null)
            todo.addChild(((TodoNode) newChild).getTodo());
    }

    /**
     * Returns our stored todo object.
     *
     * @return The stored Todo
     */
    public Todo getTodo() {
        return todo;
    }

    /**
     * Returns us whether the todo stored in this node is completed.
     *
     * @return Whether the todo stored is completed
     */
    public boolean completedTodo() {
        assert todo != null;
        return todo.isCompleted();
    }

    /**
     * Returns the stored name of the stored todo object of this for primarily
     * for showing it on the GUI.
     *
     * @return The stored todo's name
     */
    @Override
    public String toString() {
        if (todo == null) return "(Unknown todo)";
        return todo.getName();
    }

    private final Todo todo;
}
