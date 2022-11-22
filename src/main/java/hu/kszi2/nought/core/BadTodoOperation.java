package hu.kszi2.nought.core;

/**
 * Exception class thrown, whenever an invalid operation would be performed
 * on a Todo instance.
 *
 * @see Todo
 */
public class BadTodoOperation extends Exception {
    /**
     * Constructs a BadTodoOperation instance using the instance of the Todo
     * class that caused the failure, and a custom error message.
     *
     * @param todo    The instance that the bad operation was invoked on
     * @param message Custom error message
     * @see Todo
     */
    public BadTodoOperation(Todo todo, String message) {
        super(makeMessage(todo, message));
    }

    /**
     * Constructs a string error message from the Todo instance and the custom
     * error string.
     * The error message contains the hierarchy until the Todo instance (all its parents)
     * after the custom error string, like a stack trace.
     *
     * @param todo    The todo on which the bad operation took place
     * @param message The custom error string
     * @return The formatted error message
     *
     * @see Todo
     */
    private static String makeMessage(Todo todo, String message) {
        StringBuilder builder = new StringBuilder();
        var name = todo.getName();

        builder.append("Error in Todo '");
        builder.append(name);
        builder.append("': ");
        builder.append(message);

        builder.append("\nTodoTrace:\n");
        var parent = todo.getParent();
        while (parent != null) {
            builder.append("note: in '");
            builder.append(name);
            builder.append("' child of '");
            name = parent.getName();
            builder.append(name);
            builder.append("'\n");
            parent = parent.getParent();
        }

        return builder.toString();
    }
}
