package org.jayson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;
import org.jayson.parser.JsonParserFactory;
import org.jayson.render.JsonRenderFactory;

/**
 *
 * @author tofar
 */
public class JaysonBuilder {

    private final List<JsonParserFactory> parserFactories;
    private final List<JsonRenderFactory> renderFactories;

    public JaysonBuilder() {
        parserFactories = new ArrayList<>();
        renderFactories = new ArrayList<>();
    }

    //Gets default instance based on service loaders
    public static JaysonBuilder getInstance(){
        List<JsonConfig> configList = new ArrayList<>();
        for(JsonConfig config : ServiceLoader.load(JsonConfig.class)){
            configList.add(config);
        }
        JsonConfig[] configArray = configList.toArray(new JsonConfig[configList.size()]);
        Arrays.sort(configArray);
        JaysonBuilder builder = new JaysonBuilder();
        for(JsonConfig config : configArray){
            config.configure(builder);
        }
        return builder;
    }
    
    public JaysonBuilder addParserFactory(JsonParserFactory parserFactory) throws NullPointerException {
        if (parserFactory == null) {
            throw new NullPointerException();
        }
        parserFactories.add(parserFactory);
        return this;
    }

    public JaysonBuilder addRenderFactory(JsonRenderFactory renderFactory) throws NullPointerException {
        if (renderFactory == null) {
            throw new NullPointerException();
        }
        renderFactories.add(renderFactory);
        return this;
    }

    public Jayson build() throws JsonException {
        JsonParserFactory[] parserFactoryArray = parserFactories.toArray(new JsonParserFactory[parserFactories.size()]);
        JsonRenderFactory[] renderFactoryArray = renderFactories.toArray(new JsonRenderFactory[renderFactories.size()]);
        return new Jayson(parserFactoryArray, renderFactoryArray);
    }

}
