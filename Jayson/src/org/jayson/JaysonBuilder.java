package org.jayson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;
import org.jayson.parser.JaysonParserFactory;
import org.jayson.render.JaysonRenderFactory;

/**
 *
 * @author tofar
 */
public class JaysonBuilder {

    private final List<JaysonParserFactory> parserFactories;
    private final List<JaysonRenderFactory> renderFactories;

    public JaysonBuilder() {
        parserFactories = new ArrayList<>();
        renderFactories = new ArrayList<>();
    }

    //Gets default instance based on service loaders
    public static JaysonBuilder getInstance(){
        List<JaysonConfig> configList = new ArrayList<>();
        for(JaysonConfig config : ServiceLoader.load(JaysonConfig.class)){
            configList.add(config);
        }
        JaysonConfig[] configArray = configList.toArray(new JaysonConfig[configList.size()]);
        Arrays.sort(configArray);
        JaysonBuilder builder = new JaysonBuilder();
        for(JaysonConfig config : configArray){
            config.configure(builder);
        }
        return builder;
    }
    
    public JaysonBuilder addParserFactory(JaysonParserFactory parserFactory) throws NullPointerException {
        if (parserFactory == null) {
            throw new NullPointerException();
        }
        parserFactories.add(parserFactory);
        return this;
    }

    public JaysonBuilder addRenderFactory(JaysonRenderFactory renderFactory) throws NullPointerException {
        if (renderFactory == null) {
            throw new NullPointerException();
        }
        renderFactories.add(renderFactory);
        return this;
    }

    public Jayson build() throws JaysonException {
        JaysonParserFactory[] parserFactoryArray = parserFactories.toArray(new JaysonParserFactory[parserFactories.size()]);
        JaysonRenderFactory[] renderFactoryArray = renderFactories.toArray(new JaysonRenderFactory[renderFactories.size()]);
        return new Jayson(parserFactoryArray, renderFactoryArray);
    }

}
