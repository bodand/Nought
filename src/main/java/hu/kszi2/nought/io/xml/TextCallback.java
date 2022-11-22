package hu.kszi2.nought.io.xml;

import org.xml.sax.SAXException;

/**
 * <p>
 * Interface representing a class that can process a given string.
 * Its behavior is not well-defined without context, but in general it is used
 * in XML parsing to interpret a piece of string, which is clean-text in the XML document.
 * </p>
 * <p>
 * For example, in the following XML snippet, {@code <tag>TEXT</tag>} it is used to process
 * {@code "TEXT"}.
 * </p>
 */
public interface TextCallback {
    /**
     * Process a given text parsed from an XML document.
     *
     * @param text The text to process
     * @throws SAXException If processing the text causes an exception to occur, it is wrapped into
     *                      a SAXException and thrown.
     */
    void process(String text) throws SAXException;
}
