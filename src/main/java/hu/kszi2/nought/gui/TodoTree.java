package hu.kszi2.nought.gui;

import hu.kszi2.nought.core.Todo;
import hu.kszi2.nought.core.TodoStore;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.HashSet;

public class TodoTree extends DefaultTreeModel {
    public TodoTree(TodoStore store) {
        super(new RootTodoNode());
        this.store = store;
        constructNodes();
    }

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

        var rootest = ((DefaultMutableTreeNode) getRoot());
        it = roots.iterator();
        while (it.hasNext()) {
            var todo = it.next();
            addTodoAsChildToNode(rootest, todo);
        }
    }

    private void addTodoAsChildToNode(@NotNull DefaultMutableTreeNode node,
                                      Todo todo) {
        var todoNode = new TodoNode(todo);
        node.add(todoNode);
        todo.getChildren().stream()
                .map(store::findById)
                .forEach(child -> addTodoAsChildToNode(todoNode, child));
    }

    private final transient TodoStore store;
}
