package hu.kszi2.nought.gui;

import hu.kszi2.nought.core.Todo;
import hu.kszi2.nought.core.TodoStore;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultTreeModel;
import java.util.HashSet;

/**
 * The class for providing the model for the JTree showing the tree structure
 * of the Todo objects.
 */
public class TodoTree extends DefaultTreeModel {
    /**
     * Construct the tree model with the todo store object containing all todo objects.
     *
     * @param store The store of all todo elements
     */
    public TodoTree(TodoStore store) {
        super(new RootTodoNode());
        this.store = store;
        constructNodes();
    }

    /**
     * Constructs all todo objects into the tree of the TodoNode objects to
     * be shown on the GUI.
     */
    private void constructNodes() {
        var it = store.iterator();
        var roots = new HashSet<Todo>();

        while (it.hasNext()) {
            var todo = it.next();
            while (todo.getParent() != null) {
                todo = todo.getParent();
            }
            // todo is now a root node
            roots.add(todo);
        }

        var rootest = ((TodoNode) getRoot());
        it = roots.iterator();
        while (it.hasNext()) {
            var todo = it.next();
            addTodoAsChildToNode(rootest, todo, false);
        }
    }

    /**
     * Adds a new top-level (root) todo object to the tree.
     *
     * @param todo The todo to add
     */
    public void addRootTodo(Todo todo) {
        var node = new TodoNode(todo);
        var rootest = ((TodoNode) getRoot());
        rootest.add(node);
    }

    /**
     * Adds a new non-top-level (child) todo object to the tree under the
     * specified todo node.
     * If linkToParent is specified, the todo is added in a way, that the todo
     * object itself will also be added to its parent to do object as a child.
     *
     * @param node         The node object to insert the child under
     * @param todo         The todo object to insert
     * @param linkToParent Whether to link the todo objects together as well
     */
    public void addTodoAsChildToNode(@NotNull TodoNode node,
                                     Todo todo,
                                     boolean linkToParent) {
        var todoNode = new TodoNode(todo);
        node.add(todoNode, linkToParent);
        var it = todo.getChildren().stream()
                .map(store::findById).iterator();
        while (it.hasNext()) {
            var child = it.next();
            addTodoAsChildToNode(todoNode, child, false);
        }
    }

    private final TodoStore store;
}
