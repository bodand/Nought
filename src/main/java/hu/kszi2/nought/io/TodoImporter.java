package hu.kszi2.nought.io;

import hu.kszi2.nought.core.BadTodoOperation;
import hu.kszi2.nought.core.Todo;
import hu.kszi2.nought.core.TodoStore;

import java.io.InputStream;
import java.time.LocalTime;
import java.util.Date;
import java.util.UUID;

public interface TodoImporter {
    TodoStore importFrom(InputStream strm) throws Exception;

    void startTodo(UUID id);

    Todo endTodo() throws BadTodoOperation;

    void addName(String name);

    void addDesc(String name);

    void addDueDate(Date date);

    void addDueTime(LocalTime time);

    void addCompleted(boolean completed);

    void addChild(UUID cid);
}
