package hu.kszi2.nought.gui;

public class RootTodoNode extends TodoNode {
    public RootTodoNode() {
        super("Todos");
    }

    @Override
    public boolean completedTodo() {
        return false;
    }

    @Override
    public String toString() {
        return "Todos";
    }
}
