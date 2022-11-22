package hu.kszi2.nought.gui;

import hu.kszi2.nought.core.TodoStore;

import javax.swing.*;

/**
 * Class containing the main entry point to the Nought application.
 */
public class Nought {
    /**
     * The main entry point.
     *
     * @param args CLI parameters
     */
    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            /* proceed with default */
        }

        var store = new TodoStore();

        new MainFrame(store);
    }
}
