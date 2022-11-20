package hu.kszi2.nought.gui;

import javax.swing.tree.DefaultMutableTreeNode;

public class RootTodoNode extends TodoNode {
    public RootTodoNode() {
        super("Todos");
    }

    @Override
    public String toString() {
        return "Todos";
    }
}
