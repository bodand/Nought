package hu.kszi2.nought.io;

import java.io.Writer;

public interface TodoExporter {
    void export(Writer writer) throws Exception;
}
