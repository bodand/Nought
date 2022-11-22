package hu.kszi2.nought.io;

import hu.kszi2.nought.core.TodoStore;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.Writer;

/**
 * An interface representing classes that can export Todos to a given Writer.
 */
public interface TodoExporter {
    /**
     * Constructs a default implementation for exporting the given store.
     *
     * @param store The store to export
     * @return An instance of the default implementation of this interface
     */
    @Contract("_ -> new")
    static @NotNull TodoExporter newDefault(TodoStore store) {
        return new TodoXMLExporter(store);
    }

    /**
     * Exports todos to a given writer stream.
     *
     * @param writer The writer stream to export the todos to.
     * @throws Exception If an error occurs.
     */
    void export(Writer writer) throws Exception;
}
