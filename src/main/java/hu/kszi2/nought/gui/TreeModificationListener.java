package hu.kszi2.nought.gui;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import java.util.function.Consumer;

public class TreeModificationListener implements TreeModelListener {
    public TreeModificationListener(Consumer<TreeModelEvent> callback) {
        this.callback = callback;
    }

    @Override
    public void treeNodesChanged(TreeModelEvent e) {
        callback.accept(e);
    }

    @Override
    public void treeNodesInserted(TreeModelEvent e) {
        callback.accept(e);
    }

    @Override
    public void treeNodesRemoved(TreeModelEvent e) {
        callback.accept(e);
    }

    @Override
    public void treeStructureChanged(TreeModelEvent e) {
        callback.accept(e);
    }

    private final Consumer<TreeModelEvent> callback;
}
