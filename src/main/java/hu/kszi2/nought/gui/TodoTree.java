package hu.kszi2.nought.gui;

import hu.kszi2.nought.core.Todo;
import hu.kszi2.nought.core.TodoStore;
import org.jetbrains.annotations.NotNull;

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

        var rootest = ((TodoNode) getRoot());
        it = roots.iterator();
        while (it.hasNext()) {
            var todo = it.next();
            addTodoAsChildToNode(rootest, todo, false);
        }
    }

    public void addRootTodo(Todo todo) {
        var node = new TodoNode(todo);
        var rootest = ((TodoNode) getRoot());
        rootest.add(node);
    }

    public void addTodoAsChildToNode(@NotNull TodoNode node,
                                     Todo todo, boolean linkToParent) {
        var todoNode = new TodoNode(todo);
        node.add(todoNode, linkToParent);
        var it = todo.getChildren().stream()
                .map(store::findById).iterator();
        while (it.hasNext()) {
            var child = it.next();
            addTodoAsChildToNode(todoNode, child, false);
        }
    }

    private final transient TodoStore store;
}
