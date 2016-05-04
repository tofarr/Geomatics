package org.jsonutil.parser;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 *
 * @author tofarrell
 */
@Documented @Target(ElementType.METHOD) @Retention(RUNTIME)
public @interface StaticFactory {
    /**
       <p>The getter names.</p>
       @return the getter names corresponding to the parameters in the
       annotated constructor.
    */
    String[] value();
}
