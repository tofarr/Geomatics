package org.jsonutil;

/**
 * Output json in a more human readable format
 * @author tofarrell
 */
public class PrettyPrintJsonWriter extends JsonWriter {
    
    private int indent;

    public PrettyPrintJsonWriter(Appendable appendable) throws NullPointerException {
        super(appendable);
    }

    @Override
    protected void writeEndObject() throws JsonException {
        indent--;
        newLineIndent();
        super.writeEndObject();
    }

    @Override
    protected void writeBeginObject() throws JsonException {
        super.writeBeginObject();
        indent++;
    }

    @Override
    protected void writeName(String name) throws JsonException {
        if (prev != null) {
            append(',');
        }
        newLineIndent();
        name = sanitize(name);
        append(name);
    }   
    
    public PrettyPrintJsonWriter comment(String comment) throws JsonException {
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
