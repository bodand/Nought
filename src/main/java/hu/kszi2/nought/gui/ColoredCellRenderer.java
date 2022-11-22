package hu.kszi2.nought.gui;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * Special TreeCellRender that sets the font color for TreeNode render elements
 * whose Todo instance is completed.
 */
public class ColoredCellRenderer extends DefaultTreeCellRenderer {
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        var def = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

        if (value instanceof TodoNode todo
                && todo.completedTodo()
                && !selected) {
            def.setForeground(new Color(156, 177, 158));
        }

        return def;
    }
}
