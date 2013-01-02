/*
 *  StromboliMapPanel.java
 *  This file is part of StromboliApplet.
 *  
 *  StromboliApplet is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  
 *  StromboliApplet is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with StromboliApplet; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Created by Andreas Schweizer on Mon Nov 25 2002.
 *  Copyright (c) 2002 Andreas Schweizer.
 *
 */

import java.lang.Math.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.util.Vector;


public class StromboliMapPanel extends Panel
implements ImageObserver, java.util.Observer, MouseListener
{
    private StromboliCurves curves;
    private final int xSize = 395;
    private final int ySize = 370;
    private final int xCenter = 170;
    private final int yCenter = 170;
    private final double xScale = 0.08;
    private final double yScale = 0.08;
    private StromboliApplet ownerApplet;
    private Image mapImage;
    private StromboliCalculationParams cp;
    private double curAzDeg;
    private double azDegStep = 0.1;
    private boolean _axial_lines = false;
    private boolean _radial_lines = true;
    
    private Image easterImage;
    
    public StromboliMapPanel(StromboliApplet ap, StromboliCalculationParams cp, StromboliCurves curv)
    {
        String s;
        
        curves = curv;
        ownerApplet = ap;
        this.cp = cp;
        curAzDeg = 0.0;
        this.addMouseListener(this);
        
        s = ap.getParameter("map_image");
        if (s == null) s = "mapcol1b.jpg";
        mapImage = ap.getImage(ap.getDocumentBase(), s);

        if (StromboliApplet._easteregg1) {
            easterImage = ap.getImage(ap.getDocumentBase(), "easteregg1.jpg");
        } else easterImage = null;

        s = ap.getParameter("az_deg_resolution");
        if (s != null) {
            try { azDegStep = Double.valueOf(s).doubleValue(); }
            catch (Exception e) { azDegStep = 0.1; }
            if (azDegStep < 0.1) azDegStep = 0.1;
            else if (azDegStep > 180.0) azDegStep = 180.0;
        }
        
        s = ap.getParameter("enable_axial_lines");
        if (s != null) {
            if (s.equals("yes")) _axial_lines = true;
        }
        curves.setAxialLinesForMapPanel(_axial_lines);

        s = ap.getParameter("enable_radial_lines");
        if (s != null) {
            if (s.equals("no")) _radial_lines = false;
        }
        curves.setRadialLinesForMapPanel(_radial_lines);

        this.prepareImage(mapImage, this);
        cp.addObserver(this);
        curves.addObserver(this);
        repaint();
    }
    
    // avoid flickering by not erasing the drawing area prior to re-painting it
    public void update(Graphics g)
    {
        paint(g);
    }

    public void paint(Graphics g)
    {
        int x1, y1, dx, dy;
        double lineLen = 300;
        int i;
        StromboliCurveDescriptor c;
        double curveAngle, curveLen;
        
        g.drawImage(mapImage, 0, 0, xSize, ySize, null);
        if (easterImage != null && curAzDeg >= 124.0 && curAzDeg <= 148.0) {
            g.drawImage(easterImage, 250, 250, 42, 54, null);
        }
        curves.drawForMapPanel(g, xCenter, yCenter, xScale, yScale);

        // title string
        g.setColor(Color.black);
        g.setFont(new Font("sans-serif", Font.BOLD, 12));
        if (StromboliApplet._t(StromboliApplet._t_azdegnl) == 1) {
            g.drawString(StromboliApplet._s(StromboliApplet._s_ejectionDeg), 10, 20);
            g.drawString("= "+f1str(cp.azDeg_m())+"¡", 10, 40);
        } else {
            g.drawString(StromboliApplet._s(StromboliApplet._s_ejectionDeg) + " = "+f1str(cp.azDeg_m())+"¡", 10, 20);
        }
        
        // direction line
        x1 = xCenter; y1 = yCenter;
        dx = (int)(lineLen * Math.sin(cp.azDeg_m() / 180.0 * Math.PI));
        dy = (int)(lineLen * -Math.cos(cp.azDeg_m() / 180.0 * Math.PI));
        g.setColor(Color.black);
        g.drawLine(x1, y1, x1+dx, y1+dy);

        // trace terrain data
        if (StromboliApplet._debug_check_map_vs_terrain) {
            boolean haveHit;
            boolean isFirst = true;
            double unix, uniy, curFactor;
            int oldX, oldY, newX = 0, newY = 0;

            g.setColor(Color.black);
            for (double curAngle = 0.0; curAngle < 360.0; curAngle += 1.0) {
                unix = (int)(5000.0 * Math.sin(curAngle / 180.0 * Math.PI));
                uniy = (int)(5000.0 * -Math.cos(curAngle / 180.0 * Math.PI));
                haveHit = false;
                for (curFactor = 0.0; curFactor <= 1.0 && !haveHit; curFactor += 0.002) {
                    if (StromboliTerrain._heightApprox(
                            (int)(5000.0 * curFactor * Math.sin((curAngle+3) / 180.0 * Math.PI)), 
                            (int)(5000.0 * curFactor * Math.cos((curAngle+3) / 180.0 * Math.PI))
                    ) <= 0.0)
                        haveHit = true;
                }

                oldX = newX;
                oldY = newY;
                newX = (int)(unix*curFactor*xScale);
                newY = (int)(uniy*curFactor*yScale);
                
                if (!isFirst) {
                    g.drawLine(xCenter+oldX, yCenter+oldY, xCenter+newX, yCenter+newY);
                } else {
                    isFirst = false;
                }
            }
        }
    }
    
    protected void setAzDeg(double newAzDeg)
    {
        cp.setAzDeg_m((double)((int)(10.0*newAzDeg)) / 10.0);
        cp.notifyNow();
        this.repaint();
    }
    
    public double azDegResolution()
    {
        return azDegStep;
    }

    public void mouseClicked(MouseEvent e)
    {
        int dx, dy;
        double d, newAzDeg;
        long fct;
        
        dx = e.getX() - xCenter;
        dy = e.getY() - yCenter;
        d = Math.atan2(dx, -dy) / Math.PI * 180.0;
        if (d < 0) d = 360.0+d;
        fct = (long)((d / azDegStep) + 0.5);
        newAzDeg = (double)fct*azDegStep;
        setAzDeg(newAzDeg);
    }
    
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }
    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }
    
    public void update(java.util.Observable o, Object arg)
    {
        if (o == curves) {
            StromboliCurvesMessage m = (StromboliCurvesMessage)arg;
            if (m != null) {
                switch (m.msg()) {
                    case StromboliCurvesMessage.REALTIME_TICK:
                        if (curves.nofBombsInFlight() > 0)
                            repaint();
                        break;
                    case StromboliCurvesMessage.CURVE_ADDED:
                    case StromboliCurvesMessage.CURVE_REMOVED:
                    case StromboliCurvesMessage.ALL_CURVES_REMOVED:
                    case StromboliCurvesMessage.SELECTION_CHANGED:
                        if (curves.selectedCurve() != null) {
                            setAzDeg(curves.selectedCurve().azDeg_m());
                        } else {
                            // no more selected curves;
                            // repaint so that an existing selection disappears
                            repaint();
                        }
                        break;
                }
            }

        } else if (o == cp && cp.azDeg_m() != curAzDeg) {
            curAzDeg = cp.azDeg_m();
            repaint();
        }
    }
    
    public Dimension getMinimumSize()
    {
        return getPreferredSize();
    }
    
    public Dimension getMaximumSize()
    {
        return getPreferredSize();
    }
    
    public Dimension getPreferredSize()
    {
        return new Dimension(xSize, ySize);
    }

    protected String f1str(double d)
    {
        long x1;
        int x2;
        
        d = d+0.05;
        x1 = (int)d;
        d = 10.0*d - 10*x1;
        x2 = (int)d;
        return ""+x1+"."+x2;
    }
}
