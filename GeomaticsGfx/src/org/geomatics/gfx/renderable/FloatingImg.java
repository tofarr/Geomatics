package org.geomatics.gfx.renderable;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.beans.ConstructorProperties;
import org.geomatics.geom.Rect;
import org.geomatics.geom.Vect;
import org.geomatics.geom.VectBuilder;
import org.geomatics.gfx.img.ImgSpec;
import org.geomatics.util.Transform;

/**
 *
 * @author tofarrell
 */
public class FloatingImg implements Renderable {

    private final long id;
    private final ImgSpec img;
    private final Vect location;
    private final Vect offset;

    @ConstructorProperties({"id", "img", "location", "offset"})
    public FloatingImg(long id, ImgSpec img, Vect location, Vect offset) {
        this.id = id;
        this.img = img;
        this.location = location;
        this.offset = offset;
    }

    @Override
    public long getId() {
        return id;
    }

    public ImgSpec getImg() {
        return img;
    }

    public Vect getLocation() {
        return location;
    }

    public Vect getOffset() {
        return offset;
    }

    @Override
    public boolean boundsVariable() {
        return true;
    }

    @Override
    public Rect toBounds(double resolution) {
        int width = img.imgWidth();
        int height = img.imgHeight();
        double minX = location.x - (width * offset.x * resolution);
        double minY = location.y - (height * offset.y * resolution);
        double maxX = location.x + (width * (1 - offset.x) * resolution);
        double maxY = location.y + (height * (1 - offset.y) * resolution);
        return Rect.valueOf(minX, minY, maxX, maxY);
    }

    @Override
    public void render(Graphics2D g, Transform transform) {
        BufferedImage _img = img.toImg();
        VectBuilder vect = new VectBuilder(location.x - (_img.getWidth() * offset.x), location.y - (_img.getHeight() * offset.y));
        transform.transform(vect, vect);
        g.drawImage(_img, (int)Math.round(vect.getX()), (int)Math.round(vect.getY()), null);
    }

}

