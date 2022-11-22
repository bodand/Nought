package hu.kszi2.nought.gui;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import java.util.function.Consumer;

/**
 * Universal modification listener for JTree models.
 * Joins all three events into a single callback provided at construction, for
 * when it is irrelevant which event occurs.
 */
public class TreeModificationListener implements TreeModelListener {
    /**
     * Constructs the TreeModificationListener with the provided callback which
     * will then be called upon any tree modification event.
     *
     * @param callback The event callback
     */
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
