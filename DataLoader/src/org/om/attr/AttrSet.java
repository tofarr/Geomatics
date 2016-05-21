package org.om.attr;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.om.element.Element;
import org.om.element.ObjElement;
import org.om.store.StoreException;

/**
 *
 * @author tofarrell
 */
public class AttrSet implements Iterable<Attr> {

    private final Map<String, Integer> byName;
    private final Attr[] attrs;

    private AttrSet(Attr... attrs) throws IllegalArgumentException, NullPointerException {
        this.byName = new HashMap<>();
        this.attrs = attrs;
        for (int a = attrs.length; a-- > 0;) {
            Attr attr = attrs[a];
            if (byName.containsKey(attr.getName())) {
                throw new IllegalArgumentException("Duplicate attribute : " + attr.getName());
            }
            byName.put(attr.getName(), a);
        }
    }

    @ConstructorProperties({"attrs"})
    public AttrSet(List<Attr> attrs) throws IllegalArgumentException, NullPointerException {
        this(attrs.toArray(new Attr[attrs.size()]));
    }

    public List<Attr> getAttrs() {
        return Collections.unmodifiableList(Arrays.asList(attrs));
    }

    public Integer indexOf(String name) {
        return byName.get(name);
    }

    public Attr byName(String name) {
        Integer index = byName.get(name);
        return (index == null) ? null : attrs[index];
    }

    public Attr byIndex(int index) throws IndexOutOfBoundsException {
        return attrs[index];
    }

    public int size() {
        return attrs.length;
    }

    public AttrSet filter(Collection<String> attrNames) {
        List<Attr> attrList = new ArrayList<>();
        for (String attrName : attrNames) {
            Integer index = byName.get(attrName);
            if (index == null) {
                throw new IllegalArgumentException("Unknown attribute : " + attrName);
            }
            Attr attr = attrs[index];
            attrList.add(attr);
        }
        return (attrList.size() == attrs.length) ? this : new AttrSet(attrList);
    }

    public ObjElement filterElement(ObjElement element) {
        Map<String, Element> ret = new HashMap<>();
        for (Attr attr : attrs) {
            String key = attr.getName();
            ret.put(key, element.getElement(key));
        }
        return ObjElement.valueOf(ret);
    }

    public boolean containsAny(Collection<String> attrNames) {
        for (Attr attr : attrs) {
            if (attrNames.contains(attr.getName())) {
                return true;
            }
        }
        return false;
    }

    public boolean containsAll(Collection<String> attrNames) {
        return byName.keySet().containsAll(attrNames);
    }

    public void validate(ObjElement element, boolean allowUnknowns) {
        for (Attr attr : attrs) {
            if (attr.getCriteria() != null) {
                Element attrElement = element.getElement(attr.getName());
                if (!attr.getCriteria().match(element)) {
                    throw new StoreException("Value did not match attribute : " + attr.getName());
                }
            }
        }
        if (!allowUnknowns) {
            for (String key : element) {
                if (byName.containsKey(key)) {
                    throw new StoreException("Unknown attribute : " + key);
                }
            }
        }
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
        int hash = 5;
        hash = 37 * hash + Arrays.hashCode(this.attrs);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj instanceof AttrSet)) {
            return false;
        }
        final AttrSet other = (AttrSet) obj;
        return Arrays.equals(this.attrs, other.attrs);
    }

}
