package org.geomatics.gv.persist;

import java.beans.Encoder;
import java.beans.Expression;
import java.beans.PersistenceDelegate;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.jg.geom.Vect;

/**
 *
 * @author tofar
 */
public class Main {

    public static void main(String[] args) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try(XMLEncoder encoder = new XMLEncoder(out)){
            encoder.setPersistenceDelegate(Vect.class, new PersistenceDelegate(){
                @Override
                protected Expression instantiate(Object oldInstance, Encoder out) {
                    Vect vect = (Vect)oldInstance;
                    return new Expression(oldInstance, Vect.class, "valueOf",
                        new Object[] { vect.x, vect.y });
                }
            });
            encoder.writeObject(Vect.valueOf(3, 5));
        }
        System.out.println(out.toString());
        try(XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(out.toByteArray()))){
            Vect vect = (Vect)decoder.readObject();
            System.out.println(vect);
        }
    }
}
