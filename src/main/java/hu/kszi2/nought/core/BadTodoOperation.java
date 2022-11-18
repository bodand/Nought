package hu.kszi2.nought.core;

public class BadTodoOperation extends Exception {
    public BadTodoOperation(Todo todo, String message) {
        super(makeMessage(todo, message));
    }

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
