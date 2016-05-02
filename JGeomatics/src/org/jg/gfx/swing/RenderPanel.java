package org.jg.gfx.swing;

import java.awt.Graphics;
import javax.swing.JPanel;
import org.jg.gfx.source.RenderableObjectSource;
import org.jg.util.View;

/**
 *
 * @author tofarrell
 */
public class RenderPanel extends JPanel {

    private View view;
    private RenderableObjectSource source;

    public RenderPanel() {
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public RenderableObjectSource getSource() {
        return source;
    }

    public void setSource(RenderableObjectSource source) {
        this.source = source;
    }

    @Override
    protected void printComponent(Graphics g) {
        view.resizeTo(widthPx, heightPx);
    }
}
