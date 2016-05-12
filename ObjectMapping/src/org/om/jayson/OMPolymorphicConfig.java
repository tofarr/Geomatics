package org.om.jayson;

import org.jayson.poly.PolymorphicConfig;
import org.jayson.poly.PolymorphicMapBuilder;
import org.om.criteria.And;
import org.om.criteria.Criteria;
import org.om.criteria.Equal;
import org.om.criteria.Length;
import org.om.criteria.Not;
import org.om.criteria.Or;
import org.om.criteria.array.AllArrayItems;
import org.om.criteria.array.AnyArrayItem;
import org.om.criteria.array.ArrayItem;
import org.om.criteria.object.AllKeys;
import org.om.criteria.object.AnyKey;
import org.om.criteria.object.Key;
import org.om.criteria.value.Greater;
import org.om.criteria.value.GreaterEqual;
import org.om.criteria.value.IsInt;
import org.om.criteria.value.Less;
import org.om.criteria.value.LessEqual;

/**
 *
 * @author tofar
 */
public class OMPolymorphicConfig extends PolymorphicConfig {

    public OMPolymorphicConfig() {
        super(MED);
    }

    @Override
    public void configure(PolymorphicMapBuilder builder) {
        builder.getClassMap(Criteria.class)
                .add(And.class)
                .add(Equal.class)
                .add(Length.class)
                .add(Not.class)
                .add(Or.class)
                .add(AllArrayItems.class)
                .add(AnyArrayItem.class)
                .add(ArrayItem.class)
                .add(AllKeys.class)
                .add(AnyKey.class)
                .add(Key.class)
                .add(Greater.class)
                .add(GreaterEqual.class)
                .add(IsInt.class)
                .add(Less.class)
                .add(LessEqual.class);
    }

}
