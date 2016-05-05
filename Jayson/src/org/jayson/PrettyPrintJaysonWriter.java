package org.jayson;

/**
 * Output json in a more human readable format
 * @author tofarrell
 */
public class PrettyPrintJaysonWriter extends JaysonWriter {
    
    private int indent;

    public PrettyPrintJaysonWriter(Appendable appendable) throws NullPointerException {
        super(appendable);
    }

    @Override
    protected void writeEndObject() throws JaysonException {
        indent--;
        newLineIndent();
        super.writeEndObject();
    }

    @Override
    protected void writeBeginObject() throws JaysonException {
        super.writeBeginObject();
        indent++;
    }

    @Override
    protected void writeName(String name) throws JaysonException {
        if (prev != null) {
            append(',');
        }
        newLineIndent();
        name = sanitize(name);
        append(name);
    }   
    
    public PrettyPrintJaysonWriter comment(String comment) throws JaysonException {
        comment = "/* " + comment.replace("*", "* ") + " */";
        newLineIndent();
        append(comment);
        return this;
    }
    
    protected void newLineIndent(){
        append(System.lineSeparator());
        for(int i = 0; i < indent; i++){
            append('\t');
        }
    }
}
