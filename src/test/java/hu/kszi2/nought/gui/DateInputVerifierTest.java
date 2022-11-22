package hu.kszi2.nought.gui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import static org.junit.jupiter.api.Assertions.*;

class DateInputVerifierTest {
    @BeforeEach
    void setUpVerifier() {
        verifier = new DateInputVerifier();
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {"2022-08-08", "1970-01-01", "2038-01-17", "4420-06-09"})
    void verifyImplAcceptsValidTimeStrings(String time) {
        assertTrue(verifier.verifyImpl(time));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1/2/3", "20/1/03", "20220808", "2022.08.08."})
    void verifyImplRejectsInvalidTimeStrings(String time) {
        assertFalse(verifier.verifyImpl(time));
    }

    DateInputVerifier verifier;
}