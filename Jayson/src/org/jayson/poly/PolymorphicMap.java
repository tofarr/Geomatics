package org.jayson.poly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;

/**
 *
 * @author tofarrell
 */
public class PolymorphicMap {

    private static volatile PolymorphicMap instance;

    private final Map<Class, ClassMap> classMaps;

    public PolymorphicMap(PolymorphicMapBuilder builder) throws NullPointerException {
        classMaps = new HashMap<>();
        for (Entry<Class, ClassMap> entry : builder.classMaps.entrySet()) {
            ClassMap classMap = entry.getValue();
            if (classMap.numMappings() > 0) {
                classMaps.put(entry.getKey(), new ClassMap(classMap));
            }
        }
    }

    public static PolymorphicMap getInstance() {
        if (instance != null) {
            return instance;
        }
        PolymorphicMap ret = PolymorphicMapBuilder.getInstance().build();
        instance = ret;
        return ret;
    }

    public <T> String getImplName(Class<T> baseClass, Class<? extends T> implClass) {
        ClassMap classMap = classMaps.get(baseClass);
        return (classMap == null) ? null : classMap.getName(implClass);

    }

    public <T> Class<? extends T> getImplClass(Class<T> baseClass, String name) {
        ClassMap classMap = classMaps.get(baseClass);
        return (classMap == null) ? null : classMap.getImplClass(name);
    }

    public String[] getNames(Class<?> baseClass) {
        ClassMap classMap = classMaps.get(baseClass);
        return (classMap == null) ? new String[0] : classMap.getNames();
    }

    public <T> Class<? extends T>[] getImplClasses(Class<T> baseClass) {
        ClassMap classMap = classMaps.get(baseClass);
        return (classMap == null) ? new Class[0] : classMap.getImplClasses();
    }

    public <T> ClassMap<T> getClassMap(Class<T> baseClass) {
        return classMaps.get(baseClass);
    }
}
