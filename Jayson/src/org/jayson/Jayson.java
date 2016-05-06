package org.jayson;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jayson.parser.JaysonParser;
import org.jayson.parser.JaysonParserFactory;
import org.jayson.render.JaysonRenderFactory;
import org.jayson.render.JaysonRender;

/**
 *
 * @author tofar
 */
public class Jayson {

    private static volatile Jayson instance;

    private final JaysonParserFactory[] parserFactories;
    private final JaysonRenderFactory[] renderFactories;
    private final Map<Type, JaysonParser> parserCache;
    private final Map<Type, JaysonRender> renderCache;

    public Jayson(JaysonParserFactory[] parserFactories, JaysonRenderFactory[] renderFactories) {
        this.parserFactories = parserFactories.clone();
        this.renderFactories = renderFactories.clone();
        Arrays.sort(this.parserFactories);
        Arrays.sort(this.renderFactories);
        parserCache = new ConcurrentHashMap<>();
        renderCache = new ConcurrentHashMap();
    }

    //Gets default instance based on service loaders
    public static Jayson getInstance() {
        Jayson ret = instance;
        if(ret != null){
            return ret;
        }
        ret = JaysonBuilder.getInstance().build();
        instance = ret;
        return ret;
    }

    public JaysonParserFactory[] getParserFactories() {
        return parserFactories.clone();
    }

    public JaysonRenderFactory[] getRenderFactories() {
        return renderFactories.clone();
    }

    public <E> E parse(Class<E> clazz, String json) throws JaysonException {
        return parse(clazz, new StringReader(json));
    }

    public <E> E parse(Class<E> clazz, Reader reader) throws JaysonException {
        return (E) getParser(clazz).parse(this, new JaysonReader(reader));
    }

    public <E> E parse(Class<E> clazz, JaysonInput input) throws JaysonException {
        return (E) getParser(clazz).parse(this, input);
    }

    public Object parse(Type type, JaysonInput input) throws JaysonException {
        return getParser(type).parse(this, input);
    }

    public Object parse(Type type, JaysonType firstJsonType, JaysonInput input) throws JaysonException {
        return getParser(type).parse(firstJsonType, this, input);
    }
        
    public String renderStr(Object value) throws JaysonException{
        StringBuilder str = new StringBuilder();
        render(value, str);
        return str.toString();
    }
    
    public void render(Object value, Appendable out) throws JaysonException{
        render(value, new JaysonWriter(out));
    }

    public void render(Object value, JaysonOutput out) throws JaysonException {
        if (value == null) {
            out.nul();
        }
        render(value, value.getClass(), out);
    }

    public void render(Object value, Type type, JaysonOutput out) throws JaysonException {
        if (value == null) {
            out.nul();
        }
        getRender(type).render(value, this, out);
    }

    public JaysonParser getParser(Type type) {
        JaysonParser ret = parserCache.get(type);
        if (ret != null) {
            return ret; // get from cache
        }
        for (JaysonParserFactory parserFactory : parserFactories) {
            ret = parserFactory.getParserFor(type);
            if (ret != null) {
                parserCache.put(type, ret);
                return ret;
            }
        }
        throw new JaysonException("Could not create parser for type : " + type);
    }

    public JaysonRender getRender(Type type) {
        JaysonRender ret = renderCache.get(type);
        if (ret != null) {
            return ret; // get from cache
        }
        for (JaysonRenderFactory renderFactory : renderFactories) {
            ret = renderFactory.getRenderFor(type);
            if (ret != null) {
                renderCache.put(type, ret);
                return ret;
            }
        }
        throw new JaysonException("Could not create render for type : " + type);
    }
}
