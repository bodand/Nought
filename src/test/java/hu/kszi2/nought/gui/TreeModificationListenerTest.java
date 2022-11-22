package hu.kszi2.nought.gui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.event.TreeModelEvent;

import static org.junit.jupiter.api.Assertions.*;

class TreeModificationListenerTest {
    private static class DummyTreeEvent extends TreeModelEvent {
        public DummyTreeEvent() {
            super("thing", (Object[]) null);
        }
    }

    @BeforeEach
    void setUp() {
        called = false;
        listener = new TreeModificationListener(e -> called = true);
    }

    @Test
    void treeNodesChangedCallsCallback() {
        listener.treeNodesChanged(new DummyTreeEvent());
        assertTrue(called);
    }

    @Test
    void treeNodesInsertedCallsCallback() {
        listener.treeNodesInserted(new DummyTreeEvent());
        assertTrue(called);
    }

    @Test
    void treeNodesRemovedCallsCallback() {
        listener.treeNodesRemoved(new DummyTreeEvent());
        assertTrue(called);
    }

    @Test
    void treeStructureChangedCallsCallback() {
        listener.treeStructureChanged(new DummyTreeEvent());
        assertTrue(called);
    }

    TreeModificationListener listener;
    boolean called = false;
}