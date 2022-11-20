package hu.kszi2.nought.gui;

import javax.swing.*;
import javax.swing.border.StrokeBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DateInputVerifier extends FeedbackTextInputVerifier {
    @Override
    protected boolean verifyImpl(String input) {
        try {
            format.parse(input);
            return true;
        } catch (ParseException ex) {
            return false;
        }
    }

    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
}
