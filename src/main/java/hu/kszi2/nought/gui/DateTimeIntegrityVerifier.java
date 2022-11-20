package hu.kszi2.nought.gui;

import javax.swing.*;

public class DateTimeIntegrityVerifier extends FeedbackTextInputVerifier {
    public DateTimeIntegrityVerifier(JTextField dateField, JTextField timeField) {
        this.dateField = dateField;
        this.timeField = timeField;
    }

    @Override
    protected boolean verifyImpl(String input) {
        var date = dateField.getText();
        var time = timeField.getText();
        if (date.isEmpty()) {
            return time.isEmpty();
        } else {
            return true;
        }
    }

    private final JTextField dateField;
    private final JTextField timeField;
}
