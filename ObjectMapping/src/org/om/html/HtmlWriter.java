package org.om.html;

import java.io.IOException;
import java.io.Writer;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Writer for fragments of html
 *
 * @author tofarrell
 */
public class HtmlWriter implements AutoCloseable {

    private final XMLStreamWriter xml;
    private final Writer javascript;
    private final String namespace;
    private int idSeq;

    public HtmlWriter(XMLStreamWriter xml, Writer javascript, String namespace, int idSeq) {
        this.xml = xml;
        this.javascript = javascript;
        this.namespace = namespace;
        this.idSeq = idSeq;
    }

    public HtmlWriter begin(String tagName) throws HtmlException {
        try {
            xml.writeStartElement(tagName);
            return this;
        } catch (XMLStreamException ex) {
            throw new HtmlException("Error opening tag : " + tagName, ex);
        }
    }

    public HtmlWriter end() {
        try {
            xml.writeEndElement();
            return this;
        } catch (XMLStreamException ex) {
            throw new HtmlException("Error closing tag", ex);
        }
    }

    public HtmlWriter tag(String tagName) {
        try {
            xml.writeEmptyElement(tagName);
            return this;
        } catch (XMLStreamException ex) {
            throw new HtmlException("Error adding singleton tag : " + tagName, ex);
        }
    }

    public HtmlWriter attr(String attrName, String attrValue) {
        try {
            xml.writeAttribute(attrName, attrValue);
            return this;
        } catch (XMLStreamException ex) {
            throw new HtmlException("Error adding attribute " + attrName, ex);
        }
    }
    
    public HtmlWriter text(String text){
        try {
            xml.writeCharacters(text);
            return this;
        } catch (XMLStreamException ex) {
            throw new HtmlException("Error adding text", ex);
        }
    }

    public HtmlWriter js(String rawJs) {
        try {
            javascript.append(rawJs);
            return this;
        } catch (IOException ex) {
            throw new HtmlException("Error adding javascript", ex);
        }
    }
    
    /**
     * Create a new unique identifier
     */
    public String createId(){
        return namespace+(++idSeq);
    }

    public XMLStreamWriter getXml() {
        return xml;
    }

    public Writer getJavascript() {
        return javascript;
    }

    public String getNamespace() {
        return namespace;
    }

    public int getIdSeq() {
        return idSeq;
    }
    

    @Override
    public void close() {
        try {
            xml.close();
        } catch (Exception ex) {
        }
        try {
            javascript.close();
        } catch (Exception ex) {
        }
    }

}
