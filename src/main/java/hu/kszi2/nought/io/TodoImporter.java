package hu.kszi2.nought.io;

import hu.kszi2.nought.core.BadTodoOperation;

import java.time.LocalTime;
import java.util.Date;
import java.util.UUID;

public interface TodoImporter<T> {
    void startTodo(UUID id);

    T endTodo() throws BadTodoOperation;

    void addName(String name);

    void addDesc(String name);

    void addDueDate(Date date);

    void addDueTime(LocalTime time);

    void addCompleted(boolean completed);

    void addChild(UUID cid);
}
