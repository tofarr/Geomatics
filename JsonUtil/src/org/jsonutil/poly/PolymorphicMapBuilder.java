package org.jsonutil.poly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 *
 * @author tofarrell
 */
public class PolymorphicMapBuilder {
    
    final Map<Class, ClassMap> classMaps;

    public PolymorphicMapBuilder() {
        this.classMaps = new HashMap();
    }

    public static PolymorphicMapBuilder getInstance() {
        List<PolymorphicConfig> configList = new ArrayList<>();
        for (PolymorphicConfig config : ServiceLoader.load(PolymorphicConfig.class)) {
            configList.add(config);
        }
        PolymorphicConfig[] configArray = configList.toArray(new PolymorphicConfig[configList.size()]);
        Arrays.sort(configArray);
        PolymorphicMapBuilder builder = new PolymorphicMapBuilder();
        for (PolymorphicConfig config : configArray) {
            config.configure(builder);
        }
        return builder;
    }
    
    public <T> ClassMap<T> getClassMap(Class<T> baseClass) {
        ClassMap<T> map = classMaps.get(baseClass);
        if(map == null){
            map = new ClassMap(baseClass);
            classMaps.put(baseClass, map);
        }
        return map;
    }
    
    public PolymorphicMap build(){
        return new PolymorphicMap(this);
    }
}
