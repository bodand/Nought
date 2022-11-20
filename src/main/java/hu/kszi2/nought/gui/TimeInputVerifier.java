package hu.kszi2.nought.gui;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TimeInputVerifier extends FeedbackTextInputVerifier {
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
