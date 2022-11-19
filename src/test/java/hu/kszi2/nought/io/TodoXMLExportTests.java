package hu.kszi2.nought.io;

import hu.kszi2.nought.core.TodoStore;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Date;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TodoXMLExportTests {
    @BeforeAll
    static void compileValidationSchema() throws SAXException {
        var xsd = ClassLoader.getSystemResource("nought.xsd");
        schema = schemaFactory.newSchema(xsd);
    }

    @BeforeEach
    void setUpValidator() {
        if (validator == null) {
            validator = schema.newValidator();
        } else {
            validator.reset();
        }
    }

    @BeforeEach
    void setUpStoreAndExporter() {
        store = new TodoStore();
        exporter = store.newExporter();
    }

    @Test
    void emptyStoreCreatesValidXMLFile() {
        var xmlSrc = createXmlSource();

        assertDoesNotThrow(() -> validator.validate(xmlSrc));
    }

    @Test
    void singleChildlessTodoStoreCreatesValidXMLFile() throws Exception {
        var todo = store.newBuilder().newId().setName("test").setDescription("what").build();
        store.add(todo);
        var xmlSrc = createXmlSource();

        assertDoesNotThrow(() -> validator.validate(xmlSrc));
    }

    @Test
    void singleChildlessTodoStoreContainsTodosName() throws Exception {
        var todo = store.newBuilder()
                .newId().setName("test").setDescription("what").build();
        store.add(todo);
        var xml = createXmlString();

        assertTrue(xml.contains(todo.getName()));
    }

    @Test
    void singleChildlessTodoStoreContainsTodosDescription() throws Exception {
        var todo = store.newBuilder()
                .newId().setName("test").setDescription("what").build();
        store.add(todo);
        var xml = createXmlString();

        assertTrue(xml.contains(todo.getDescription()));
    }

    @Test
    void singleCompletedChildlessTodoStoreCreatesValidXMLFile() throws Exception {
        var todo = store.newBuilder()
                .newId().setName("test").setDescription("what").setCompleted(true).build();
        store.add(todo);
        var xmlSrc = createXmlSource();

        assertDoesNotThrow(() -> validator.validate(xmlSrc));
    }

    @Test
    void singleCompletedChildlessTodoStoreContainsEmptyCompletedTag() throws Exception {
        var todo = store.newBuilder()
                .newId().setName("test").setDescription("what").setCompleted(true).build();
        store.add(todo);

        var xml = createXmlString();
        var inSrc = new InputSource(new StringReader(xml));

        // Selects the id of the todonode that has an empty completed child tag, and chops the leading _ from it
        var guidSelector = "substring(//node()[local-name()='todo'][node()[local-name()='completed'][not(text())]]/@id, 2)";
        var outputGuid = xpath.evaluate(guidSelector, inSrc);
        assertAll(
                () -> assertNotEquals("", outputGuid),
                () -> assertEquals(todo.getId(), UUID.fromString(outputGuid)));
    }

    @Test
    void singleDueDatedChildlessTodoStoreCreatesValidXMLFile() throws Exception {
        var todo = store.newBuilder()
                .newId().setName("test").setDescription("what")
                .setDueDate(Date.valueOf("2038-01-17"))
                .build();
        store.add(todo);
        var xmlSrc = createXmlSource();

        assertDoesNotThrow(() -> validator.validate(xmlSrc));
    }

    @Test
    void singleDueDatedChildlessTodoStoreContainsDueDate() throws Exception {
        final var EOT = "2038-01-19";
        var todo = store.newBuilder()
                .newId().setName("test").setDescription("what")
                .setDueDate(Date.valueOf(EOT))
                .build();
        store.add(todo);
        var xml = createXmlString();

        assertTrue(xml.contains(EOT));
    }

    @Test
    void singleDuedChildlessTodoStoreCreatesValidXMLFile() throws Exception {
        var todo = store.newBuilder()
                .newId().setName("test").setDescription("what")
                .setDueDate(Date.valueOf("2038-01-17"))
                .setDueTime(LocalTime.of(4, 20, 42))
                .build();
        store.add(todo);
        var xmlSrc = createXmlSource();

        assertDoesNotThrow(() -> validator.validate(xmlSrc));
    }

    @Test
    void singleDuedChildlessTodoStoreContainsDueDate() throws Exception {
        final var EOT = "2038-01-19";
        var todo = store.newBuilder()
                .newId().setName("test").setDescription("what")
                .setDueDate(Date.valueOf(EOT))
                .setDueTime(LocalTime.of(4, 20, 42))
                .build();
        store.add(todo);
        var xml = createXmlString();

        assertTrue(xml.contains(EOT));
    }

    @Test
    void singleDuedChildlessTodoStoreContainsDueTime() throws Exception {
        final var EOT = "2038-01-19";
        var todo = store.newBuilder()
                .newId().setName("test").setDescription("what")
                .setDueDate(Date.valueOf(EOT))
                .setDueTime(LocalTime.of(4, 20, 42))
                .build();
        store.add(todo);
        var xml = createXmlString();

        assertTrue(xml.contains("04:20:42"));
    }

    @Test
    void childrenTodoStoreAllOutputsValidXML() throws Exception {
        var child1 = store.newBuilder()
                .newId().setName("child1").setDescription("lorem ipsum")
                .build();
        var child2 = store.newBuilder()
                .newId().setName("child2").setDescription("dolor sit amet")
                .build();
        var parent = store.newBuilder()
                .newId().setName("parent").setDescription("consectetur adipiscing elit")
                .addChild(child1.getId())
                .addChild(child2.getId())
                .build();
        store.add(child1);
        store.add(child2);
        store.add(parent);

        var xmlSrc = createXmlSource();

        assertDoesNotThrow(() -> validator.validate(xmlSrc));
    }

    @Test
    void childrenTodoStoreContainsAllIds() throws Exception {
        var child1 = store.newBuilder()
                .newId().setName("child1").setDescription("lorem ipsum")
                .build();
        var child2 = store.newBuilder()
                .newId().setName("child2").setDescription("dolor sit amet")
                .build();
        var parent = store.newBuilder()
                .newId().setName("parent").setDescription("consectetur adipiscing elit")
                .addChild(child1.getId())
                .addChild(child2.getId())
                .build();
        store.add(child1);
        store.add(child2);
        store.add(parent);
        var guids = new HashSet<UUID>();
        guids.add(child1.getId());
        guids.add(child2.getId());
        guids.add(parent.getId());

        var xml = createXmlString();
        var inSrc = new InputSource(new StringReader(xml));

        var guidSelector = "//node()[local-name()='todo'][@id]";
        var outputTodos = (NodeList) xpath.evaluate(guidSelector, inSrc, XPathConstants.NODESET);
        assertNotNull(outputTodos);
        var found = new HashSet<UUID>();
        for (int i = 0; i < outputTodos.getLength(); i++) {
            var text = outputTodos.item(i).getAttributes().getNamedItem("id").getNodeValue();
            var guid = UUID.fromString(text.substring(1));
            found.add(guid);
        }
        assertEquals(guids, found);
    }
    @Test
    void childrenTodoStoreContainsChildrenRefs() throws Exception {
        var child1 = store.newBuilder()
                .newId().setName("child1").setDescription("lorem ipsum")
                .build();
        var child2 = store.newBuilder()
                .newId().setName("child2").setDescription("dolor sit amet")
                .build();
        var parent = store.newBuilder()
                .newId().setName("parent").setDescription("consectetur adipiscing elit")
                .addChild(child1.getId())
                .addChild(child2.getId())
                .build();
        store.add(child1);
        store.add(child2);
        store.add(parent);
        var guids = new HashSet<>();
        guids.add(child1.getId());
        guids.add(child2.getId());

        var xml = createXmlString();
        var inSrc = new InputSource(new StringReader(xml));

        var guidSelector = "//node()[local-name()='todo'][@ref]";
        var outputTodos = (NodeList) xpath.evaluate(guidSelector, inSrc, XPathConstants.NODESET);
        assertNotNull(outputTodos);
        var found = new HashSet<UUID>();
        for (int i = 0; i < outputTodos.getLength(); i++) {
            var text = outputTodos.item(i).getAttributes().getNamedItem("ref").getNodeValue();
            var guid = UUID.fromString(text.substring(1));
            found.add(guid);
        }
        assertEquals(guids, found);

    }

    private String createXmlString() {
        var writer = new StringWriter();
        assertDoesNotThrow(() -> exporter.export(writer));
        return writer.toString();
    }

    @Contract(" -> new")
    private @NotNull Source createXmlSource() {
        return new StreamSource(new StringReader(createXmlString()));
    }

    private TodoStore store;
    private TodoExporter exporter;
    private static final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    private static Schema schema;
    private static Validator validator;
    private final XPath xpath = XPathFactory.newInstance().newXPath();
}
