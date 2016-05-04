package org.jsonutil.poly;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author tofarrell
 */
public class ClassMap<E> {

    public final Class<E> baseClass;
    private final Map<String, Class<? extends E>> nameToImpl;
    private final Map<Class<? extends E>, String> implToName;

    public ClassMap(Class<E> baseClass) {
        this.baseClass = baseClass;
        nameToImpl = new HashMap<>();
        implToName = new HashMap<>();
    }

    public ClassMap(ClassMap<E> map) {
        this.baseClass = map.baseClass;
        this.nameToImpl = new HashMap<>(map.nameToImpl);
        this.implToName = new HashMap<>(map.implToName);
    }

    public String getName(Class<? extends E> implClass){  
        return implToName.get(implClass);
    }
    
    public Class<? extends E> getImplClass(String name){
        return nameToImpl.get(name);
    }
    
    public ClassMap<E> add(Class<? extends E> implClass) throws NullPointerException {
        return add(implClass.getSimpleName(), implClass);
    }

    public ClassMap<E> add(String name, Class<? extends E> implClass) throws NullPointerException {
        if (name == null) {
            throw new NullPointerException("name must not be null");
        }
        if (name.isEmpty()) {
            throw new NullPointerException("name must not be empty");
        }
        if(!baseClass.isAssignableFrom(implClass)){
            throw new IllegalArgumentException("ImplClass "+implClass+" was not assignable from base class "+baseClass);
        }
        remove(name);
        remove(implClass);
        nameToImpl.put(name, implClass);
        implToName.put(implClass, name);
        return this;
    }

    public ClassMap<E> remove(String name) {
        Class<? extends E> implClass = nameToImpl.remove(name);
        implToName.remove(implClass);
        return this;
    }

    public ClassMap<E> remove(Class<? extends E> implClass) {
        String name = implToName.remove(implClass);
        nameToImpl.remove(name);
        return this;
    }
    
    public ClassMap<E> clear(){
        implToName.clear();
        nameToImpl.clear();
        return this;
    }

    public int numMappings() {
        return nameToImpl.size();
    }

    public String[] getNames() {
        return nameToImpl.keySet().toArray(new String[nameToImpl.size()]);
    }

    public Class<? extends E>[] getImplClasses() {
        return implToName.keySet().toArray(new Class[implToName.size()]);
    }
}
