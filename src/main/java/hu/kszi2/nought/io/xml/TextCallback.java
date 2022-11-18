package hu.kszi2.nought.io.xml;

import org.xml.sax.SAXException;

public interface TextCallback {
    void process(String text) throws SAXException;
}
