package hu.kszi2.nought.gui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.Element;

import static org.junit.jupiter.api.Assertions.*;

class FieldUpdateListenerTest {
    private static class DummyDocumentEvent implements DocumentEvent {
        @Override
        public int getOffset() {
            return 0;
        }

        @Override
        public int getLength() {
            return 0;
        }

        @Override
        public Document getDocument() {
            return null;
        }

        @Override
        public EventType getType() {
            return null;
        }

        @Override
        public ElementChange getChange(Element elem) {
            return null;
        }
    }

    @BeforeEach
    void setUp() {
        event = new DummyDocumentEvent();
    }

    @Test
    void insertUpdateCallsCallbackIfSourceIsNotNull() {
        var update = new FieldUpdateListener<>(
                () -> "Source",
                () -> "Input",
                (src, inp) -> {
                    assertEquals("Source", src);
                    assertEquals("Input", inp);
                });
        update.insertUpdate(event);
    }

    @Test
    void insertUpdateDoesNotCallCallbackIfSourceIsNull() {
        final boolean[] called = {false};
        var update = new FieldUpdateListener<>(
                () -> null,
                () -> "Input",
                (src, inp) -> {
                    called[0] = true;
                });
        update.insertUpdate(event);
        assertFalse(called[0]);
    }

    @Test
    void changedUpdateCallsCallbackIfSourceIsNotNull() {
        var update = new FieldUpdateListener<>(
                () -> "Source",
                () -> "Input",
                (src, inp) -> {
                    assertEquals("Source", src);
                    assertEquals("Input", inp);
                });
        update.changedUpdate(event);
    }

    @Test
    void changedUpdateDoesNotCallCallbackIfSourceIsNull() {
        final boolean[] called = {false};
        var update = new FieldUpdateListener<>(
                () -> null,
                () -> "Input",
                (src, inp) -> {
                    called[0] = true;
                });
        update.changedUpdate(event);
        assertFalse(called[0]);
    }

    @Test
    void removeUpdateCallsCallbackIfSourceIsNotNull() {
        var update = new FieldUpdateListener<>(
                () -> "Source",
                () -> "Input",
                (src, inp) -> {
                    assertEquals("Source", src);
                    assertEquals("Input", inp);
                });
        update.removeUpdate(event);
    }

    @Test
    void removeUpdateDoesNotCallCallbackIfSourceIsNull() {
        final boolean[] called = {false};
        var update = new FieldUpdateListener<>(
                () -> null,
                () -> "Input",
                (src, inp) -> {
                    called[0] = true;
                });
        update.removeUpdate(event);
        assertFalse(called[0]);
    }

    DocumentEvent event;
}