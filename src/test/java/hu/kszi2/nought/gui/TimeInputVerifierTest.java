package hu.kszi2.nought.gui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class TimeInputVerifierTest {
    @BeforeEach
    void setUpVerifier() {
        verifier = new TimeInputVerifier();
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {"11:22", "01:30:04", "01:02", "11:12:02Z"})
    void verifyImplAcceptsValidTimeStrings(String time) {
        assertTrue(verifier.verifyImpl(time));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1am", "3pm", "10.10", "one o'clock", "random", "25:12", "11:65"})
    void verifyImplRejectsInvalidTimeStrings(String time) {
        assertFalse(verifier.verifyImpl(time));
    }

    TimeInputVerifier verifier;
}