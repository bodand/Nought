package hu.kszi2.nought.io;

import hu.kszi2.nought.core.BadTodoOperation;
import hu.kszi2.nought.core.Todo;
import hu.kszi2.nought.core.TodoBuilder;
import hu.kszi2.nought.core.TodoStore;
import hu.kszi2.nought.io.xml.ElementHandler;
import hu.kszi2.nought.io.xml.TextCallback;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TodoXMLImporter
        extends DefaultHandler
        implements TodoImporter {
    @Override
    public TodoStore importFrom(InputStream strm)
            throws ParserConfigurationException, SAXException, IOException {
        var sax = SAXParserFactory.newInstance();
        var parser = sax.newSAXParser();
        parser.parse(strm, this);
        return store;
    }

    public TodoXMLImporter(TodoStore todoStore) {
        store = todoStore;
        elementHandlers.put("todo", attributes -> {
            var id = attributes.getValue("id");
            if (id != null) {
                id = id.substring(1); // chop leading _
                startTodo(UUID.fromString(id));
                return;
            }
            var ref = attributes.getValue("ref");
            if (ref != null) {
                skipEnd = true;
                ref = ref.substring(1);
                addChild(UUID.fromString(ref));
                return;
            }
            throw new SAXException("invalid <todo> element: " +
                    "neither todo definition with id attribute, " +
                    "nor reference with ref attribute");
        });
        elementHandlers.put("name",
                attributes -> nextTextCallback = this::addName);
        elementHandlers.put("desc",
                attributes -> nextTextCallback = this::addDesc);
        elementHandlers.put("date",
                attributes -> nextTextCallback = this::parseDueDate);
        elementHandlers.put("time",
                attributes -> nextTextCallback = this::parseDueTime);
        elementHandlers.put("completed",
                attributes -> nextTextCallback = this::addCompleted);
    }

    private void parseDueDate(String s) {
        try {
            var formatter = new SimpleDateFormat("yyyy-MM-dd");
            addDueDate(formatter.parse(s));
        } catch (ParseException ex) {
            /* nop */
        }
    }

    private void parseDueTime(String s) {
        try {
            var formatter = DateTimeFormatter.ISO_TIME;
            addDueTime(LocalTime.parse(s, formatter));
        } catch (DateTimeParseException ex) {
            /* nop */
        }
    }

    @Override
    public void startTodo(UUID id) {
        builder = store.newBuilder().setId(id);
    }

    @Override
    public Todo endTodo() throws BadTodoOperation {
        if (skipEnd) {
            skipEnd = false;
            return null;
        }
        if (builder != null) {
            var todo = builder.build();
            builder = null;
            return todo;
        }
        return null;
    }

    @Override
    public void addName(String name) {
        builder.setName(name);
    }

    @Override
    public void addDesc(String name) {
        builder.setDescription(name);
    }

    @Override
    public void addDueDate(Date date) {
        builder.setDueDate(date);
    }

    @Override
    public void addDueTime(LocalTime time) {
        builder.setDueTime(time);
    }

    @Override
    public void addCompleted(boolean completed) {
        builder.setCompleted(completed);
    }

    public void addCompleted(String s) {
        addCompleted(true);
    }

    @Override
    public void addChild(UUID cid) {
        builder.addChild(cid);
    }

    @Override
    public void startElement(String uri,
                             String localName,
                             String qName,
                             Attributes attributes) throws SAXException {
        elementHandlers.getOrDefault(qName, ignore -> {
        }).handle(attributes);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        try {
            if (qName.equals("todo")) {
                var todo = endTodo();
                if (todo != null) store.add(todo);
            }
        } catch (Exception ex) {
            throw new SAXException(ex);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        var str = new String(ch, start, length);
        if (nextTextCallback != null) {
            nextTextCallback.process(str);
            nextTextCallback = null;
        }
    }

    private final TodoStore store;
    private TodoBuilder builder;
    private final Map<String, ElementHandler> elementHandlers = new HashMap<>();
    private TextCallback nextTextCallback;
    private boolean skipEnd = false;
}
