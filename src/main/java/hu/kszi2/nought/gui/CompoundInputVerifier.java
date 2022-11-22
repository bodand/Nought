package hu.kszi2.nought.gui;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * InputVerifier that combines a set of different InputVerifiers.
 * Upon verification, they will be checked in the order they were provided in the
 * constructor, and the compound check will only succeed if all inner checks
 * have succeeded.
 */
public class CompoundInputVerifier extends InputVerifier {
    /**
     * Constructs the verifier with a number of InputVerifiers
     *
     * @param verif The verifiers to package together
     */
    public CompoundInputVerifier(InputVerifier... verif) {
        verifiers.addAll(Arrays.stream(verif).toList());
    }

    @Override
    public boolean verify(JComponent input) {
        return verifiers.stream()
                .allMatch(verifier -> verifier.verify(input));
    }

    private final List<InputVerifier> verifiers = new ArrayList<>();
}
