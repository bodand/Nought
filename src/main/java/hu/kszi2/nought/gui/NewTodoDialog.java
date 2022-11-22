package hu.kszi2.nought.gui;

import hu.kszi2.nought.core.BadTodoOperation;
import hu.kszi2.nought.core.Todo;
import hu.kszi2.nought.core.TodoBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * The modal dialog box used to create new todo entries.
 */
public class NewTodoDialog extends JDialog {
    /**
     * Constructs a new dialog window with a parent and the {@link TodoBuilder}
     * instance to use to construct the new todo object.
     * The window is modal: interactions with the parent window are blocked
     * until this window is visible.
     *
     * @param parent  The dialog windows parent window
     * @param builder The builder to build the new todo object with
     */
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

    /**
     * Adds a shortcut on the Esc key to close the dialog window.
     *
     * @param cancel The button to handle the escape event
     */
    private void setCancelShortcut(@NotNull JButton cancel) {
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

    /**
     * The action that happens whenever the creation of the todo is cancelled.
     */
    private void cancelAction() {
        built = null;
        dispose();
    }

    /**
     * Adds a shortcut on the Ctrl+Enter keys to OK the dialog window, and
     * create the new todo object.
     *
     * @param builder The todo builder object
     * @param ok      The button to handle the creation event
     */

    private void setOkShortcut(TodoBuilder builder, @NotNull JButton ok) {
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

    /**
     * The action that happens whenever the "OK" button was pressed.
     * The creation of the new todo object.
     *
     * @param builder The builder object
     */
    private void okAction(@NotNull TodoBuilder builder) {
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

    /**
     * Returns the builder given at construction.
     *
     * @return The builder that is used to build the Todo instance
     */
    private TodoBuilder getBuilder() {
        return builder;
    }

    /**
     * Returns the built object, or {@code null} if creation was cancelled.
     *
     * @return The build object or {@code null}
     */
    public @Nullable Todo getBuilt() {
        return built;
    }

    private Todo built;
    private final TodoBuilder builder;
}
