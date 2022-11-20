package hu.kszi2.nought.gui;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DateInputVerifier extends FeedbackTextInputVerifier {
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
