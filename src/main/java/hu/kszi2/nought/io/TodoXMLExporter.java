package hu.kszi2.nought.io;

import hu.kszi2.nought.core.Todo;
import hu.kszi2.nought.core.TodoStore;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.xml.stream.*;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TodoXMLExporter implements TodoExporter {
    public static final String NOUGHT_NAMESPACE = "https://kszi2.hu/~bodand/nought.xsd";

    public TodoXMLExporter(TodoStore store) {
        this.store = store;
    }

    @Override
    public void export(Writer writer) throws XMLStreamException {
        var factory = XMLOutputFactory.newFactory();
        stream = factory.createXMLStreamWriter(writer);

        stream.setDefaultNamespace(NOUGHT_NAMESPACE);
        stream.writeStartDocument();
        stream.writeStartElement(NOUGHT_NAMESPACE, "nought");
        stream.writeDefaultNamespace(NOUGHT_NAMESPACE);
        stream.writeStartElement(NOUGHT_NAMESPACE, "todos");

        var it = store.iterator();
        while (it.hasNext()) {
            addTodoOutput(it.next());
        }

        stream.writeEndDocument(); //</todos></nought>
        stream.close();

        stream = null;
        written.clear();
    }

    private void addTodoOutput(@NotNull Todo todo) throws XMLStreamException {
        var children = todo.getChildren();
        if (wroteAllChildren(children)) {
            writeTodo(todo);
            var parent = todo.getParent();
            if (parent != null) {
                addTodoOutput(parent);
            }
        }
    }

    @Contract(pure = true)
    private @NotNull String formatGuid(@NotNull UUID id) {
        return "_" + id;
    }

    private boolean wroteAllChildren(List<UUID> cids) {
        return written.containsAll(cids);
    }

    private void writeTodo(@NotNull Todo todo) throws XMLStreamException {
        if (written.contains(todo.getId())) return;

        written.add(todo.getId());
        stream.writeStartElement(NOUGHT_NAMESPACE, "todo");
        stream.writeAttribute(NOUGHT_NAMESPACE, "id", formatGuid(todo.getId()));
        // 1. name
        writeTodoField("name", todo.getName());
        // 2. desc
        writeTodoField("desc", todo.getDescription());
        // 3. completed?
        if (todo.isCompleted()) {
            stream.writeEmptyElement(NOUGHT_NAMESPACE, "completed");
        }
        // 4. depends-on?
        writeChildren(todo);
        // 5. due?
        writeDue(todo);

        stream.writeEndElement();
    }

    private void writeChildren(@NotNull Todo todo) throws XMLStreamException {
        var children = todo.getChildren();
        if (children.isEmpty()) return;

        stream.writeStartElement(NOUGHT_NAMESPACE, "depends-on");
        for (var child : children) {
            writeTodoRef(child);
        }
        stream.writeEndElement();
    }

    private void writeTodoRef(UUID id) throws XMLStreamException {
        stream.writeEmptyElement(NOUGHT_NAMESPACE, "todo");
        stream.writeAttribute("ref", formatGuid(id));
    }

    private void writeTodoField(String field, String data) throws XMLStreamException {
        if (data == null) return;
        stream.writeStartElement(NOUGHT_NAMESPACE, field);
        stream.writeCharacters(data);
        stream.writeEndElement();
    }

    private void writeDue(@NotNull Todo todo) throws XMLStreamException {
        var date = todo.getDueDate();
        var time = todo.getDueTime();
        if (date == null) return;

        stream.writeStartElement(NOUGHT_NAMESPACE, "due");
        // 1. date
        writeTodoField("date", formatDate(date));
        // 2. time?
        writeTodoField("time", formatTime(time));

        stream.writeEndElement();
    }

    private String formatTime(LocalTime time) {
        if (time == null) return null;
        var format = DateTimeFormatter.ofPattern("HH:mm:ss");
        return format.format(time);
    }

    private String formatDate(Date date) {
        if (date == null) return null;
        var format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }

    private XMLStreamWriter stream;
    private final TodoStore store;
    private final Set<UUID> written = new HashSet<>();
}
