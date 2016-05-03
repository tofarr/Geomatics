package org.jg.gfx.img;

import java.awt.image.BufferedImage;
import java.beans.ConstructorProperties;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import org.jg.geom.GeomException;

/**
 *
 * @author tofarrell
 */
public class UrlImgSpec implements ImgSpec {

    private final String url;
    private transient BufferedImage img;

    @ConstructorProperties({"url"})
    public UrlImgSpec(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public BufferedImage toImg() {
        if(img != null){
            return img;
        }
        try {
            URL _url = new URL(url);
            BufferedImage ret = ImageIO.read(_url);
            img = ret;
            return ret;
        } catch (IOException ex) {
            throw new GeomException("Error loading image", ex);
        }
    }

    @Override
    public int imgWidth() throws GeomException {
        return toImg().getWidth();
    }

    @Override
    public int imgHeight() throws GeomException {
        return toImg().getHeight();
    }
}
