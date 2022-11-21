package hu.kszi2.nought.gui;

import hu.kszi2.nought.core.BadTodoOperation;
import hu.kszi2.nought.core.Todo;
import hu.kszi2.nought.core.TodoBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class NewTodoDialog extends JDialog {
    public NewTodoDialog(Frame parent, TodoBuilder builder) {
        super(parent, "Nought - New todo", true);
        this.builder = builder;
        this.builder.newId();

        setSize(300, 180);
        setLayout(new GridBagLayout());

        // NAME //
        add(new JLabel("Name"), new GridBagConstraints(0, 0,
                1, 1,
                0.0, 0.0,
                GridBagConstraints.LINE_START,
                GridBagConstraints.HORIZONTAL,
                new Insets(8, 8, 3, 8), 0, 0));

        var name = new JTextField();
        name.getDocument().addDocumentListener(new FieldUpdateListener<>(this::getBuilder, name::getText, TodoBuilder::setName));
        add(name, new GridBagConstraints(1, 0,
                2, 1,
                1.0, 0.0,
                GridBagConstraints.LINE_END,
                GridBagConstraints.HORIZONTAL,
                new Insets(8, 8, 3, 8), 0, 0));

        // DESCRIPTION //
        add(new JLabel("Description"), new GridBagConstraints(0, 1,
                1, 1,
                0.0, 0.0,
                GridBagConstraints.LINE_START,
                GridBagConstraints.HORIZONTAL,
                new Insets(3, 8, 3, 8), 0, 0));

        var desc = new JTextArea();
        desc.getDocument().addDocumentListener(new FieldUpdateListener<>(this::getBuilder, desc::getText, TodoBuilder::setDescription));
        add(new JScrollPane(desc), new GridBagConstraints(1, 1,
                2, 1,
                1.0, 1.0,
                GridBagConstraints.LINE_END,
                GridBagConstraints.BOTH,
                new Insets(3, 8, 3, 8), 0, 0));

        var ok = new JButton("OK");
        ok.addActionListener(ae -> okAction(builder));
        setOkShortcut(builder, ok);
        add(ok, new GridBagConstraints(2, 3,
                1, 1,
                0, 0,
                GridBagConstraints.LINE_END,
                GridBagConstraints.NONE,
                new Insets(3, 8, 8, 8), 0, 0));

        var cancel = new JButton("Cancel");
        cancel.setBackground(new Color(176, 49, 49));
        cancel.addActionListener(ae -> cancelAction());
        setCancelShortcut(cancel);
        add(cancel, new GridBagConstraints(2, 3,
                1, 1,
                0, 0,
                GridBagConstraints.LINE_START,
                GridBagConstraints.NONE,
                new Insets(3, 8, 8, 8), 0, 0));
    }

    private void setCancelShortcut(JButton cancel) {
        cancel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ESCAPE"),
                        "shortcutEsc");
        cancel.getActionMap().put("shortcutEsc", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelAction();
            }
        });
    }

    private void cancelAction() {
        built = null;
        dispose();
    }

    private void setOkShortcut(TodoBuilder builder, JButton ok) {
        ok.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("control ENTER"),
                        "shortcutOk");
        ok.getActionMap().put("shortcutOk", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                okAction(builder);
            }
        });
    }

    private void okAction(TodoBuilder builder) {
        try {
            built = builder.build();
            dispose();
        } catch (BadTodoOperation | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not create Todo: \n" + ex.getMessage(),
                    "Error - New Todo",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public TodoBuilder getBuilder() {
        return builder;
    }

    public Todo getBuilt() {
        return built;
    }

    private Todo built;
    private final TodoBuilder builder;
}
