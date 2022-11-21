package hu.kszi2.nought.gui;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

class FieldUpdateListener<T> implements DocumentListener {

    public FieldUpdateListener(Supplier<T> todoSource,
                               Supplier<String> inputLens,
                               BiConsumer<T, String> todoLens) {
        this.todoSource = todoSource;
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
        var todo = todoSource.get();
        if (todo != null) {
            todoLens.accept(todo, inputLens.get());
        }
    }

    private final Supplier<T> todoSource;
    private final Supplier<String> inputLens;
    private final BiConsumer<T, String> todoLens;
}
