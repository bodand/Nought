package hu.kszi2.nought.gui;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Verifier that verifies if a given input string is a valid time string.
 * The string must be compliant to the ISO specification, briefly
 * {@code HH:mm[:ss]}, but the complete specification is supported.
 */
public class TimeInputVerifier extends FeedbackTextInputVerifier {
    /**
     * Verifies if the given input is a valid time according to the ISO
     * standard for writing times.
     * The empty string is also accepted, to allow a user to empty out a field.
     *
     * @param input The input string to check
     * @return Whether the input was a valid time string
     */
    @Override
    protected boolean verifyImpl(String input) {
        try {
            if (input.isEmpty()) return true; // empty string is a valid time
            format.parse(input);
            return true;
        } catch (DateTimeParseException ex) {
            return false;
        }
    }

    private static final DateTimeFormatter format = DateTimeFormatter.ISO_TIME;
}
