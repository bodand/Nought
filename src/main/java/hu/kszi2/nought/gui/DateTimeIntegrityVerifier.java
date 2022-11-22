package hu.kszi2.nought.gui;

import javax.swing.*;

/**
 * Checks if two text fields (date and time) are provided in a legal order.
 * That is, time can only have a value if date already has a nonempty string.
 * The fields to check are set at construction, the value passed to the
 * verification function is irrelevant.
 */
public class DateTimeIntegrityVerifier extends FeedbackTextInputVerifier {
    /**
     * Constructs the verifier for these two provided text fields.
     *
     * @param dateField The date field to check
     * @param timeField The time field to check. This can only have a nonempty
     *                  value if dateField already has one as well.
     */
    public DateTimeIntegrityVerifier(JTextField dateField, JTextField timeField) {
        this.dateField = dateField;
        this.timeField = timeField;
    }

    /**
     * Verifies that the wanted order of assignment is held among the two
     * text fields.
     *
     * @param input The input string
     * @return Whether they are in a legal state
     */
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
