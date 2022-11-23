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
        setLocationRelativeTo(parent);

        var conb = new GridBagConstraintBuilder();
        var topInsets = new Insets(8, 8, 3, 8);
        var innerInsets = new Insets(3, 8, 3, 8);
        var bottomInsets = new Insets(3, 8, 8, 8);
        // NAME //
        add(new JLabel("Name"), conb.ipad(3, 3)
                .grid(0, 0)
                .insets(topInsets)
                .anchor(GridBagConstraints.LINE_START)
                .fill(GridBagConstraints.HORIZONTAL)
                .build());

        var name = new JTextField();
        name.getDocument().addDocumentListener(new FieldUpdateListener<>(this::getBuilder, name::getText, TodoBuilder::setName));
        add(name, conb.ipad(3, 3)
                .grid(1, 0)
                .gridwidth(2)
                .insets(topInsets)
                .weightx(1.0)
                .anchor(GridBagConstraints.LINE_END)
                .fill(GridBagConstraints.HORIZONTAL)
                .build());

        // DESCRIPTION //
        add(new JLabel("Description"), conb.ipad(3, 3)
                .grid(0, 1)
                .insets(innerInsets)
                .anchor(GridBagConstraints.LINE_START)
                .fill(GridBagConstraints.HORIZONTAL)
                .build());

        var desc = new JTextArea();
        desc.getDocument().addDocumentListener(new FieldUpdateListener<>(this::getBuilder, desc::getText, TodoBuilder::setDescription));
        add(new JScrollPane(desc), conb.ipad(3, 3)
                .grid(1, 1)
                .gridwidth(2)
                .weight(1.0, 1.0)
                .insets(innerInsets)
                .anchor(GridBagConstraints.LINE_END)
                .fill(GridBagConstraints.BOTH)
                .build());

        var ok = new JButton("OK");
        ok.addActionListener(ae -> okAction());
        setOkShortcut(ok);
        add(ok, conb.ipad(3, 3)
                .grid(2, 3)
                .insets(bottomInsets)
                .anchor(GridBagConstraints.LINE_END)
                .fill(GridBagConstraints.NONE)
                .build());

        var cancel = new JButton("Cancel");
        cancel.setBackground(new Color(176, 49, 49));
        cancel.addActionListener(ae -> cancelAction());
        setCancelShortcut(cancel);
        add(cancel, conb.ipad(3, 3)
                .grid(2, 3)
                .insets(bottomInsets)
                .anchor(GridBagConstraints.LINE_START)
                .fill(GridBagConstraints.NONE)
                .build());
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
     * @param ok The button to handle the creation event
     */
    private void setOkShortcut(@NotNull JButton ok) {
        ok.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("control ENTER"),
                        "shortcutOk");
        ok.getActionMap().put("shortcutOk", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                okAction();
            }
        });
    }

    /**
     * The action that happens whenever the "OK" button was pressed.
     * The creation of the new todo object.
     */
    private void okAction() {
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
