package hu.kszi2.nought.io;

import hu.kszi2.nought.core.TodoStore;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

/**
 * An interface that defines the behavior of a class that can import a TodoStore
 * from an InputStream.
 */
public interface TodoImporter {
    /**
     * Constructs a default implementation for import to the given store.
     *
     * @param store The store to import to
     * @return An instance of the default implementation of this interface
     */
    @Contract("_ -> new")
    static @NotNull TodoImporter newDefault(TodoStore store) {
        return new TodoXMLImporter(store);
    }

    /**
     * Imports a TodoStore from a given input stream.
     *
     * @param strm The input stream to read from
     * @return The parsed TodoStore object
     * @throws Exception If an error occurs
     */
    TodoStore importFrom(InputStream strm) throws Exception;
}
