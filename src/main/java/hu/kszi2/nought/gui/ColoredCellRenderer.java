package hu.kszi2.nought.gui;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class ColoredCellRenderer extends DefaultTreeCellRenderer {
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
       if (value instanceof TodoNode todo) {
            var def = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

            if (todo.completedTodo() && !selected) {
                def.setForeground(new Color(156, 177, 158));
            }
            return def;
        }
        // should not happen
        return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    }
}
