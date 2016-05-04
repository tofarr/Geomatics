package org.jsonutil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;
import org.jsonutil.parser.JsonParserFactory;
import org.jsonutil.render.JsonRenderFactory;

/**
 *
 * @author tofar
 */
public class JsonCoderBuilder {

    private final List<JsonParserFactory> parserFactories;
    private final List<JsonRenderFactory> renderFactories;

    public JsonCoderBuilder() {
        parserFactories = new ArrayList<>();
        renderFactories = new ArrayList<>();
    }

    //Gets default instance based on service loaders
    public static JsonCoderBuilder getInstance(){
        List<JsonConfig> configList = new ArrayList<>();
        for(JsonConfig config : ServiceLoader.load(JsonConfig.class)){
            configList.add(config);
        }
        JsonConfig[] configArray = configList.toArray(new JsonConfig[configList.size()]);
        Arrays.sort(configArray);
        JsonCoderBuilder builder = new JsonCoderBuilder();
        for(JsonConfig config : configArray){
            config.configure(builder);
        }
        return builder;
    }
    
    public JsonCoderBuilder addParserFactory(JsonParserFactory parserFactory) throws NullPointerException {
        if (parserFactory == null) {
            throw new NullPointerException();
        }
        parserFactories.add(parserFactory);
        return this;
    }

    public JsonCoderBuilder addRenderFactory(JsonRenderFactory renderFactory) throws NullPointerException {
        if (renderFactory == null) {
            throw new NullPointerException();
        }
        renderFactories.add(renderFactory);
        return this;
    }

    public JsonCoder build() throws JsonException {
        JsonParserFactory[] parserFactoryArray = parserFactories.toArray(new JsonParserFactory[parserFactories.size()]);
        JsonRenderFactory[] renderFactoryArray = renderFactories.toArray(new JsonRenderFactory[renderFactories.size()]);
        return new JsonCoder(parserFactoryArray, renderFactoryArray);
    }

}
