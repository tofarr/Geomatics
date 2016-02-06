package org.jg;

import java.text.MessageFormat;

/**
 *
 * @author tim.ofarrell
 */
class Util {

    static boolean isValid(double ord) {
        return !(Double.isInfinite(ord) || Double.isNaN(ord));
    }
    
    static void check(double ord, String pattern) throws IllegalArgumentException {
        if (Double.isInfinite(ord) || Double.isNaN(ord)) {
            throw new IllegalArgumentException(MessageFormat.format(pattern, Double.toString(ord)));
        }
    }

    static String ordToStr(double ord) {
        String ret = Double.toString(ord);
        if (ret.endsWith(".0")) {
            ret = ret.substring(0, ret.length() - 2);
        }
        return ret;
    }

    static int hash(double value) {
        long val = Double.doubleToLongBits(value);
        int ret = (int) (val ^ (val >>> 32));
        //we do a bitwise shift here, as it serves to effectively randomise the number
        //and helps prevent clustering in hashing algorithms, massively boosting performance
        for(int i = 0; i < 3; i++){
            int ret2 = (ret >>> 8);
            ret ^= ret2;
        }
        return ret;
    }
}
