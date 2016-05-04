package org.jsonutil;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import org.jsonutil.parser.JsonParser;
import org.jsonutil.parser.JsonParserFactory;
import org.jsonutil.render.JsonRender;
import org.jsonutil.render.JsonRenderFactory;

/**
 *
 * @author tofar
 */
public class JsonCoder {
    
    private static volatile JsonCoder instance;
    
    private final JsonParserFactory[] parserFactories;
    private final JsonRenderFactory[] renderFactories;
    private final Map<Type, JsonParser> parserCache;
    private final Map<Type, JsonRender> renderCache;
    
    public JsonCoder(JsonParserFactory[] parserFactories, JsonRenderFactory[] renderFactories) {
        this.parserFactories = parserFactories.clone();
        this.renderFactories = renderFactories.clone();
        Arrays.sort(parserFactories);
        Arrays.sort(renderFactories);
        parserCache = new ConcurrentHashMap<>();
        renderCache = new ConcurrentHashMap();
    }
    
    //Gets default instance based on service loaders
    public static JsonCoder getInstance(){
        JsonCoder ret = JsonCoderBuilder.getInstance().build();
        instance = ret;
        return ret;
    }

    public JsonParserFactory[] getParserFactories() {
        return parserFactories.clone();
    }

    public JsonRenderFactory[] getRenderFactories() {
        return renderFactories.clone();
    }
    
    public <E> E parse(Class<E> clazz, JsonInput input) throws JsonException{
        return (E)getParser(clazz).parse(this, input);
    }
    
    public Object parse(Type type, JsonInput input) throws JsonException {
        return getParser(type).parse(this, input);
    }
    
    public Object parse(Type type, JsonType firstJsonType, JsonInput input) throws JsonException {
        return getParser(type).parse(firstJsonType, this, input);
    }
    
    public void render(Object value, JsonOutput out) throws JsonException {
        if (value == null) {
            out.nul();
        }
        render(value, value.getClass(), out);
    }
    
    public void render(Object value, Type type, JsonOutput out) throws JsonException {
        if (value == null) {
            out.nul();
        }
        getRender(type).render(value, this, out);
    }
    
    public JsonParser getParser(Type type) {
        JsonParser ret = parserCache.get(type);
        if (ret != null) {
            return ret; // get from cache
        }
        for (JsonParserFactory parserFactory : parserFactories) {
            ret = parserFactory.getParserFor(type);
            if (ret != null) {
                parserCache.put(type, ret);
                return ret;
            }
        }
        throw new JsonException("Could not create parser for type : " + type);
    }
    
    public JsonRender getRender(Type type) {
        JsonRender ret = renderCache.get(type);
        if (ret != null) {
            return ret; // get from cache
        }
        for (JsonRenderFactory renderFactory : renderFactories) {
            ret = renderFactory.getRenderFor(type);
            if (ret != null) {
                renderCache.put(type, ret);
                return ret;
            }
        }
        throw new JsonException("Could not create render for type : " + type);
    }
}
