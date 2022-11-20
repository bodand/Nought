package hu.kszi2.nought.gui;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompoundInputVerifier extends InputVerifier {
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
