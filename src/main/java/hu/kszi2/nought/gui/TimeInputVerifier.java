package hu.kszi2.nought.gui;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TimeInputVerifier extends FeedbackTextInputVerifier {
    @Override
    protected boolean verifyImpl(String input) {
        try {
            format.parse(input);
            return true;
        } catch (DateTimeParseException ex) {
            return false;
        }
    }

    private final DateTimeFormatter format = DateTimeFormatter.ISO_TIME;
}
