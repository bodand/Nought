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

public class MainFrame extends JFrame {
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

    private void constructTodoEditor() {
        var gbc = new GridBagConstraints();
        gbc.ipadx = 3;
        gbc.ipady = 3;

        gbc.insets = new Insets(8, 8, 3, 8);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.weightx = 0.0;
        add(new JLabel("Name"), gbc.clone());
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.gridwidth = 2;
        gbc.weightx = .6;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        name = new JTextField();
        name.getDocument().addDocumentListener(new FieldUpdateListener<>(this::getEdited, name::getText,
                (todo, value) -> {
                    todo.setName(value);
                    reloadTreeAtSelected();
                }));
        add(name, gbc.clone());

        gbc.insets = new Insets(3, 8, 3, 8);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        add(new JLabel("Description"), gbc.clone());
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.gridwidth = 2;
        gbc.weightx = .6;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        description = new JTextArea();
        description.getDocument().addDocumentListener(new FieldUpdateListener<>(this::getEdited, description::getText, Todo::setDescription));
        add(new JScrollPane(description), gbc.clone());

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridheight = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        add(new JLabel("Due"), gbc.clone());
        gbc.gridx = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        add(new JLabel("Date"), gbc.clone());
        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.weightx = .6;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        dueDate = new JTextField();
        dueDate.getDocument().addDocumentListener(new FieldUpdateListener<>(this::getEdited, dueDate::getText,
                (todo, value) -> {
                    try {
                        todo.setDueDate(value);
                    } catch (Exception ex) {
                        /* ignore */
                    }
                }));
        add(dueDate, gbc.clone());
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.weightx = 0.0;
        add(new JLabel("Time"), gbc.clone());
        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.weightx = .6;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        dueTime = new JTextField();
        dueTime.getDocument().addDocumentListener(new FieldUpdateListener<>(this::getEdited, dueTime::getText,
                (todo, value) -> {
                    try {
                        todo.setDueTime(value);
                    } catch (Exception ex) {
                        /* ignore */
                    }
                }));
        add(dueTime, gbc.clone());

        dueTime.setInputVerifier(new CompoundInputVerifier(
                new TimeInputVerifier(),
                new DateTimeIntegrityVerifier(dueDate, dueTime)
        ));
        dueDate.setInputVerifier(new CompoundInputVerifier(
                new DateInputVerifier(),
                new DateTimeIntegrityVerifier(dueDate, dueTime)
        ));

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
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
        add(completed, gbc.clone());

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 8, 8, 8);
        addSubtodo = new JButton("Add subtodo");
        addSubtodo.addActionListener(ae -> newChild());
        add(addSubtodo, gbc.clone());
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.2;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        remove = new JButton("Remove");
        remove.addActionListener(ae -> deleteSelected());
        add(remove, gbc);
    }

    private void constructTodoExplorer() {
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
        add(new JScrollPane(tree),
                new GridBagConstraints(3, 0, 1, 5, 0.4, 1.0,
                        GridBagConstraints.CENTER,
                        GridBagConstraints.BOTH,
                        new Insets(8, 0, 3, 8),
                        0, 0));

        JButton addNew = new JButton("New");
        addNew.addActionListener(ae -> newRoot());
        add(addNew,
                new GridBagConstraints(3, 5, 1, 1, 0.0, 0.0,
                        GridBagConstraints.LINE_START,
                        GridBagConstraints.NONE,
                        new Insets(3, 0, 8, 8),
                        0, 0));
    }

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

    private void saveStore() {
        if (currentFile == null) {
            saveStoreAs();
            return;
        }

        saveStoreTo(store, currentFile);
    }

    private void saveStoreAs() {
        var dlg = new FileDialog(this, "Nought - Save as...", FileDialog.SAVE);
        var file = showFileDialog(dlg);
        if (file == null) return; // cancel

        changeCurrentFile(file);
        saveStoreTo(store, currentFile);
    }

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

    private @Nullable File showFileDialog(@NotNull FileDialog dlg) {
        dlg.setFilenameFilter((dir, filename) -> filename.endsWith(".not"));
        dlg.setVisible(true);

        var dir = dlg.getDirectory();
        var filepath = dlg.getFile();
        if (filepath == null) return null;

        if (dir != null) filepath = dir + filepath;
        return new File(filepath);
    }

    private void newRoot() {
        var built = getNewTodo();
        if (built == null) return;

        var treeModel = (TodoTree) tree.getModel();
        treeModel.addRootTodo(built);
        treeModel.reload();
    }

    private void newChild() {
        var built = getNewTodo();
        if (built == null) return;

        var treeModel = (TodoTree) tree.getModel();
        var node = ((TodoNode) tree.getLastSelectedPathComponent());
        treeModel.addTodoAsChildToNode(node, built, true);
        treeModel.reload();
    }

    @Nullable
    private Todo getNewTodo() {
        var todoDlg = new NewTodoDialog(this, store.newBuilder());
        todoDlg.setLocationRelativeTo(this);
        todoDlg.setVisible(true);
        return todoDlg.getBuilt();
    }

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

    private void changeCurrentFile(@NotNull File file) {
        changeEdited(null);
        var titleBuilder = new StringBuilder();
        if (!saved) titleBuilder.append("*");
        titleBuilder.append("Nought - ");
        titleBuilder.append(file.getName());
        setTitle(titleBuilder.toString());
        currentFile = file;
    }

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

    public Todo getEdited() {
        return edited;
    }

    public void reloadTreeAtSelected() {
        if (edited != null) {
            ((DefaultTreeModel) tree.getModel()).reload(((TreeNode) tree.getLastSelectedPathComponent()));
        }
    }

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
