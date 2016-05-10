package org.geomatics.gv;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.ContainerOrderFocusTraversalPolicy;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import org.geomatics.geom.LineString;
import org.geomatics.geom.Rect;
import org.geomatics.geom.Vect;
import org.geomatics.gfx.fill.ColorFill;
import org.geomatics.gfx.outline.BasicOutline;
import org.geomatics.gfx.renderable.RenderableOutline;
import org.geomatics.gfx.source.CompoundRenderableObjectSource;
import org.geomatics.gfx.swing.RenderPanel;
import org.geomatics.gv.model.LayerModel;
import org.geomatics.gv.model.LayerViewModel;
import org.geomatics.gv.service.GeomViewerService;
import org.geomatics.gv.service.GeomViewerService.GeomViewerListener;
import org.geomatics.util.Tolerance;
import org.geomatics.util.ViewPoint;

/**
 * Store frame state to file...
 *
 *
 * each layer can be backed
 *
 * @author tofarrell
 */
public class GeomViewerFrame extends javax.swing.JFrame {

    private static final Logger LOG = Logger.getLogger(GeomViewerFrame.class.getName());
    public static final String SERVICE = "service";
    private GeomViewerService service;
    private StyleDialog styleDialog;
    private int focusIndex;
    private BufferDialog bufferDialog;
    private CombineDialog combineDialog;
    
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
        renderPanel.addMouseListeners();
        renderPanel.addPropertyChangeListener(RenderPanel.VIEWPOINT, new PropertyChangeListener(){
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if(service != null){
                    service.setViewPoint(renderPanel.getViewPoint());
                }
            }
        });
        styleDialog = new StyleDialog(this, true);
        styleDialog.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                service.setLayerView(focusIndex, styleDialog.getModel());
            }
        });
        bufferDialog = new BufferDialog(this, true);
        combineDialog = new CombineDialog(this, true);
    }

    public GeomViewerService getService() {
        return service;
    }

    public void setService(GeomViewerService service) {
        if (this.service == service) {
            return;
        }
        if (this.service != null) {
            this.service.removeListener(viewListener);
        }
        service.addListener(viewListener);
        Rect bounds = service.getBounds();
        setLocation((int) bounds.minX, (int) bounds.minY);
        setSize((int) bounds.getWidth(), (int) bounds.getHeight());
        renderPanel.setViewPoint(service.getViewPoint());
        bufferDialog.setService(service);
        updateLayersFromService(service);
        firePropertyChange(SERVICE, this.service, this.service = service);
    }
    
    private void updateLayersFromService(GeomViewerService service){
        LayerViewModel[] layerViews = service.getLayerViews();
        List<LayerModel> layers = new ArrayList<>();
        for(LayerViewModel layerView : layerViews){
            LayerModel layer = layerView.layer;
            if(layer != null){
                layers.add(layer);
            }
        }
        renderPanel.setSource(CompoundRenderableObjectSource.valueOf(layers.toArray(new LayerModel[layers.size()])));
        Component[] components = layersMenu.getMenuComponents();
        for (int c = components.length; c-- > 2;) {
            layersMenu.remove(components[c]);
        }
        for(int i = 0; i < layerViews.length; i++){
            addLayerMenu(i, layerViews[i]);
        }
        renderPanel.repaint();
    }

    private void addLayerMenu(final int index, final LayerViewModel layerView) {
        final LayerModel layer = layerView.layer;
        String name = (layer == null) ? layerView.path : layer.title;
        JMenu menu = new JMenu(name);
        
        JMenuItem style = new JMenuItem("Style");
        style.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                focusIndex = index;
                styleDialog.setModel((layer == null) ? LayerModel.DEFAULT : layer);
                styleDialog.setVisible(true);
            }
        });
        menu.add(style);
        
        //menu.add(new JMenuItem("Edit")); // not implementing this for now...
        
        JMenuItem saveAs = new JMenuItem("Save As");
        saveAs.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                String dir;
                if(layerView.path == null){
                    dir = service.getDefaultPath();
                }else{
                    File file = new File(layerView.path);
                    dir = file.getAbsoluteFile().getParent();
                }
                final JFileChooser fc = new JFileChooser(dir);
                int returnVal = fc.showSaveDialog(GeomViewerFrame.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    service.setDefaultPath(file.getParent());
                    service.setLayerView(index, file.getAbsolutePath(), service.getLayerView(index).layer);
                }
            }
        });
        menu.add(saveAs);
        
        JMenuItem delete = new JMenuItem("Delete");
        delete.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean purge = (layerView.path != null) && (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(GeomViewerFrame.this, "Also delete file?"));
                service.removeLayerView(index, purge);
            }
        });
        menu.add(delete);
        
        layersMenu.add(menu);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        renderPanel = new org.geomatics.gfx.swing.RenderPanel();
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
        javax.swing.JPopupMenu.Separator jSeparator1 = new javax.swing.JPopupMenu.Separator();
        actionsMenu = new javax.swing.JMenu();
        bufferItem = new javax.swing.JMenuItem();
        combineItem = new javax.swing.JMenuItem();

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
        viewLocation.setOpaque(false);
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
                .addContainerGap(265, Short.MAX_VALUE)
                .addGroup(renderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(viewLocation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mouseLocation)))
        );

        getContentPane().add(renderPanel, java.awt.BorderLayout.CENTER);

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
        openLayerMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openLayerMenuItemActionPerformed(evt);
            }
        });
        layersMenu.add(openLayerMenuItem);
        layersMenu.add(jSeparator1);

        mainMenu.add(layersMenu);

        actionsMenu.setText("Actions");

        bufferItem.setText("Buffer");
        bufferItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bufferItemActionPerformed(evt);
            }
        });
        actionsMenu.add(bufferItem);

        combineItem.setText("Combine");
        combineItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                combineItemActionPerformed(evt);
            }
        });
        actionsMenu.add(combineItem);

        mainMenu.add(actionsMenu);

        setJMenuBar(mainMenu);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void zoomInMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomInMenuItemActionPerformed
        renderPanel.setViewPoint(service.getViewPoint().zoomIn());
    }//GEN-LAST:event_zoomInMenuItemActionPerformed

    private void zoomOutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomOutMenuItemActionPerformed
        renderPanel.setViewPoint(service.getViewPoint().zoomOut());
    }//GEN-LAST:event_zoomOutMenuItemActionPerformed

    private void panUpMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_panUpMenuItemActionPerformed
        renderPanel.setViewPoint(service.getViewPoint().panPx(0, -10));
    }//GEN-LAST:event_panUpMenuItemActionPerformed

    private void panDownMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_panDownMenuItemActionPerformed
        renderPanel.setViewPoint(service.getViewPoint().panPx(0, 10));
    }//GEN-LAST:event_panDownMenuItemActionPerformed

    private void panLeftMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_panLeftMenuItemActionPerformed
        renderPanel.setViewPoint(service.getViewPoint().panPx(10, 0));
    }//GEN-LAST:event_panLeftMenuItemActionPerformed

    private void panRightMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_panRightMenuItemActionPerformed
        renderPanel.setViewPoint(service.getViewPoint().panPx(-10, 0));
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
            ViewPoint viewPoint = ViewPoint.valueOf(Vect.valueOf(x, y), resolution);
            service.setViewPoint(viewPoint);
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
        updateBounds();
    }//GEN-LAST:event_formComponentMoved

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        updateBounds();
    }//GEN-LAST:event_formComponentResized

    private void resetLocationMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetLocationMenuItemActionPerformed
        renderPanel.setViewPoint(ViewPoint.DEFAULT);
    }//GEN-LAST:event_resetLocationMenuItemActionPerformed

    private void openLayerMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openLayerMenuItemActionPerformed
        final JFileChooser fc = new JFileChooser(service.getDefaultPath());
        int returnVal = fc.showOpenDialog(GeomViewerFrame.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            service.setDefaultPath(file.getParent());
            service.addLayerView(file.getAbsolutePath());
        }
    }//GEN-LAST:event_openLayerMenuItemActionPerformed

    private void bufferItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bufferItemActionPerformed
        bufferDialog.setVisible(true);
    }//GEN-LAST:event_bufferItemActionPerformed

    private void combineItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_combineItemActionPerformed
        combineDialog.setVisible(true);
    }//GEN-LAST:event_combineItemActionPerformed

    private void updateBounds() {
        if (service != null) {
            Rect rect = Rect.valueOf(getX(), getY(), getX() + getWidth(), getY() + getHeight());
            service.setBounds(rect);
        }
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

    private final GeomViewerListener viewListener = new GeomViewerListener() {
        @Override
        public void onViewUpdate(Rect bounds, ViewPoint viewPoint) {
            renderPanel.setViewPoint(viewPoint);
            setLocation((int) bounds.minX, (int) bounds.minY);
            setSize((int) bounds.getWidth(), (int) bounds.getHeight());
        }

        @Override
        public void onLayerUpdate(int index, LayerViewModel layer) {
            updateLayersFromService(service);
        }

        @Override
        public void onError(Exception ex) {
            LOG.log(Level.SEVERE, "Error", ex);
            String msg = ex.getMessage();
            if(msg != null){
                JOptionPane.showMessageDialog(GeomViewerFrame.this, msg);
            }
        }

        @Override
        public void onClose() {
        }

    };

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

                GeomViewerService service = new GeomViewerService();
                if (service.numLayers() == 0) { // We add one!
                    service.addLayerView(LayerModel.DEFAULT); // a new layer with a null path!
                }
                frame.setService(service);
                frame.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu actionsMenu;
    private javax.swing.JMenuItem bufferItem;
    private javax.swing.JMenuItem combineItem;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JMenu layersMenu;
    private javax.swing.JMenuBar mainMenu;
    private javax.swing.JLabel mouseLocation;
    private javax.swing.JMenuItem openLayerMenuItem;
    private javax.swing.JMenuItem panDownMenuItem;
    private javax.swing.JMenuItem panLeftMenuItem;
    private javax.swing.JMenuItem panRightMenuItem;
    private javax.swing.JMenuItem panUpMenuItem;
    private org.geomatics.gfx.swing.RenderPanel renderPanel;
    private javax.swing.JMenuItem resetLocationMenuItem;
    private javax.swing.JCheckBoxMenuItem showMouseLocationMenuItem;
    private javax.swing.JCheckBoxMenuItem showViewLocationMenuItem;
    private javax.swing.JTextField viewLocation;
    private javax.swing.JMenu viewMenu;
    private javax.swing.JMenuItem zoomInMenuItem;
    private javax.swing.JMenuItem zoomOutMenuItem;
    // End of variables declaration//GEN-END:variables
}
