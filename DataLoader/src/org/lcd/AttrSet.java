package org.lcd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author tofar
 */
public final class AttrSet implements Iterable<Attr> {

    private final Map<String, Integer> indexMap;
    private final Attr[] attrs;


    public AttrSet(Attr... attrs) throws NullPointerException, IllegalArgumentException {
        this.attrs = attrs.clone();
        this.indexMap = buildIndexMap(attrs);
        
    }

    public AttrSet(List<Attr> attrs) {
        this.attrs = attrs.toArray(new Attr[attrs.size()]);
        this.indexMap = buildIndexMap(this.attrs);
    }
    
    private static Map<String,Integer> buildIndexMap(Attr[] attrs){
        Map<String,Integer> ret = new HashMap<>();
        for(int i = 0; i < attrs.length; i++){
            String name = attrs[i].getName();
            if (ret.containsKey(name)) {
                throw new IllegalArgumentException("Duplicate attribute found : " + name);
            }
            ret.put(name, i);
        }
        return ret;
    }

    public int numAttrs() {
        return attrs.length;
    }

    public Attr<?> getAttr(int index) {
        return attrs[index];
    }

    public Attr<?> getAttr(String name) {
        Integer index = indexMap.get(name);
        return (index == null) ? null : attrs[index];
    }
    
    public int indexOf(String name){
        Integer index = indexMap.get(name);
        return (index == null) ? -1 : index;
    }

    public List<Attr> toList() {
        return new ArrayList<>(Arrays.asList(attrs));
    }

    public LinkedHashMap<String, Attr> toMap() {
        LinkedHashMap<String, Attr> ret = new LinkedHashMap<>();
        for (Attr attr : attrs) {
            ret.put(attr.getName(), attr);
        }
        return ret;
    }
    
    public List<String> nameList(){
        ArrayList<String> ret = new ArrayList<>();
        for(Attr attr : attrs){
            ret.add(attr.getName());
        }
        return ret;
    }

    @Override
    public Iterator<Attr> iterator() {
        return new Iterator<Attr>() {

            int index;

            @Override
            public boolean hasNext() {
                return index < attrs.length;
            }

            @Override
            public Attr next() {
                return attrs[index++];
            }

        };
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 13 * hash + Arrays.hashCode(this.attrs);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof AttrSet){
            return Arrays.equals(this.attrs, ((AttrSet)obj).attrs);
        }
        return false;
    }

    
}
