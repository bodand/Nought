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
        sax.setFeature("http://xml.org/sax/features/external-general-entities", false);
        sax.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        var parser = sax.newSAXParser();
        parser.parse(strm, this);
        return store;
    }

    /**
     * Constructs an importer, by setting the store to put the parsed todos into, and internally
     * sets up the required element handlers.
     *
     * @param todoStore The store to parse the XML into
     */
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

    /**
     * Parses a given string as a date of the ISO (long) format ({@code yyyy-MM-dd}) and
     * if it did not cause an exception, passes it to the function to add it
     * to the currently parsed todo object.
     *
     * @param dateStr The text to parse as a date
     */
    private void parseDueDate(String dateStr) {
        try {
            var formatter = new SimpleDateFormat("yyyy-MM-dd");
            addDueDate(formatter.parse(dateStr));
        } catch (ParseException ex) {
            /* nop */
        }
    }

    /**
     * Parses a given string as a time of the ISO format ({@code HH:mm:ss}) and
     * if it did not cause an exception, passes it to the function to add it
     * to the currently parsed todo object.
     *
     * @param timeStr The text to parse as a time
     */
    private void parseDueTime(String timeStr) {
        try {
            var formatter = DateTimeFormatter.ISO_TIME;
            addDueTime(LocalTime.parse(timeStr, formatter));
        } catch (DateTimeParseException ex) {
            /* nop */
        }
    }

    /**
     * Handle a given todo elements start.
     *
     * @param id The id of the element encountered.
     */
    private void startTodo(UUID id) {
        builder = store.newBuilder().setId(id);
    }

    /**
     * Finish the handling of a todo object.
     * This uses the previously set state of the importer to produce the appropriate Todo object.
     *
     * @return The newly constructed Todo object.
     * @throws BadTodoOperation If a Todo object could not be constructed while keeping its
     *                          invariants.
     */
    private Todo endTodo() throws BadTodoOperation {
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

    /**
     * Sets the name of the next built todo.
     *
     * @param name The name of the todo
     */
    private void addName(String name) {
        builder.setName(name);
    }

    /**
     * Sets the description of the next built todo.
     *
     * @param desc The description of the todo
     */
    private void addDesc(String desc) {
        builder.setDescription(desc);
    }

    /**
     * Sets the due date of the next built todo.
     *
     * @param date The due date of the todo
     */
    private void addDueDate(Date date) {
        builder.setDueDate(date);
    }

    /**
     * Sets the due time of the next built todo.
     *
     * @param time The due date of the todo
     */
    private void addDueTime(LocalTime time) {
        builder.setDueTime(time);
    }

    /**
     * Sets the completion status of the next built todo.
     *
     * @param completed The due date of the todo
     */
    private void addCompleted(boolean completed) {
        builder.setCompleted(completed);
    }

    private void addCompleted(String s) {
        addCompleted(true);
    }

    /**
     * Adds a child referenced by its identifier to the next built todo.
     *
     * @param cid The child todo's identifier
     */
    private void addChild(UUID cid) {
        builder.addChild(cid);
    }

    /**
     * Checks if there is an element handler specified for the given element,
     * and calls it.
     * Otherwise, the function is nop.
     *
     * @param uri        The Namespace URI, or the empty string if the
     *                   element has no Namespace URI or if Namespace
     *                   processing is not being performed.
     * @param localName  The local name (without prefix), or the
     *                   empty string if Namespace processing is not being
     *                   performed.
     * @param qName      The qualified name (with prefix), or the
     *                   empty string if qualified names are not available.
     * @param attributes The attributes attached to the element.  If
     *                   there are no attributes, it shall be an empty
     *                   Attributes object.
     * @throws SAXException If an XML error occurs
     */
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

    /**
     * If a text callback has been previously set in the importer, calls it with the found list
     * of characters.
     * If the callback does not throw, it is unset again, marking it completed.
     *
     * @param ch     The characters.
     * @param start  The start position in the character array.
     * @param length The number of characters to use from the
     *               character array.
     * @throws SAXException If an XML error occurs
     */
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
