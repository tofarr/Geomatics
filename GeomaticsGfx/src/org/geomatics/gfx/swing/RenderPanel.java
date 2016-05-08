package org.geomatics.gfx.swing;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import javax.swing.JPanel;
import org.geomatics.geom.Vect;
import org.geomatics.gfx.renderable.Renderable;
import org.geomatics.gfx.source.RenderableObjectSource;
import org.geomatics.gfx.source.SimpleRenderableObjectSource;
import org.geomatics.util.Transform;
import org.geomatics.util.View;
import org.geomatics.util.ViewPoint;

/**
 *
 * @author tofarrell
 */
public class RenderPanel extends JPanel {

    public static final String SOURCE = "source";
    public static final String VIEWPOINT = "viewPoint";
    private RenderableObjectSource source;
    private ViewPoint viewPoint;
    private transient MouseAdapter mouseAdapter;

    public RenderPanel() {
        viewPoint = ViewPoint.DEFAULT;
        source = SimpleRenderableObjectSource.EMPTY;
    }

    public ViewPoint getViewPoint() {
        return viewPoint;
    }

    public void setViewPoint(ViewPoint viewPoint) throws NullPointerException {
        if(viewPoint == null){
            throw new NullPointerException();
        }
        if(!this.viewPoint.equals(viewPoint)){
            this.firePropertyChange(VIEWPOINT, this.viewPoint, this.viewPoint = viewPoint);
            repaint();
        }
    }

    public RenderableObjectSource getSource() {
        return source;
    }

    public void setSource(RenderableObjectSource source) throws NullPointerException {
        if(source == null){
            throw new NullPointerException();
        }
        if(!this.source.equals(source)){
            this.firePropertyChange(SOURCE, this.source, this.source = source);
            repaint();
        }
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        int width = getWidth();
        int height = getHeight();
        if((width > 0) && (height > 0)){
            View view = viewPoint.toView(width, height);
            final Graphics2D g2d = (Graphics2D)g;
            final Transform transform = view.getTransform();
            source.load(view, new RenderableObjectSource.RenderableObjectProcessor() {
                @Override
                public boolean process(Renderable renderable) {
                    renderable.render(g2d, transform);
                    return true;
                }
            });
        }
    }
    
    
    public void addMouseListeners() {
        if (mouseAdapter != null) {
            return;
        }
        mouseAdapter = new MouseAdapter() {
            Point2D dragOrigin;

            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragOrigin != null) {
                    Vect center = viewPoint.getCenter();
                    double x = center.x - ((e.getX() - dragOrigin.getX()) * viewPoint.getResolution());
                    double y = center.y + ((e.getY() - dragOrigin.getY()) * viewPoint.getResolution());
                    setViewPoint(viewPoint.moveTo(Vect.valueOf(x, y)));
                    dragOrigin = e.getPoint();
                    setCursor(new Cursor(Cursor.MOVE_CURSOR));
                } else {
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                dragOrigin = null;
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragOrigin = null;
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }

            @Override
            public void mousePressed(MouseEvent e) {
                dragOrigin = e.getPoint();
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                Vect center = Vect.valueOf(e.getX(), e.getY());
                Vect dimensions = Vect.valueOf(getWidth(), getHeight());
                ViewPoint zoomed = viewPoint.zoom(center, dimensions, (int)e.getPreciseWheelRotation());
                setViewPoint(zoomed);
            }
        };
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
        addMouseWheelListener(mouseAdapter);
    }
}
