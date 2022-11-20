package hu.kszi2.nought.gui;

import hu.kszi2.nought.core.TodoStore;

import javax.swing.*;

public class Nought {
    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available, you can set the GUI to another look and feel.
        }

        var store = new TodoStore();

        new MainFrame(store);
    }
}
