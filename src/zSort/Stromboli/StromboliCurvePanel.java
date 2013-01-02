/*
 *  StromboliCurvePanel.java
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
 *  Created by Andreas Schweizer on Sat Nov 23 2002.
 *  Copyright (c) 2002 Andreas Schweizer.
 *
 */

import java.awt.*;
import java.util.Vector;
import java.awt.event.*;

public class StromboliCurvePanel extends Panel
implements java.util.Observer, MouseListener
{
    private StromboliCurves curves;
    private Frame offscreenImageFrame;
    private Image offscreenImage;
    private Image trashcanImage;
    private double curAngle_t;
    private StromboliCalculationParams newParams;
    private double scaleMetersToPixels = 0.2;
    private double maxSelectionClickDistance = 20.0;
    private StromboliTerrain t;
    private int xSize = 700, yOffset = 290, ySize = 311;
    private Vector terrainCrossSection;
    private int terrainXs[];
    private int terrainYs[];
    private int tab1, tab2, tab3, tab4;
    private int infoSheetHSize;
    private int infoSheetLeft;
    private boolean infoSheetLeftArrowActive = false;
    private boolean infoSheetRightArrowActive = false;
    
    public StromboliCurvePanel(StromboliCalculationParams cp, StromboliCurves curv, 
        StromboliTerrain ter, StromboliApplet ap)
    {
        curves = curv;
        t = ter;
        
        infoSheetHSize = StromboliApplet._t(StromboliApplet._t_hsize);
        infoSheetLeft = xSize - infoSheetHSize;
        tab1 = StromboliApplet._t(StromboliApplet._t_tab1);
        tab2 = StromboliApplet._t(StromboliApplet._t_tab2);
        tab3 = StromboliApplet._t(StromboliApplet._t_tab3);
        tab4 = StromboliApplet._t(StromboliApplet._t_tab4);

        offscreenImageFrame = new Frame();
        offscreenImageFrame.addNotify();
        offscreenImage = offscreenImageFrame.createImage(xSize, ySize);

        trashcanImage = ap.getImage(ap.getDocumentBase(), "trashcan.jpg");
        this.prepareImage(trashcanImage, this);
        
        newParams = cp;
        newParams.addObserver(this);

        curAngle_t = cp.azDeg_t();
        prepareTerrainPolygon();

        this.addMouseListener(this);
        curves.addObserver(this);
    }
    
    public StromboliCalculationParams curParams()
    {
        return newParams;
    }
        
    protected void setAzDeg(double newAzDeg)
    {
            curAngle_t = newAzDeg;
            newParams.setAzDeg_t(newAzDeg);
            prepareTerrainPolygon();
            repaint();
    }
    
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
                            setAzDeg(curves.selectedCurve().azDeg_t());
                        } else {
                            // no more selected curves;
                            // repaint so that an existing selection disappears
                            repaint();
                        }
                        break;
                }
            }

        } else if (o == newParams && curAngle_t != newParams.azDeg_t()) {
            setAzDeg(newParams.azDeg_t());
        }
    }
    
    private void prepareTerrainPolygon()
    {
        StromboliPoint p;
        int i;
        
        terrainCrossSection = t.crossSection(newParams.azDeg_t(), newParams.stepWidth());
        
        terrainXs = new int[terrainCrossSection.size()+1];
        terrainYs = new int[terrainCrossSection.size()+1];
        
        for (i=0; i<terrainCrossSection.size(); i++) {
            p = (StromboliPoint)terrainCrossSection.elementAt(i);
            terrainXs[i] = (int)(scaleMetersToPixels * p.x);
            terrainYs[i] = yOffset - (int)(scaleMetersToPixels * p.y);
        }

        terrainXs[i] = 0;    terrainYs[i] = yOffset;
        
        this.paintTerrainCrossection(offscreenImage.getGraphics());
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
    
    // avoid flickering by not erasing the drawing area prior to re-painting it
    public void update(Graphics g)
    {
        paint(g);
    }

    public void paintTerrainCrossection(Graphics g)
    {
        Rectangle bounds = this.getBounds();	// panel bounds
        StromboliCurveDescriptor c;
        boolean atLeastOneInFlight;
        int i, v;
        
        g.setColor(new Color(0.843f, 0.944f, 1.000f));
        g.fillRect(0,0,1000, yOffset);
        
        g.setColor(new Color(0.600f, 0.800f, 0.800f));
        g.setFont(new Font("sans-serif", Font.PLAIN, 12));
        for (i=1; i<7; i++) {
            v = i*(int)(scaleMetersToPixels * 500.0f);
            g.drawLine(v, 0, v, ySize);
            g.drawLine(v+1, 0, v+1, ySize);
            g.drawString((500*i)+"m", v+6, 20);
        }
        for (i=1; i<3; i++) {
            v = i*(int)(scaleMetersToPixels * 500.0f);
            g.drawLine(0, yOffset-v, 1000, yOffset-v);
            g.drawLine(0, yOffset-v-1, 1000, yOffset-v-1);
            g.drawString((500*i)+"m", xSize-45, yOffset-v+15);
        }
                
        g.setColor(new Color(0f, 0f, 0.4f));
        g.fillRect(0, yOffset, 1000, ySize);
        
        g.setColor(new Color(0.294f, 0.184f, 0.165f));
        g.fillPolygon(terrainXs, terrainYs, terrainXs.length);
    }
    
    public void paintInfoRectangle(Graphics g)
    {
        int y = 10;
        int xsleft[] = {infoSheetLeft+tab1, infoSheetLeft+tab1+5, infoSheetLeft+tab1+5};
        int xsright[] = {infoSheetLeft+tab1+20, infoSheetLeft+tab1+25, infoSheetLeft+tab1+20};
        int ysleft[] = {20, 15, 25};
        int ysright[] = {15, 20, 25};
        int d = 1545, t = 21;
        StromboliCurveDescriptor c = curves.selectedCurve();
        
        if (c != null) {            
            g.setColor(new Color(.96f, .96f, .74f));
            g.fillRect(infoSheetLeft, y, infoSheetHSize-20, 21);
            g.setColor(new Color(.96f, .96f, .84f));
            g.fillRect(infoSheetLeft, y+21, infoSheetHSize-20, 87);
            g.setColor(Color.black);
            g.drawRect(infoSheetLeft, y, infoSheetHSize-20, 108);
            g.drawLine(infoSheetLeft, y+21, xSize-20, y+21);
            g.drawLine(infoSheetLeft, y+57, xSize-20, y+57);
            
            g.setFont(new Font("sans-serif", Font.BOLD, 10));
            g.drawString(StromboliApplet._s(StromboliApplet._s_curvenr)+" " 
                + (curves.indexOf(c)+1), infoSheetLeft+6, y+15);
            
            g.drawString(StromboliApplet._s(StromboliApplet._s_shape)+":", infoSheetLeft+6, y+36);
            g.drawString(StromboliApplet._s(StromboliApplet._s_launchAngle)+":", infoSheetLeft+6, y+51);
            g.drawString(StromboliApplet._s(StromboliApplet._s_launchVeloc)+":", infoSheetLeft+tab2, y+51);
            
            g.drawString(StromboliApplet._s(StromboliApplet._s_startEnergy)+":", infoSheetLeft+6, y+72);
            g.drawString(StromboliApplet._s(StromboliApplet._s_flightDist)+":", infoSheetLeft+6, y+87);
            g.drawString(StromboliApplet._s(StromboliApplet._s_flightTime)+":", infoSheetLeft+tab2, y+87);
            g.drawString(StromboliApplet._s(StromboliApplet._s_hMax)+":", infoSheetLeft+6, y+102);
            g.drawString(StromboliApplet._s(StromboliApplet._s_atDist)+":", infoSheetLeft+tab2, y+102);
    
            g.setFont(new Font("sans-serif", Font.PLAIN, 10));
            g.drawString(c.shapeDescriptor(), infoSheetLeft+tab1, y+36);
            g.drawString(f1str(c.alpha0())+"¡", infoSheetLeft+tab1, y+51);
            g.drawString(f1str(c.v0())+"m/s", infoSheetLeft+tab4, y+51);
            
            g.drawString(f1str(c.v0()*c.v0()*c.m0()*0.5)+" Joules", infoSheetLeft+tab1, y+72);
            g.drawString(f1str(c.dst())+"m", infoSheetLeft+tab1, y+87);
            g.drawString(f1str(c.tTot())+"s", infoSheetLeft+tab3, y+87);
            g.drawString(f1str(c.hMax())+"m", infoSheetLeft+tab1, y+102);
            g.drawString(f1str(c.hMaxDist())+"m", infoSheetLeft+tab3, y+102);
            
            infoSheetLeftArrowActive = (curves.indexOf(c) != 0);
            infoSheetRightArrowActive = (curves.indexOf(c) < (curves.size()-1));
            
            g.setColor(infoSheetLeftArrowActive ? Color.black : Color.gray);
            g.fillPolygon(xsleft, ysleft, 3);
            g.setColor(infoSheetRightArrowActive ? Color.black : Color.gray);
            g.fillPolygon(xsright, ysright, 3);
            g.drawImage(trashcanImage, xSize-40, y+3, 14, 14, null);
        }
    }

    public void paint(Graphics g)
    {
        g.drawImage(offscreenImage, 0, 0, this);
        curves.drawForTerrainPanel(g, curAngle_t, yOffset, scaleMetersToPixels);
        paintInfoRectangle(g);
    }
        
    public void mouseClicked(MouseEvent e)
    {
        int x, y;
        double xT, yT;
        StromboliCurveDescriptor c;
        boolean clickEaten = false;
        int ix;
        
        x = e.getX();
        y = e.getY();

        // If we're currently displaying the info panel, we first check if the user
        // has clicked into the trashcan icon in the panel to delete the selected curve
        if (curves.selectedCurve() != null) {

            if (x >= infoSheetLeft && x <= xSize-20 && y >= 10 && y <= 93) {
                // INV: Clicked into info panel

                if (x >= xSize-40 && x <= xSize-26 && y >= 13 && y <= 27) {
                    // INV: Clicked into trashcan icon
                    c = curves.selectedCurve();
                    ix = curves.indexOf(c);
                    curves.removeCurve(c);
                    if (--ix < 0) ix = 0;
                    curves.selectCurveAtIndex(ix);
                
                } else if (x >= infoSheetLeft+tab1-3 && x <= infoSheetLeft+tab1+5+3 && y >= 13 && y <= 27) {
                    if (infoSheetLeftArrowActive) {
                        curves.selectCurveAtIndex(curves.selectedCurveIndex()-1);
                    }
                } else if (x >= infoSheetLeft+tab1+20-3 && x <= infoSheetLeft+tab1+25+3 && y >= 13 && y <= 27) {
                    if (infoSheetRightArrowActive) {
                        curves.selectCurveAtIndex(curves.selectedCurveIndex()+1);
                    }
                }
                
                clickEaten = true;
            }
        }
        
        if (!clickEaten) {
            xT = x / scaleMetersToPixels;
            yT = (yOffset-y) / scaleMetersToPixels;
        
            c = curves.findCurveAt(curAngle_t, xT, yT, maxSelectionClickDistance);
            curves.selectCurve(c);
        }
    }
    
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }
    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }

    
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
