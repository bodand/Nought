package hu.kszi2.nought.gui;

import hu.kszi2.nought.core.Todo;
import hu.kszi2.nought.core.TodoStore;
import org.jetbrains.annotations.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class MainFrame extends JFrame {
    private class FieldUpdateListener implements DocumentListener {
        public FieldUpdateListener(Supplier<String> inputLens,
                                   BiConsumer<Todo, String> todoLens) {
            this.inputLens = inputLens;
            this.todoLens = todoLens;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            updateField();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            updateField();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            updateField();
        }

        private void updateField() {
            if (edited != null) {
                todoLens.accept(edited, inputLens.get());
                ((DefaultTreeModel) tree.getModel()).reload(((TreeNode) tree.getLastSelectedPathComponent()));
            }
        }

        private final Supplier<String> inputLens;
        private final BiConsumer<Todo, String> todoLens;
    }

    public MainFrame(TodoStore store) {
        this.store = store;
        try {
            setLayout(new GridBagLayout());

            constructMenuBar();
            constructTodoEditor();
            constructTodoExplorer();

            setTitle("Nought");
            setSize(700, 350);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setVisible(true);
            changeEdited(null);
        } catch (Exception ex) {
            System.err.println("error: cannot initialize Nought: ");
            ex.printStackTrace();
        }
    }

    private void constructMenuBar() {
        var mb = new MenuBar();
        var fileMenu = new Menu("File");
        var todoMenu = new Menu("Todo");

        // File //
        // File > Save
        var save = new MenuItem("Save");
        save.addActionListener(ae -> saveStore());
        fileMenu.add(save);
        // File > SaveAs
        var saveAs = new MenuItem("Save as");
        saveAs.addActionListener(ae -> saveStoreAs());
        fileMenu.add(saveAs);
        // --
        fileMenu.add("-");
        // File > Load
        var load = new MenuItem("Load");
        load.addActionListener(ae -> loadNewStore());
        fileMenu.add(load);
        // --
        fileMenu.add("-");
        // File > Exit
        var exit = new MenuItem("Exit");
        exit.addActionListener(ae -> dispose());
        fileMenu.add(exit);

        // Todo //
        // Todo > New
        var newRoot = new MenuItem("New");
        newRoot.addActionListener(ae -> newRoot());
        todoMenu.add(newRoot);
        // Todo > New dependent
        var newTodo = new MenuItem("New dependent");
        newTodo.addActionListener(ae -> newChild());
        todoMenu.add(newTodo);
        // --
        todoMenu.add("-");
        // Todo > Delete
        var delete = new MenuItem("Delete");
        delete.addActionListener(ae -> deleteSelected());
        todoMenu.add(delete);
        // Todo > Delete with children
        var deleteTree = new MenuItem("Delete with children");
        deleteTree.addActionListener(ae -> deleteSelectedTree());
        todoMenu.add(deleteTree);

        mb.add(fileMenu);
        mb.add(todoMenu);
        setMenuBar(mb);
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
        name.getDocument().addDocumentListener(new FieldUpdateListener(name::getText, Todo::setName));
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
        description.getDocument().addDocumentListener(new FieldUpdateListener(description::getText, Todo::setDescription));
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
        dueDate.getDocument().addDocumentListener(new FieldUpdateListener(dueDate::getText,
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
//        dueTime.getDocument().addDocumentListener(new FieldUpdateListener(dueTime::getText, Todo::setName));
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
        add(completed, gbc.clone());

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 8, 8, 8);
        addSubtodo = new JButton("Add subtodo");
        add(addSubtodo, gbc.clone());
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0.2;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        remove = new JButton("Remove");
        add(remove, gbc);
    }

    private void constructTodoExplorer() {
        tree = new JTree(new TodoTree(store));
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

        addNew = new JButton("New");
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
        // #TODO implement
    }

    private void newChild() {
        // #TODO implement
    }

    private void deleteSelected() {
        // #TODO implement
    }

    private void deleteSelectedTree() {
        // #TODO implement
    }

    private void changeCurrentFile(@NotNull File file) {
        setTitle("Nought - " + file.getName());
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
            addNew.setEnabled(false);
            addSubtodo.setEnabled(false);
            remove.setEnabled(false);
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
        completed.setEnabled(true);
        completed.setSelected(edited.isCompleted());

        addNew.setEnabled(true);
        addSubtodo.setEnabled(true);
        remove.setEnabled(true);
    }

    private transient TodoStore store;
    private JTree tree;
    private JTextField name;
    private JTextArea description;
    private JTextField dueDate;
    private JTextField dueTime;
    private JCheckBox completed;

    private JButton addSubtodo;
    private JButton remove;
    private JButton addNew;
    private transient Todo edited;
    private File currentFile;
}
