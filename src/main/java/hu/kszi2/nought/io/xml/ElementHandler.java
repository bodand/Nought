package hu.kszi2.nought.io.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Utility interface, used to provide a class that can handle a given element.
 * It does not store, by itself which element it can handle, just handles it,
 * based on its attributes.
 */
public interface ElementHandler {
    /**
     * Handles a given element, based on its attributes.
     *
     * @param attributes The currently parsed element's (tag's) attributes
     * @throws SAXException If handling the element it is wrapped in a SAXException and thrown
     *                      further
     */
    void handle(Attributes attributes) throws SAXException;
}
