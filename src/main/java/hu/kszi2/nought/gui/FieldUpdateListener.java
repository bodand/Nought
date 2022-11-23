package hu.kszi2.nought.gui;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Universal document change listener.
 * Takes three parameters:
 * <dl>
 *     <dt><strong>Source</strong></dt>
 *     <dd>
 *         A nullary function that returns a object which can be passed to
 *         the action function.
 *     </dd>
 *     <dt><strong>Input</strong></dt>
 *     <dd>
 *         An input nullary function which returns the input that returns
 *         the string to pass into the consumer function.
 *     </dd>
 *     <dt><strong>Consumer</strong></dt>
 *     <dd>
 *         The function to which the source and input values will be passed
 *         to whenever any change occurs in the listened to document.
 *     </dd>
 * </dl>
 *
 * @param <T> The type of the parameter to pass to the consumer parameter when
 *            invoking it
 */
class FieldUpdateListener<T> implements DocumentListener {

    /**
     * Takes the three callback parameters and stores them in the class.
     *
     * @param source The source supplier
     * @param input  The input supplier
     * @param consumer   The consumer
     */
    public FieldUpdateListener(Supplier<T> source,
                               Supplier<String> input,
                               BiConsumer<T, String> consumer) {
        this.source = source;
        this.input = input;
        this.consumer = consumer;
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

    /**
     * Unified update handler.
     * Calls the source supplier, and if it does not return null,
     * it calls the consumer with the outputs of the two input callbacks.
     */
    private void updateField() {
        var todo = source.get();
        if (todo != null) {
            consumer.accept(todo, input.get());
        }
    }

    private final Supplier<T> source;
    private final Supplier<String> input;
    private final BiConsumer<T, String> consumer;
}
