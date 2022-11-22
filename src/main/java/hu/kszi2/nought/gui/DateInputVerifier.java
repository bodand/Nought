package hu.kszi2.nought.gui;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Verifier that verifies if a given input string is a valid date string.
 * The string must be compliant to the ISO (long) date specification:
 * {@code yyyy-MM-dd}.
 */
public class DateInputVerifier extends FeedbackTextInputVerifier {
    /**
     * Verifies if the given input is a valid date according to the ISO
     * standard for writing dates.
     * The empty string is also accepted, to allow a user to empty out a field.
     *
     * @param input The input string to check
     * @return Whether the input was a valid date string
     */
    @Override
    protected boolean verifyImpl(String input) {
        try {
            if (input.isEmpty()) return true; // empty string is a valid date
            format.parse(input);
            return true;
        } catch (ParseException ex) {
            return false;
        }
    }

    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
}
