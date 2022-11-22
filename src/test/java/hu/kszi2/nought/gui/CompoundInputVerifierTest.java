package hu.kszi2.nought.gui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.*;

class CompoundInputVerifierTest {

    @BeforeEach
    void setUp() {
        field = new JTextField();
        verifier = new CompoundInputVerifier(
                new DateInputVerifier(),
                new TimeInputVerifier());
    }

    @Test
    void verifyOnlySucceedsIfAllInnerElementsDo() {
        field.setText(""); // the only input on which Date and Time both succeed
        assertTrue(verifier.verify(field));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2022-08-08", "12:34", "neither"})
    void verifyFailsIfAnyCheckFails(String text) {
        field.setText(text);
        assertFalse(verifier.verify(field));
    }

    CompoundInputVerifier verifier;
    JTextField field;
}