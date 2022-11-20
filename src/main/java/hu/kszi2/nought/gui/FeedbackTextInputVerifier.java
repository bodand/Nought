package hu.kszi2.nought.gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public abstract class FeedbackTextInputVerifier extends InputVerifier {
    protected FeedbackTextInputVerifier() {
        defaultTextFieldBorder = new JTextField().getBorder();
        var redHighlight = new LineBorder(Color.RED, 1, true);
        invalidBorder = BorderFactory.createCompoundBorder(defaultTextFieldBorder, redHighlight);
    }

    @Override
    public final boolean verify(JComponent input) {
        var field = ((JTextField) input);
        var txt = field.getText();

        var ret = verifyImpl(txt);
        if (ret) {
            input.setBorder(defaultTextFieldBorder);
        } else {
            input.setBorder(invalidBorder);
        }
        return ret;
    }

    protected abstract boolean verifyImpl(String input);

    private final Border defaultTextFieldBorder;
    private final Border invalidBorder;
}
