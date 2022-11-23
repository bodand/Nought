package hu.kszi2.nought.gui;

import hu.kszi2.nought.core.BadTodoOperation;
import hu.kszi2.nought.core.Todo;
import hu.kszi2.nought.core.TodoStore;
import org.jetbrains.annotations.*;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

/**
 * The main frame of the Nought application.
 * This is the frame which allows the editing of currently existing todos,
 * rendering and allowing editing of the todo hierarchy.
 */
public class MainFrame extends JFrame {
    /**
     * Constructs a main frame over a given {@link TodoStore}.
     * The contents of the store will be rendered.
     *
     * @param store The store containing the todos
     */
    public MainFrame(TodoStore store) {
        this.store = store;
        try {
            setLayout(new GridBagLayout());

            constructMenuBar();
            constructTodoEditor();
            constructTodoExplorer();

            setTitle("Nought");
            setSize(700, 350);
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    closeSelf();
                }
            });
            setVisible(true);
            changeEdited(null);
        } catch (Exception ex) {
            System.err.println("error: cannot initialize Nought: ");
            ex.printStackTrace();
        }
    }

    /**
     * Creates the menu bar of the frame.
     * This contains the File and Todo menus with file saving/loading facilities
     * and todo addition/removal features, respectively.
     */
    private void constructMenuBar() {
        var jmb = new JMenuBar();
        var fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        var todoMenu = new JMenu("Todo");
        todoMenu.setMnemonic(KeyEvent.VK_T);

        // File //
        // File > Save
        JMenuItem saveMenu = new JMenuItem("Save", KeyEvent.VK_S);
        saveMenu.setAccelerator(KeyStroke.getKeyStroke("control S"));
        saveMenu.addActionListener(ae -> saveStore());
        fileMenu.add(saveMenu);
        // File > SaveAs
        JMenuItem saveAsMenu = new JMenuItem("Save as", KeyEvent.VK_A);
        saveAsMenu.setAccelerator(KeyStroke.getKeyStroke("control shift S"));
        saveAsMenu.addActionListener(ae -> saveStoreAs());
        fileMenu.add(saveAsMenu);
        // --
        fileMenu.addSeparator();
        // File > Load
        var load = new JMenuItem("Load", KeyEvent.VK_L);
        load.setAccelerator(KeyStroke.getKeyStroke("control L"));
        load.addActionListener(ae -> loadNewStore());
        fileMenu.add(load);
        // --
        fileMenu.addSeparator();
        // File > Exit
        var exit = new JMenuItem("Exit", KeyEvent.VK_X);
        exit.setAccelerator(KeyStroke.getKeyStroke("control Q"));
        exit.addActionListener(ae -> closeSelf());
        fileMenu.add(exit);

        // Todo //
        // Todo > New
        var newRoot = new JMenuItem("New", KeyEvent.VK_N);
        newRoot.addActionListener(ae -> newRoot());
        newRoot.setAccelerator(KeyStroke.getKeyStroke("control shift N"));
        todoMenu.add(newRoot);
        // Todo > New dependent
        newTodoMenu = new JMenuItem("New dependent", KeyEvent.VK_D);
        newTodoMenu.addActionListener(ae -> newChild());
        newTodoMenu.setAccelerator(KeyStroke.getKeyStroke("control N"));
        todoMenu.add(newTodoMenu);
        // --
        todoMenu.addSeparator();
        // Todo > Delete
        deleteMenu = new JMenuItem("Delete", KeyEvent.VK_D);
        deleteMenu.addActionListener(ae -> deleteSelected());
        deleteMenu.setAccelerator(KeyStroke.getKeyStroke("control D"));
        todoMenu.add(deleteMenu);
        // Todo > Delete with children
        deleteTreeMenu = new JMenuItem("Delete with subtodoes", KeyEvent.VK_W);
        deleteTreeMenu.addActionListener(ae -> deleteSelectedTree());
        deleteTreeMenu.setAccelerator(KeyStroke.getKeyStroke("control shift D"));
        todoMenu.add(deleteTreeMenu);

        jmb.add(fileMenu);
        jmb.add(todoMenu);
        setJMenuBar(jmb);
    }

    /**
     * Closes the current frame by calling dispose.
     * If there are unsaved changes, it will open a confirmation dialog, asking
     * whether to save them.
     * This dialog can abort closing the window, by the user selecting cancel.
     */
    private void closeSelf() {
        if (!saved) {
            var yes = 0;
            var cancel = 2;
            var userSure = JOptionPane.showConfirmDialog(this,
                    "You have unsaved changes. Would you like to save before exiting?",
                    "Unsaved progress",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (userSure == cancel) return;
            if (userSure == yes) saveStore();
        }
        dispose();
    }

    /**
     * Creates the todo-editor part of the frame.
     * This contains the name and description fields, and the
     * validity checked date/time fields, as well as the completion checkbox.
     */
    private void constructTodoEditor() {
        var topInsets = new Insets(8, 8, 3, 8);
        var innerInsets = new Insets(3, 8, 3, 8);
        var bottomInsets = new Insets(3, 8, 8, 8);
        var builder = new GridBagConstraintBuilder();

        add(new JLabel("Name"), builder.ipad(3, 3)
                .grid(0, 0)
                .insets(topInsets)
                .anchor(GridBagConstraints.LINE_START).build());

        name = new JTextField();
        name.getDocument().addDocumentListener(new FieldUpdateListener<>(this::getEdited, name::getText,
                (todo, value) -> {
                    todo.setName(value);
                    reloadTreeAtSelected();
                }));
        add(name, builder.ipad(3, 3)
                .grid(1, 0)
                .insets(topInsets)
                .anchor(GridBagConstraints.LINE_END)
                .fill(GridBagConstraints.HORIZONTAL)
                .gridwidth(2)
                .weightx(.6)
                .build());

        add(new JLabel("Description"), builder.ipad(3, 3)
                .grid(0, 1)
                .insets(innerInsets)
                .anchor(GridBagConstraints.LINE_START)
                .build());
        description = new JTextArea();
        description.getDocument().addDocumentListener(new FieldUpdateListener<>(this::getEdited, description::getText, Todo::setDescription));
        add(new JScrollPane(description), builder.ipad(3, 3)
                .grid(1, 1)
                .gridwidth(2)
                .insets(innerInsets)
                .anchor(GridBagConstraints.LINE_END)
                .fill(GridBagConstraints.BOTH)
                .weight(.6, 1.0)
                .build());

        add(new JLabel("Due"), builder.ipad(3, 3)
                .grid(0, 2)
                .insets(innerInsets)
                .gridheight(2)
                .anchor(GridBagConstraints.FIRST_LINE_START)
                .build());
        add(new JLabel("Date"), builder.ipad(3, 3)
                .grid(1, 2)
                .insets(innerInsets)
                .anchor(GridBagConstraints.LINE_START)
                .build());
        dueDate = new JTextField();
        dueDate.getDocument().addDocumentListener(new FieldUpdateListener<>(this::getEdited, dueDate::getText,
                (todo, value) -> {
                    try {
                        todo.setDueDate(value);
                    } catch (Exception ex) {
                        /* ignore */
                    }
                }));
        add(dueDate, builder.ipad(3, 3)
                .grid(2, 2)
                .insets(innerInsets)
                .fill(GridBagConstraints.HORIZONTAL)
                .anchor(GridBagConstraints.LINE_END)
                .weightx(.6)
                .build());

        add(new JLabel("Time"), builder.ipad(3, 3)
                .grid(1, 3)
                .insets(innerInsets)
                .anchor(GridBagConstraints.LINE_START)
                .build());
        dueTime = new JTextField();
        dueTime.getDocument().addDocumentListener(new FieldUpdateListener<>(this::getEdited, dueTime::getText,
                (todo, value) -> {
                    try {
                        todo.setDueTime(value);
                    } catch (Exception ex) {
                        /* ignore */
                    }
                }));
        add(dueTime, builder.ipad(3, 3)
                .grid(2, 3)
                .insets(innerInsets)
                .fill(GridBagConstraints.HORIZONTAL)
                .anchor(GridBagConstraints.LINE_END)
                .weightx(.6)
                .build());

        dueTime.setInputVerifier(new CompoundInputVerifier(
                new TimeInputVerifier(),
                new DateTimeIntegrityVerifier(dueDate, dueTime)
        ));
        dueDate.setInputVerifier(new CompoundInputVerifier(
                new DateInputVerifier(),
                new DateTimeIntegrityVerifier(dueDate, dueTime)
        ));

        completed = new JCheckBox("Completed");
        completed.addActionListener(ae -> {
            try {
                if (edited != null) {
                    edited.setCompleted(completed.isSelected());
                    newTodoMenu.setEnabled(!completed.isEnabled());
                    addSubtodo.setEnabled(!completed.isSelected());
                }
            } catch (BadTodoOperation ex) {
                /* ignore */
            }
        });
        add(completed, builder.ipad(3, 3)
                .grid(0, 4)
                .insets(innerInsets)
                .anchor(GridBagConstraints.LINE_START)
                .build());

        addSubtodo = new JButton("Add subtodo");
        addSubtodo.addActionListener(ae -> newChild());
        add(addSubtodo, builder.ipad(3, 3)
                .grid(0, 5)
                .insets(bottomInsets)
                .anchor(GridBagConstraints.LINE_START)
                .fill(GridBagConstraints.HORIZONTAL)
                .build());

        remove = new JButton("Remove");
        remove.addActionListener(ae -> deleteSelected());
        add(remove, builder.ipad(3, 3)
                .grid(1, 5)
                .insets(bottomInsets)
                .anchor(GridBagConstraints.LINE_START)
                .fill(GridBagConstraints.HORIZONTAL)
                .build());
    }

    /**
     * Creates the todo explorer controls; this contains the tree and the new
     * button.
     */
    private void constructTodoExplorer() {
        var builder = new GridBagConstraintBuilder();

        tree = new JTree(new TodoTree(store));
        tree.setCellRenderer(new ColoredCellRenderer());
        tree.getModel().addTreeModelListener(new TreeModificationListener(e -> setSaved(false)));
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(e -> {
            var path = e.getNewLeadSelectionPath();
            if (path == null) {
                edited = null;
                return;
            }
            var node = (TodoNode) path.getLastPathComponent();
            changeEdited(node.getTodo());
        });
        add(new JScrollPane(tree), builder.ipad(3, 3)
                .grid(3, 0)
                .gridheight(5)
                .weight(.4, 1.)
                .anchor(GridBagConstraints.CENTER)
                .fill(GridBagConstraints.BOTH)
                .insets(8, 0, 3, 8)
                .build());

        JButton addNew = new JButton("New");
        addNew.addActionListener(ae -> newRoot());
        add(addNew, builder.ipad(3, 3)
                .grid(3, 5)
                .insets(3, 0, 8, 8)
                .anchor(GridBagConstraints.LINE_START)
                .fill(GridBagConstraints.NONE)
                .build());
    }

    /**
     * Shows a file selection dialog to the user, and if they do not cancel it,
     * it imports the todo list from the file into a new store and sets that
     * as our new list of todos to work with.
     */
    private void loadNewStore() {
        try {
            var dlg = new FileDialog(this, "Nought - Load", FileDialog.LOAD);
            var file = showFileDialog(dlg);
            if (file == null) return; // cancel

            var newStore = new TodoStore();
            var importer = newStore.newImporter();
            importer.importFrom(new FileInputStream(file));

            changeCurrentFile(file);
            store = newStore;
            var todoTree = new TodoTree(store);
            tree.setModel(todoTree);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Could not open file: \n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Saves to the currently opened file.
     * If there is no file open, it behaves as if saveStoreAs was called.
     */
    private void saveStore() {
        if (currentFile == null) {
            saveStoreAs();
            return;
        }

        saveStoreTo(store, currentFile);
    }

    /**
     * Shows a file selection dialog to the user and if they do not cancel it,
     * we save our current list of todos to this file.
     * This file becomes the currently opened file, and any further saves which
     * do not specify a file will be saved to this file.
     */
    private void saveStoreAs() {
        var dlg = new FileDialog(this, "Nought - Save as...", FileDialog.SAVE);
        var file = showFileDialog(dlg);
        if (file == null) return; // cancel

        changeCurrentFile(file);
        saveStoreTo(store, currentFile);
    }

    /**
     * Implementation of saving a store to a file.
     * Creates the store's default exporter class and exports it into the
     * file given as a parameter.
     * If any failures occur, an error message is shown to the user and the
     * save is aborted.
     *
     * @param store The store to save
     * @param file  The file to save to
     */
    private void saveStoreTo(TodoStore store, File file) {
        try {
            var exporter = store.newExporter();
            exporter.export(new FileWriter(file));
            setSaved(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not save to file " + file + ":\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Sets common settings on a file dialog window and presents it to the user.
     * If the user cancels the selection the function returns {@code null},
     * otherwise it returns the full file-path to the selected file.
     * Also filters for files ending in {@code .not}, however, this does not
     * seem to work on Windows.
     *
     * @param dlg The file dialog to show
     * @return The selected file
     */
    private @Nullable File showFileDialog(@NotNull FileDialog dlg) {
        dlg.setFilenameFilter((dir, filename) -> filename.endsWith(".not"));
        dlg.setVisible(true);

        var dir = dlg.getDirectory();
        var filepath = dlg.getFile();
        if (filepath == null) return null;

        if (dir != null) filepath = dir + filepath;
        return new File(filepath);
    }

    /**
     * Adds a new root to the current todo tree and store.
     * The subtree is refreshed as to immediately allow the user to see the changes.
     */
    private void newRoot() {
        var built = getNewTodo();
        if (built == null) return;

        var treeModel = (TodoTree) tree.getModel();
        treeModel.addRootTodo(built);
        treeModel.reload(((TodoNode) treeModel.getRoot()));
    }

    /**
     * Adds a new node to the current todo tree and store, as a child node to
     * whichever node is currently selected for editing.
     * The subtree is refreshed as to immediately allow the user to see the changes.
     */
    private void newChild() {
        var built = getNewTodo();
        if (built == null) return;

        var treeModel = (TodoTree) tree.getModel();
        var node = ((TodoNode) tree.getLastSelectedPathComponent());
        treeModel.addTodoAsChildToNode(node, built, true);
        treeModel.reload(node);
    }

    /**
     * Shows a dialog to the user asking them for a new todo's name and description.
     * If the user fills out this information, a new {@link Todo} object is built,
     * and is returned.
     * If the user cancels the dialog, {@code null} is returned.
     *
     * @return The created todo, or {@code null}
     */
    @Nullable
    private Todo getNewTodo() {
        var todoDlg = new NewTodoDialog(this, store.newBuilder());
        todoDlg.setVisible(true);
        return todoDlg.getBuilt();
    }

    /**
     * Remove the currently selected todo node from the tree, and the associated
     * todo object from the store.
     * If an error occurs, such as trying to create orphan todos, an error
     * message is shown to the user and no action is taken.
     */
    private void deleteSelected() {
        try {
            if (edited != null) {
                var id = edited.getId();
                store.removeById(id);

                var node = (TodoNode) tree.getLastSelectedPathComponent();
                ((DefaultTreeModel) tree.getModel()).removeNodeFromParent(node);
                changeEdited(null);
            }
        } catch (BadTodoOperation ex) {
            JOptionPane.showMessageDialog(this,
                    "Cannot delete node with subtodos.\n" +
                            "See [Todo > Delete with subtodos] for purging a whole subtree.",
                    "Error - Children",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Asks if the user really wants to delete the subtree, and then if they
     * are really sure about this, the selected todo and all its children are
     * removed from the tree and the store.
     * This is a really destructive operation, handle with care.
     */
    private void deleteSelectedTree() {
        if (edited != null) {
            var yes = 0;
            var userSure = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to remove this with all its subtodoes?",
                    "Mass todo removal",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (userSure != yes) return;

            var id = edited.getId();
            store.removeBranchAtId(id);

            var node = (TodoNode) tree.getLastSelectedPathComponent();
            ((DefaultTreeModel) tree.getModel()).removeNodeFromParent(node);
            changeEdited(null);
        }
    }

    /**
     * Basically a setter for the currently open file.
     * Updates all file related redundant information: the file variable
     * and the title.
     *
     * @param file The new file to use
     */
    private void changeCurrentFile(@NotNull File file) {
        changeEdited(null);
        setTitle("Nought - " + file.getName());
        currentFile = file;
    }

    /**
     * Setter for the edited todo.
     * <p>
     * If the edited todo is unset, disables all relevant todo editing
     * functionality, such as the text fields setting different properties.
     * </p>
     * <p>
     * If the edited todo is a valid todo, the values of the text fields are
     * updated to reflect this change and enables editing features.
     * Based on the family situation of the todo, the completion checkbox is
     * also enabled.
     * </p>
     *
     * @param todo The todo to set as the currently edited todo
     */
    private void changeEdited(Todo todo) {
        edited = todo;
        if (edited == null) {
            name.setEnabled(false);
            name.setText("");
            description.setEnabled(false);
            description.setText("");
            dueTime.setEnabled(false);
            dueTime.setText("");
            dueDate.setEnabled(false);
            dueDate.setText("");
            completed.setEnabled(false);
            completed.setSelected(false);
            addSubtodo.setEnabled(false);
            remove.setEnabled(false);
            newTodoMenu.setEnabled(false);
            deleteMenu.setEnabled(false);
            deleteTreeMenu.setEnabled(false);
            return;
        }
        name.setEnabled(true);
        name.setText(edited.getName());
        description.setEnabled(true);
        description.setText(edited.getDescription());
        dueTime.setEnabled(true);
        if (edited.getDueTime() != null) {
            var timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");
            dueTime.setText(edited.getDueTime().format(timeFmt));
        } else {
            dueTime.setText("");
        }
        dueDate.setEnabled(true);
        if (edited.getDueDate() != null) {
            var dateFmt = new SimpleDateFormat("yyyy-MM-dd");
            dueDate.setText(dateFmt.format(edited.getDueDate()));
        } else {
            dueDate.setText("");
        }

        var childrenOk = todo.getChildren().stream()
                .map(id -> store.findById(id))
                .allMatch(Todo::isCompleted);
        var parent = todo.getParent();
        boolean parentOk = true;
        if (parent != null) {
            parentOk = !parent.isCompleted();
        }
        completed.setEnabled(childrenOk && parentOk);
        completed.setSelected(edited.isCompleted());

        addSubtodo.setEnabled(!edited.isCompleted());
        remove.setEnabled(true);
        newTodoMenu.setEnabled(true);
        deleteMenu.setEnabled(true);
        deleteTreeMenu.setEnabled(true);
    }

    /**
     * Returns the currently edited todo object.
     * Used as a lambda object.
     *
     * @return The edited todo
     */
    private Todo getEdited() {
        return edited;
    }

    /**
     * Instructs the jtree to reload itself at the currently selected node.
     * If there is nothing selected, the function is nop.
     */
    private void reloadTreeAtSelected() {
        if (edited != null) {
            ((DefaultTreeModel) tree.getModel()).reload(((TreeNode) tree.getLastSelectedPathComponent()));
        }
    }

    /**
     * Sets the saved status of the currently open todo store.
     * If the saved status is set to true and the title starts with an asterisk,
     * it is removed;
     * otherwise if the new saved status is false and the title does not already
     * have a leading asterisk, it is added.
     *
     * @param saved The new save status of the todo store
     */
    private void setSaved(boolean saved) {
        if (!saved) {
            if (!getTitle().startsWith("*")) {
                setTitle("*" + getTitle());
            }
        } else {
            if (getTitle().startsWith("*")) {
                setTitle(getTitle().substring(1));
            }
        }
        this.saved = saved;
    }

    private boolean saved = true;
    private JMenuItem deleteTreeMenu;
    private JMenuItem deleteMenu;
    private JMenuItem newTodoMenu;
    private TodoStore store;
    private JTree tree;
    private JTextField name;
    private JTextArea description;
    private JTextField dueDate;
    private JTextField dueTime;
    private JCheckBox completed;

    private JButton addSubtodo;
    private JButton remove;
    private Todo edited;
    private File currentFile;
}
