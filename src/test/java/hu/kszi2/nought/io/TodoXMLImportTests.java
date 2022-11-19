package hu.kszi2.nought.io;

import hu.kszi2.nought.core.TodoStore;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TodoXMLImportTests {
    @BeforeEach
    void setUpStore() {
        store = new TodoStore();
    }

    @BeforeEach
    void setUpXMLHandler() {
        handler = new TodoXMLImporter(store);
    }

    @BeforeEach
    void setUpParser() throws Exception {
        var sax = SAXParserFactory.newInstance();
        parser = sax.newSAXParser();
    }

    @Test
    void emptyTodosListCreatesEmptyStore() throws Exception {
        var store = mock(TodoStore.class);
        @Language("XML") var data = """
                <?xml version="1.0" encoding="UTF-8" ?>
                <nought xmlns="https://kszi2.hu/~bodand/nought.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    				xsi:schemaLocation="https://kszi2.hu/~bodand/nought.xsd file://nought.xsd">
                    <todos/>
                </nought>""";

        parser.parse(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)),
                handler);
        verify(store, never()).add(notNull());
    }

    @Test
    void invalidTodoElemRaisesAnException() {
        @Language("XML") var data = """
                <?xml version="1.0" encoding="UTF-8" ?>
                <nought xmlns="https://kszi2.hu/~bodand/nought.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:schemaLocation="https://kszi2.hu/~bodand/nought.xsd file://nought.xsd">
                    <todos>
                        <todo>
                	        <name>Todo 1</name>
                	        <name>Description</name>
                        </todo>
                    </todos>
                </nought>""";

        var iae = assertThrows(SAXException.class,
                () -> parser.parse(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)),
                        handler));
        assertTrue(iae.getMessage().contains("id attribute"));
    }

    @Test
    void todoElemWithInsufficientChildrenRaisesAnException() {
        @Language("XML") var data = """
                <?xml version="1.0" encoding="UTF-8" ?>
                <nought xmlns="https://kszi2.hu/~bodand/nought.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:schemaLocation="https://kszi2.hu/~bodand/nought.xsd file://nought.xsd">
                    <todos>
                        <todo id="_60f2abf2-f76d-49f9-a4a5-c87b13a9cbbc">
                	        <name>Todo 1</name>
                        </todo>
                    </todos>
                </nought>""";

        var iae = assertThrows(SAXException.class,
                () -> parser.parse(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)),
                        handler));
        assertTrue(iae.getMessage().contains("required setters"));
    }

    @Test
    void todoStoreAddWasCalledWithCorrectGUID() throws Exception {
        @Language("XML") var data = """
                <?xml version="1.0" encoding="UTF-8" ?>
                <nought xmlns="https://kszi2.hu/~bodand/nought.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:schemaLocation="https://kszi2.hu/~bodand/nought.xsd file://nought.xsd">
                    <todos>
                        <todo id="_60f2abf2-f76d-49f9-a4a5-c87b13a9cbbc">
                	        <name>Todo 1</name>
                	        <desc>Todo text</desc>
                        </todo>
                    </todos>
                </nought>""";
        var guid = UUID.fromString("60f2abf2-f76d-49f9-a4a5-c87b13a9cbbc");

        parser.parse(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)),
                handler);
        var todo = assertDoesNotThrow(() -> store.findById(guid));
        assertEquals("Todo 1", todo.getName());
        assertEquals("Todo text", todo.getDescription());
    }

    @Test
    void todoStoreContainsCorrectChildrenAfterParsing() throws Exception {
        @Language("XML") var data = """
                <?xml version="1.0" encoding="UTF-8" ?>
                <nought xmlns="https://kszi2.hu/~bodand/nought.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:schemaLocation="https://kszi2.hu/~bodand/nought.xsd file://nought.xsd">
                    <todos>
                        <todo id="_60f2abf2-f76d-49f9-a4a5-c87b13a9cbbc">
                	        <name>Todo 1</name>
                	        <desc>Todo text</desc>
                        </todo>
                        <todo id="_80a41331-54a6-49a1-b8da-97c651a8110b">
                	        <name>Todo 2</name>
                	        <desc>More todo text</desc>
                	        <depends-on>
                	            <todo ref="_60f2abf2-f76d-49f9-a4a5-c87b13a9cbbc"/>
                	        </depends-on>
                        </todo>
                    </todos>
                </nought>""";
        var parentGuid = UUID.fromString("80a41331-54a6-49a1-b8da-97c651a8110b");
        var childGuid = UUID.fromString("60f2abf2-f76d-49f9-a4a5-c87b13a9cbbc");

        parser.parse(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)),
                handler);

        var child = assertDoesNotThrow(() -> store.findById(childGuid));
        var parent = assertDoesNotThrow(() -> store.findById(parentGuid));

        assertEquals(parentGuid, child.getParentId());
        assertSame(parent, child.getParent());
    }

    @Test
    void todoStoreHasSetCompleted() throws Exception {
        @Language("XML") var data = """
                <?xml version="1.0" encoding="UTF-8" ?>
                <nought xmlns="https://kszi2.hu/~bodand/nought.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:schemaLocation="https://kszi2.hu/~bodand/nought.xsd file://nought.xsd">
                    <todos>
                        <todo id="_60f2abf2-f76d-49f9-a4a5-c87b13a9cbbc">
                	        <name>Incomplete</name>
                	        <desc>Todo text</desc>
                        </todo>
                        <todo id="_a36671e7-1b96-4af9-ab55-8dbae5fda273">
                            <name>Complete</name>
                            <desc>Good :)</desc>
                            <completed />
                        </todo>
                    </todos>
                </nought>""";
        var incompleteGuid = UUID.fromString("60f2abf2-f76d-49f9-a4a5-c87b13a9cbbc");
        var completeGuid = UUID.fromString("a36671e7-1b96-4af9-ab55-8dbae5fda273");

        parser.parse(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)),
                handler);
        var incomplete = assertDoesNotThrow(() -> store.findById(incompleteGuid));
        var complete = assertDoesNotThrow(() -> store.findById(completeGuid));

        assertFalse(incomplete.isCompleted());
        assertTrue(complete.isCompleted());
    }

    @Test
    void todoStoreAddWasCalledWithCorrectDueDateTime() throws Exception {
        @Language("XML") var data = """
                <?xml version="1.0" encoding="UTF-8" ?>
                <nought xmlns="https://kszi2.hu/~bodand/nought.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:schemaLocation="https://kszi2.hu/~bodand/nought.xsd file://nought.xsd">
                    <todos>
                        <todo id="_60f2abf2-f76d-49f9-a4a5-c87b13a9cbbc">
                	        <name>Todo 1</name>
                	        <desc>Todo text</desc>
                            <due>
                				<date>2022-11-07</date>
                				<time>23:59:00</time>
                			</due>
                        </todo>
                    </todos>
                </nought>""";
        var guid = UUID.fromString("60f2abf2-f76d-49f9-a4a5-c87b13a9cbbc");

        parser.parse(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)),
                handler);
        var todo = assertDoesNotThrow(() -> store.findById(guid));
        var formatter = new SimpleDateFormat("yyyy-MM-dd");
        var expDate = formatter.parse("2022-11-07");

        assertEquals(expDate, todo.getDueDate());
        assertEquals(LocalTime.of(23, 59),
                todo.getDueTime());
    }

    @Test
    void todoStoreDoesNotSetIncorrectlyFormattedDate() throws Exception {
        @Language("XML") var data = """
                <?xml version="1.0" encoding="UTF-8" ?>
                <nought xmlns="https://kszi2.hu/~bodand/nought.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:schemaLocation="https://kszi2.hu/~bodand/nought.xsd file://nought.xsd">
                    <todos>
                        <todo id="_60f2abf2-f76d-49f9-a4a5-c87b13a9cbbc">
                	        <name>Todo 1</name>
                	        <desc>Todo text</desc>
                            <due>
                				<date>7/11/2022</date>
                			</due>
                        </todo>
                    </todos>
                </nought>""";
        var guid = UUID.fromString("60f2abf2-f76d-49f9-a4a5-c87b13a9cbbc");

        parser.parse(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)),
                handler);
        var todo = assertDoesNotThrow(() -> store.findById(guid));

        assertNull(todo.getDueDate());
    }

    @Test
    void todoStoreDoesNotSetIncorrectlyFormattedTime() throws Exception {
        @Language("XML") var data = """
                <?xml version="1.0" encoding="UTF-8" ?>
                <nought xmlns="https://kszi2.hu/~bodand/nought.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:schemaLocation="https://kszi2.hu/~bodand/nought.xsd file://nought.xsd">
                    <todos>
                        <todo id="_60f2abf2-f76d-49f9-a4a5-c87b13a9cbbc">
                	        <name>Todo 1</name>
                	        <desc>Todo text</desc>
                            <due>
                				<date>2022-11-07</date>
                				<time>2022-11-07</time>
                			</due>
                        </todo>
                    </todos>
                </nought>""";
        var guid = UUID.fromString("60f2abf2-f76d-49f9-a4a5-c87b13a9cbbc");

        parser.parse(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)),
                handler);
        var todo = assertDoesNotThrow(() -> store.findById(guid));

        assertNull(todo.getDueTime());
    }

    private TodoStore store;
    private DefaultHandler handler;
    private SAXParser parser;

}
