package org.lcd.sort;

import java.util.Comparator;
import org.lcd.AttrSet;
import org.lcd.Result;

/**
 *
 * @author tofar
 */
public interface SortOrder extends Comparator<Result> {
    
    public int compare(AttrSet attrs, Object[] valuesA, Object[] valuesB);
}
