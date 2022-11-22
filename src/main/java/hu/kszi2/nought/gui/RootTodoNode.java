package hu.kszi2.nought.gui;

/**
 * Special TodoNode which represents the up-most root element of the tree.
 * This is needed for the JTree can only render trees and not forests, hence
 * by taking all the subtrees in the forest and adding them under a common root
 * node, we make them into one tree.
 * This is what this class does.
 */
public class RootTodoNode extends TodoNode {
    /**
     * Constructs a RootTodoNode by passing a string to the superclass of
     * TodoNode using the protected constructor.
     */
    public RootTodoNode() {
        super("Todos");
    }

    /**
     * Whether the todo is completed. Since this is not a real todo, it is
     * always false.
     *
     * @return False.
     */
    @Override
    public boolean completedTodo() {
        return false;
    }

    /**
     * The presented name of the object.
     *
     * @return {@code "Todos"}
     */
    @Override
    public String toString() {
        return "Todos";
    }
}
