package hu.kszi2.nought.io.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public interface ElementHandler {
    void handle(Attributes attributes) throws SAXException;
}
