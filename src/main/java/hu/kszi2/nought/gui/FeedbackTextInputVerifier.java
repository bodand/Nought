package hu.kszi2.nought.gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * InputVerifier class for JTextField objects that sets a red border around
 * the input if the value is invalid.
 * This way this class provides some feedback to the user when they try to input
 * invalid data, instead of just trapping them in the input box without explanation.
 */
public abstract class FeedbackTextInputVerifier extends InputVerifier {
    /**
     * Constructor that sets the internal state up for use.
     */
    protected FeedbackTextInputVerifier() {
        defaultTextFieldBorder = new JTextField().getBorder();
        var redHighlight = new LineBorder(Color.RED, 1, true);
        invalidBorder = BorderFactory.createCompoundBorder(defaultTextFieldBorder, redHighlight);
    }

    /**
     * Checks if the provided input component's string is valid according to the
     * logic implemented in the subclasses.
     * If it is, it changes the border of the input back to the original, while
     * it is not, it changes it to a modified border which also has a small
     * red border around the text.
     *
     * @param input the JComponent to verify
     * @return Whether the component has valid text
     */
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

    /**
     * Abstract function used to preform the actual check on the input string.
     *
     * @param input The input string
     * @return Whether the input was valid
     */
    protected abstract boolean verifyImpl(String input);

    private final Border defaultTextFieldBorder;
    private final Border invalidBorder;
}
