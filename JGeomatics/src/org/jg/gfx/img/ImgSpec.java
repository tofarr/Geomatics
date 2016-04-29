package org.jg.gfx.img;

import java.awt.image.BufferedImage;
import org.jg.geom.GeomException;

/**
 *
 * @author tofarrell
 */
public interface ImgSpec {

    int getWidth() throws GeomException;

    int getHeight() throws GeomException;

    BufferedImage toImg() throws GeomException;
}
