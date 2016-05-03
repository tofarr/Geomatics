package org.jg.gfx.swing.gv;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.ContainerOrderFocusTraversalPolicy;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import javax.swing.BorderFactory;
import javax.swing.JMenu;
import org.jg.geom.LineString;
import org.jg.geom.Rect;
import org.jg.geom.Ring;
import org.jg.geom.Vect;
import org.jg.gfx.fill.ColorFill;
import org.jg.gfx.outline.BasicOutline;
import org.jg.gfx.renderable.RenderableOutline;
import org.jg.gfx.source.CompoundRenderableObjectSource;
import org.jg.gfx.swing.RenderPanel;
import org.jg.util.Tolerance;
import org.jg.util.ViewPoint;

/**
 * Store frame state to file...
 *
 *
 * each layer can be backed
 *
 * @author tofarrell
 */
public class GeomViewerFrame extends javax.swing.JFrame {

    public static final String MODEL = "model";
    private GeomViewerModel model;

    /**
     * Creates new form GeomViewerFrame
     */
    public GeomViewerFrame() {
        initComponents();
        renderPanel.addPropertyChangeListener(RenderPanel.VIEWPOINT, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (viewLocation.isVisible()) {
                    ViewPoint viewPoint = renderPanel.getViewPoint();
                    DecimalFormat format = new DecimalFormat("0.###");
                    String str = "(" + format.format(viewPoint.getCenter().x) + ","
                            + format.format(viewPoint.getCenter().y) + ", "
                            + format.format(viewPoint.getResolution()) + ")";
                    viewLocation.setText(str);
                    viewLocation.setBorder(null);
                }
            }
        });
        setFocusTraversalPolicy(new ContainerOrderFocusTraversalPolicy() {
            @Override
            public Component getFirstComponent(Container aContainer) {
                return renderPanel;
            }

        });
    }

    public GeomViewerModel getModel() {
        return model;
    }

    public void setModel(GeomViewerModel model) {
        renderPanel.setViewPoint(model.getViewPoint());
        renderPanel.setSource(CompoundRenderableObjectSource.valueOf(model.getLayers()));
        Component[] components = layersMenu.getComponents();
        for(int c = components.length; c-- > 2;){
            layersMenu.remove(components[c]);
        }
        //for(GeomLayer layer : model.getLayers()){
        //    addLayerMenu(layer);
        //}
        this.model = model;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        renderPanel = new org.jg.gfx.swing.RenderPanel();
        viewLocation = new javax.swing.JTextField();
        mouseLocation = new javax.swing.JLabel();
        mainMenu = new javax.swing.JMenuBar();
        viewMenu = new javax.swing.JMenu();
        zoomInMenuItem = new javax.swing.JMenuItem();
        zoomOutMenuItem = new javax.swing.JMenuItem();
        resetLocationMenuItem = new javax.swing.JMenuItem();
        javax.swing.JPopupMenu.Separator jSeparator2 = new javax.swing.JPopupMenu.Separator();
        panUpMenuItem = new javax.swing.JMenuItem();
        panDownMenuItem = new javax.swing.JMenuItem();
        panLeftMenuItem = new javax.swing.JMenuItem();
        panRightMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        showViewLocationMenuItem = new javax.swing.JCheckBoxMenuItem();
        showMouseLocationMenuItem = new javax.swing.JCheckBoxMenuItem();
        layersMenu = new javax.swing.JMenu();
        openLayerMenuItem = new javax.swing.JMenuItem();
        addBlankLayerMenuItem = new javax.swing.JMenuItem();
        javax.swing.JPopupMenu.Separator jSeparator1 = new javax.swing.JPopupMenu.Separator();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Geometry Viewer");
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentMoved(java.awt.event.ComponentEvent evt) {
                formComponentMoved(evt);
            }
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        renderPanel.setBackground(new java.awt.Color(255, 255, 255));
        renderPanel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                renderPanelMouseDragged(evt);
            }
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                renderPanelMouseMoved(evt);
            }
        });

        viewLocation.setBorder(null);
        viewLocation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewLocationActionPerformed(evt);
            }
        });

        mouseLocation.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        mouseLocation.setText("0,0");

        javax.swing.GroupLayout renderPanelLayout = new javax.swing.GroupLayout(renderPanel);
        renderPanel.setLayout(renderPanelLayout);
        renderPanelLayout.setHorizontalGroup(
            renderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(renderPanelLayout.createSequentialGroup()
                .addComponent(viewLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(mouseLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        renderPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {mouseLocation, viewLocation});

        renderPanelLayout.setVerticalGroup(
            renderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, renderPanelLayout.createSequentialGroup()
                .addGap(0, 265, Short.MAX_VALUE)
                .addGroup(renderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(viewLocation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mouseLocation)))
        );

        viewMenu.setText("View");

        zoomInMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.CTRL_MASK));
        zoomInMenuItem.setText("Zoom In");
        zoomInMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomInMenuItemActionPerformed(evt);
            }
        });
        viewMenu.add(zoomInMenuItem);

        zoomOutMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_U, java.awt.event.InputEvent.CTRL_MASK));
        zoomOutMenuItem.setText("Zoom Out");
        zoomOutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomOutMenuItemActionPerformed(evt);
            }
        });
        viewMenu.add(zoomOutMenuItem);

        resetLocationMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        resetLocationMenuItem.setText("Reset Location");
        resetLocationMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetLocationMenuItemActionPerformed(evt);
            }
        });
        viewMenu.add(resetLocationMenuItem);
        viewMenu.add(jSeparator2);

        panUpMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP, 0));
        panUpMenuItem.setText("Pan Up");
        panUpMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                panUpMenuItemActionPerformed(evt);
            }
        });
        viewMenu.add(panUpMenuItem);

        panDownMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN, 0));
        panDownMenuItem.setText("Pan Down");
        panDownMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                panDownMenuItemActionPerformed(evt);
            }
        });
        viewMenu.add(panDownMenuItem);

        panLeftMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, 0));
        panLeftMenuItem.setText("Pan Left");
        panLeftMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                panLeftMenuItemActionPerformed(evt);
            }
        });
        viewMenu.add(panLeftMenuItem);

        panRightMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, 0));
        panRightMenuItem.setText("Pan Right");
        panRightMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                panRightMenuItemActionPerformed(evt);
            }
        });
        viewMenu.add(panRightMenuItem);
        viewMenu.add(jSeparator3);

        showViewLocationMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        showViewLocationMenuItem.setSelected(true);
        showViewLocationMenuItem.setText("Show View Location");
        showViewLocationMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showViewLocationMenuItemActionPerformed(evt);
            }
        });
        viewMenu.add(showViewLocationMenuItem);

        showMouseLocationMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, java.awt.event.InputEvent.CTRL_MASK));
        showMouseLocationMenuItem.setSelected(true);
        showMouseLocationMenuItem.setText("Show Mouse Location");
        showMouseLocationMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showMouseLocationMenuItemActionPerformed(evt);
            }
        });
        viewMenu.add(showMouseLocationMenuItem);

        mainMenu.add(viewMenu);

        layersMenu.setText("Layers");

        openLayerMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openLayerMenuItem.setText("Open Layer");
        layersMenu.add(openLayerMenuItem);

        addBlankLayerMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        addBlankLayerMenuItem.setText("New Blank Layer");
        layersMenu.add(addBlankLayerMenuItem);
        layersMenu.add(jSeparator1);

        mainMenu.add(layersMenu);

        setJMenuBar(mainMenu);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(renderPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(renderPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void zoomInMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomInMenuItemActionPerformed
        ViewPoint viewPoint = renderPanel.getViewPoint();
        viewPoint = viewPoint.zoomIn();
        renderPanel.setViewPoint(viewPoint);
    }//GEN-LAST:event_zoomInMenuItemActionPerformed

    private void zoomOutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomOutMenuItemActionPerformed
        ViewPoint viewPoint = renderPanel.getViewPoint();
        viewPoint = viewPoint.zoomOut();
        renderPanel.setViewPoint(viewPoint);
    }//GEN-LAST:event_zoomOutMenuItemActionPerformed

    private void panUpMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_panUpMenuItemActionPerformed
        renderPanel.setViewPoint(renderPanel.getViewPoint().panPx(0, -10));
    }//GEN-LAST:event_panUpMenuItemActionPerformed

    private void panDownMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_panDownMenuItemActionPerformed
        renderPanel.setViewPoint(renderPanel.getViewPoint().panPx(0, 10));
    }//GEN-LAST:event_panDownMenuItemActionPerformed

    private void panLeftMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_panLeftMenuItemActionPerformed
        renderPanel.setViewPoint(renderPanel.getViewPoint().panPx(10, 0));
    }//GEN-LAST:event_panLeftMenuItemActionPerformed

    private void panRightMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_panRightMenuItemActionPerformed
        renderPanel.setViewPoint(renderPanel.getViewPoint().panPx(-10, 0));
    }//GEN-LAST:event_panRightMenuItemActionPerformed

    private void showViewLocationMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showViewLocationMenuItemActionPerformed
        viewLocation.setVisible(!viewLocation.isVisible());
    }//GEN-LAST:event_showViewLocationMenuItemActionPerformed

    private void viewLocationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewLocationActionPerformed
        try {
            String text = viewLocation.getText();
            text = text.replace('(', ' ').replace(')', ' ');
            String[] parts = text.split(",");
            double x = Double.parseDouble(parts[0].trim());
            double y = Double.parseDouble(parts[1].trim());
            double resolution = Double.parseDouble(parts[2].trim());
            ViewPoint view = renderPanel.getViewPoint();
            view = view.moveTo(Vect.valueOf(x, y)).zoomTo(resolution);
            renderPanel.setViewPoint(view);
            renderPanel.requestFocus();
        } catch (Exception ex) {
            viewLocation.setBorder(BorderFactory.createLineBorder(Color.RED));
        }
    }//GEN-LAST:event_viewLocationActionPerformed

    private void renderPanelMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_renderPanelMouseMoved
        updateMouseLocation(evt);
    }//GEN-LAST:event_renderPanelMouseMoved

    private void renderPanelMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_renderPanelMouseDragged
        updateMouseLocation(evt);
    }//GEN-LAST:event_renderPanelMouseDragged

    private void showMouseLocationMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showMouseLocationMenuItemActionPerformed
        mouseLocation.setVisible(!mouseLocation.isVisible());
    }//GEN-LAST:event_showMouseLocationMenuItemActionPerformed

    private void formComponentMoved(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentMoved
        setModel(new GeomViewerModel(getRect(), model.getLayers(), model.getViewPoint()));
    }//GEN-LAST:event_formComponentMoved

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        setModel(new GeomViewerModel(getRect(), model.getLayers(), model.getViewPoint()));
    }//GEN-LAST:event_formComponentResized

    private void resetLocationMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetLocationMenuItemActionPerformed
        setModel(new GeomViewerModel(getRect(), model.getLayers(), ViewPoint.DEFAULT));
    }//GEN-LAST:event_resetLocationMenuItemActionPerformed

    private Rect getRect(){
        return Rect.valueOf(getX(), getY(), getWidth(), getHeight());
    }

    private void updateMouseLocation(MouseEvent evt) {
        ViewPoint viewPoint = renderPanel.getViewPoint();
        double x = (evt.getX() - (renderPanel.getWidth() / 2.0)) * viewPoint.getResolution() + viewPoint.getCenter().x;
        double y = ((renderPanel.getHeight() / 2.0) - evt.getY()) * viewPoint.getResolution() + viewPoint.getCenter().y;

        DecimalFormat format = new DecimalFormat("0.###");
        String str = "(" + format.format(x) + ","
                + format.format(y) + ")";
        mouseLocation.setText(str);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(GeomViewerFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GeomViewerFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GeomViewerFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GeomViewerFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                GeomViewerFrame frame = new GeomViewerFrame();

                RenderableOutline symbol = new RenderableOutline(0,
                        LineString.valueOf(Tolerance.DEFAULT, -5, -5, 5, -5, 5, 5, -5, 5, -5, -5),
                        new ColorFill(0xFFFF0000),
                        new BasicOutline(1));

                GeomLayer layer = new GeomLayer("Basic Layer", null,
                        Ring.valueOf(Tolerance.DEFAULT, 0, 0, 100, 0, 100, 100, 0, 100, 0, 0),
                        null,
                        new ColorFill(0xFF000000),
                        new BasicOutline(1),
                        symbol);

                GeomViewerModel model = new GeomViewerModel(Rect.valueOf(0, 0, 800, 600),
                        new GeomLayer[]{layer},
                        ViewPoint.DEFAULT);

                frame.setModel(model);

                frame.renderPanel.addMouseListeners();
                frame.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem addBlankLayerMenuItem;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JMenu layersMenu;
    private javax.swing.JMenuBar mainMenu;
    private javax.swing.JLabel mouseLocation;
    private javax.swing.JMenuItem openLayerMenuItem;
    private javax.swing.JMenuItem panDownMenuItem;
    private javax.swing.JMenuItem panLeftMenuItem;
    private javax.swing.JMenuItem panRightMenuItem;
    private javax.swing.JMenuItem panUpMenuItem;
    private org.jg.gfx.swing.RenderPanel renderPanel;
    private javax.swing.JMenuItem resetLocationMenuItem;
    private javax.swing.JCheckBoxMenuItem showMouseLocationMenuItem;
    private javax.swing.JCheckBoxMenuItem showViewLocationMenuItem;
    private javax.swing.JTextField viewLocation;
    private javax.swing.JMenu viewMenu;
    private javax.swing.JMenuItem zoomInMenuItem;
    private javax.swing.JMenuItem zoomOutMenuItem;
    // End of variables declaration//GEN-END:variables
}
