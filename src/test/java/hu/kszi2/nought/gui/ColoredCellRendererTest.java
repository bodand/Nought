package hu.kszi2.nought.gui;

import hu.kszi2.nought.core.Todo;
import hu.kszi2.nought.core.TodoStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

class ColoredCellRendererTest {
    @BeforeEach
    void setUp() throws Exception {
        var store = new TodoStore();
        todo = store.newBuilder().setName("A").setDescription("D").newId().build();

        renderer = new ColoredCellRenderer();
        tree = new JTree();
        tree.setCellRenderer(renderer);
    }

    @Test
    void getTreeCellRendererComponentRecolorsCompletedNode() throws Exception {
        todo.setCompleted(true);
        var node = new TodoNode(todo);
        var comp = renderer.getTreeCellRendererComponent(tree,
                node,
                false,
                true,
                true,
                1,
                false);
        assertEquals(new Color(156, 177, 158), comp.getForeground());
    }

    @Test
    void getTreeCellRendererComponentDoesNotRecolorIncompleteNode() throws Exception {
        todo.setCompleted(false);
        var node = new TodoNode(todo);
        var comp = renderer.getTreeCellRendererComponent(tree,
                node,
                false,
                true,
                true,
                1,
                false);
        assertNotEquals(new Color(156, 177, 158), comp.getForeground());
    }

    JTree tree;
    ColoredCellRenderer renderer;
    Todo todo;
}