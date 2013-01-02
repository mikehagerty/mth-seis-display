/*
 *  StromboliCurveDescriptor.java
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
import java.util.Date;


public class StromboliCurveDescriptor
{
    private Vector points;
    private StromboliCalculationParams cp;
    private StromboliTerrain t;
    private long startedAt;
    private boolean realtimeDraw;
    private double _hMaxDist;
    private boolean isSelected;
            
    public StromboliCurveDescriptor(StromboliCalculationParams params, StromboliTerrain terrain, boolean realtimeDraw)
    {
        this.realtimeDraw = realtimeDraw;
        isSelected = false;
        points = new Vector();
        try {
            cp = (StromboliCalculationParams)params.clone();
//            cp.deleteObservers();
// doesn't work -- but why?!?

            t = terrain;
        } catch (Exception e) {
        }
    }
    
    public void setSelected(boolean newFlagValue)
    {
        isSelected = newFlagValue;
    }
    
    public boolean inFlight()
    {
// todo -- hack
        long kXXXXX = 500;
        long curTime = (new Date()).getTime();
        if (!realtimeDraw) return false;
        else return (curTime-startedAt-kXXXXX <= 1000*cp.tTot);
    }
    
    public void setWithAir(boolean withAir)
    {
        if (!withAir) {
            cp.setRho(0);
        }
    }
    
    protected double vabs(double x, double y)
    {
        return java.lang.Math.sqrt(x*x+y*y);
    }
    
    protected Vector vstdlen(double x, double y, double length)
    {
        Vector v = new Vector();
        double lenFactor = length / vabs(x, y);
        v.addElement(new Double(x*lenFactor));
        v.addElement(new Double(y*lenFactor));
        return v;
    }
    
    public double dst() { return cp.dst; }
    public double azDeg_m() { return cp.azDeg_m(); }
    public double azDeg_t() { return cp.azDeg_t(); }
    
    public double alpha0() { return cp.alpha0(); }
    public double v0() { return cp.v0(); }    
    public String shapeDescriptor() { return cp.shapeDescriptor(); }
    
    public double tTot() { return cp.tTot; }
    public double m0() { return cp.m0(); }
    
    public double hMax()
    {
        double hMax = 0.0, h;
        int i;
        
        for (i=0; i<points.size(); i++) {
            h = ((StromboliPoint)points.elementAt(i)).y;
            if (h>hMax) {
                hMax = h;
                _hMaxDist = ((StromboliPoint)points.elementAt(i)).x;
            }
        }
        return hMax;
    }
    public double hMaxDist()
    {
        return _hMaxDist;
    }
    
    // does a linear interpolation on the data points
    // returns a large negative number if distance is outside of the interpolation range
    public double heightAtDistance(double distMeters)
    {
        double h = -1e50;
        StromboliPoint p, pPrev;
        int i, imax = points.size();
        double dx0, dx1;
        
        if (points != null) {
            if (points.size() > 0) {
                p = (StromboliPoint)points.elementAt(0);
                if (p != null) {
                    if (distMeters >= p.x) {
                        i = 1;
                        while (i < imax) {
                            pPrev = p;
                            p = (StromboliPoint)points.elementAt(i);
                            if (p != null) {
                                if (distMeters <= p.x) {
                                    dx0 = p.x - pPrev.x;
                                    dx1 = distMeters - pPrev.x;
                                    h = pPrev.y + (dx1 / dx0)*(p.y - pPrev.y);
                                    i = imax;	// leave loop 'the soft way'
                                } else {
                                    i++;
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return h;
    }
    
    public void calculate()
    {
        boolean done = false, crash = false;
        double dx, r, frx, fry;
        double bx, by, bz;
        double limes = 0.0;
        Vector fr;
        
        startedAt = (new Date()).getTime();
        points.removeAllElements();
        
        cp.hght = t.heightApprox(0.0, 0.0);	// start height
        cp.fgx = 0;
        cp.fgy = -cp.m0()*cp.g();
        cp.vex = cp.v0()*java.lang.Math.cos(cp.alpha0()/180.0*Math.PI);
        cp.vey = cp.v0()*java.lang.Math.sin(cp.alpha0()/180.0*Math.PI);
        
        while (!done) {
            cp.mr = 0.5*cp.cw()*cp.rho()*cp.acr();	// depends on cur. bomb height
            cp.it++;
            dx = cp.vex * cp.dt();
            addPoint(cp.dst, cp.hght);
            cp.dst = cp.dst + dx;
            cp.hght = cp.hght + cp.vey * cp.dt();
            r = cp.mr * vabs(cp.vex, cp.vey) * vabs(cp.vex, cp.vey);
            fr = vstdlen(cp.vex, cp.vey, -r);
            frx = ((Double)(fr.elementAt(0))).doubleValue();
            fry = ((Double)(fr.elementAt(1))).doubleValue();
            cp.vex = cp.vex + ((frx + cp.fgx) / cp.m0()) * cp.dt();
            cp.vey = cp.vey + ((fry + cp.fgy) / cp.m0()) * cp.dt();
            bx = (int)(cp.dst * java.lang.Math.sin(cp.azDeg_t() / 180.0 * Math.PI));
            by = (int)(java.lang.Math.sqrt(cp.dst*cp.dst - bx*bx));
            if ((cp.azDeg_t() > 90.0) && (cp.azDeg_t() < 270.0)) by = -by;
            bz = t.heightApprox(bx, by);
            limes = bz;
            crash = (cp.hght <= limes);
            done = crash || (cp.it >= cp.maxIt);
        }
        
        if (crash) {
            addPoint(cp.dst, limes);
            cp.tTot = cp.it * cp.dt();
        }
    }
    
    public void addPoint(StromboliPoint p)
    {
        points.addElement(p);
    }
    
    public void addPoint(double x, double y)
    {
        addPoint(new StromboliPoint(x, y)); 
    }
    
    public void drawForTerrainPanel(Graphics g, int yOffset, double scaleMtoPix)
    {
        long curTime = (new Date()).getTime();
        StromboliPoint prevPoint = (StromboliPoint)points.elementAt(0);
        StromboliPoint curPoint;
        int i, maxi;
        int x0, y0, x1, y1;
        boolean notComplete;
        
        if (!realtimeDraw) {
            notComplete = false;
            maxi = points.size();
        } else {
            notComplete = true;
            maxi = (int)((curTime-startedAt) / (cp.dt() * 1000.0));
            if (maxi >= points.size()) {
                maxi = points.size();
                notComplete = false;
            }
        }

        if (!isSelected) {
            g.setColor(notComplete ? Color.red : Color.black);
        } else {
            g.setColor(notComplete ? (new Color(.65f, .33f, .90f)) : (new Color(.22f, .49f, .80f)));
        }
        
        x0 = (int)(prevPoint.x * scaleMtoPix);
        y0 = (int)(prevPoint.y * scaleMtoPix);
        
        for (i=1; i<maxi; i++) {
            curPoint = (StromboliPoint)points.elementAt(i);
            x1 = (int)(curPoint.x * scaleMtoPix);
            y1 = (int)(curPoint.y * scaleMtoPix);
            
            // x2: position of last draw
            if (x1-x0 > 3 || i==maxi-1) {
                g.drawLine(x0, yOffset-y0, x1, yOffset-y1);
                x0 = x1;
                y0 = y1;
            }
            
            prevPoint = curPoint;
        }

        if (!notComplete) {
            Font f = new Font("sans-serif", Font.PLAIN, 10);
            g.setColor(Color.orange);
            g.setFont(f);
            g.drawString(((int)cp.dst)+"m", (int)(prevPoint.x*scaleMtoPix)-20,	
                yOffset-(int)(prevPoint.y*scaleMtoPix)+15);
        }
    }
    
    public java.util.Vector xyForMapPanel(int x0, int y0, double xMtoPix, double yMtoPix)
    {
        boolean notComplete;
        long curTime = (new Date()).getTime();
        double lengthFactor, curveLength;
        double curveAngle;
        int dx, dy, x1, y1;
        Vector v = new Vector();

        if (!realtimeDraw) {
            notComplete = false;
            lengthFactor = 1.0;
        } else {
            notComplete = true;
            lengthFactor = (double)(curTime-startedAt) / 1000.0 / (double)(cp.tTot);
            if (lengthFactor > 1.0) lengthFactor = 1.0;
            else if (lengthFactor < 0.0) lengthFactor = 0.0;
            if (lengthFactor >= 1.0) notComplete = false;
        }

        curveAngle = this.azDeg_m();
        curveLength = this.dst() * lengthFactor;
        dx = (int)(xMtoPix * curveLength * Math.sin(curveAngle / 180.0 * Math.PI));
        dy = (int)(yMtoPix * curveLength * -Math.cos(curveAngle / 180.0 * Math.PI));
        x1 = x0+dx; y1 = y0+dy;
        
        v.addElement(new Integer(x1));
        v.addElement(new Integer(y1));
        v.addElement(new Boolean(notComplete));
        return v;
    }

    public void drawForMapPanel(Graphics g, int x0, int y0, double xMtoPix, double yMtoPix)
    {
        boolean notComplete = false;
        int x, y;
        Vector v;
        
        v = xyForMapPanel(x0, y0, xMtoPix, yMtoPix);
        x = ((Integer)(v.elementAt(0))).intValue();
        y = ((Integer)(v.elementAt(1))).intValue();
        notComplete = ((Boolean)(v.elementAt(2))).booleanValue();
        
        g.setColor(notComplete ? Color.red : Color.blue);
        
        // draw an 'x' at the current bomb position -w-h-i-l-e- -i-t- -i-s- -i-n- -f-l-i-g-h-t-
//        if (notComplete) {
            g.drawLine(x-3, y-3, x+3, y+3);
            g.drawLine(x-3, y+3, x+3, y-3);
//        }
        if (isSelected) {
            g.drawOval(x-4, y-4, 8, 8);
        }
    }
}
