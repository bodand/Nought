package hu.kszi2.nought.gui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeIntegrityVerifierTest {

    @BeforeEach
    void setUp() {
        timeField = new JTextField();
        dateField = new JTextField();
        verifier = new DateTimeIntegrityVerifier(dateField, timeField);
    }

    @ParameterizedTest
    @CsvSource({
            ",",
            "2022-08-08,",
            "2022-08-08,11:22",
    })
    void verifyImplAcceptsValidCombinations(String date, String time) {
        dateField.setText(date);
        timeField.setText(time);
        assertTrue(verifier.verifyImpl(""));
    }

    @ParameterizedTest
    @CsvSource({
            ",11:22"
    })
    void verifyImplRejectsInvalidCombinations(String date, String time) {
        dateField.setText(date);
        timeField.setText(time);
        assertFalse(verifier.verifyImpl(""));
    }

    JTextField timeField;
    JTextField dateField;
    DateTimeIntegrityVerifier verifier;
}