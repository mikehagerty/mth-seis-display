/*
 *  StromboliCurves.java
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
 *  Created by Andreas Schweizer on Wed Dec 18 2002.
 *  Copyright (c) 2002 Andreas Schweizer.
 *
 */

import java.awt.*;
import java.util.Vector;

// Container class to manage all curves that have been created in the applet.

public class StromboliCurves extends java.util.Observable
implements StromboliTimerListener
{
    private Vector curves;
    private boolean simulateRealtimeCurves;
    private StromboliTimerThread timerThread;
    private long updateIntervalMillisecs;
    private StromboliCurveDescriptor selectedCurve;
    private boolean axialLinesForMapPanel = false;
    private boolean radialLinesForMapPanel = true;
    
    public StromboliCurves(boolean rtc)
    {
        curves = new Vector();
        selectedCurve = null;
        
        simulateRealtimeCurves = rtc;
        updateIntervalMillisecs = 200;
        
        timerThread = new StromboliTimerThread(this, updateIntervalMillisecs);
        timerThread.start();
    }
    
    public Vector curves()
    {
        return curves;
    }
    
    public int size()
    {
        return curves.size();
    }
    
    public int indexOf(StromboliCurveDescriptor c)
    {
        return curves.indexOf(c);
    }
    
    public StromboliCurveDescriptor elementAt(int i)
    {
        if (i>=curves.size()) return null;
        else return (StromboliCurveDescriptor)curves.elementAt(i);
    }
    
    // If "realtime" is active, listeners get periodic messages to 
    // update the curves as they develop. The timebase is a separate
    // thread which we're an listener of -- it periodically calls our
    // 'tick' method.
    
    public boolean usesRealtime()
    {
        return simulateRealtimeCurves;
    }
    
    public void setUsesRealtime(boolean newRtc)
    {
        simulateRealtimeCurves = newRtc;
    }
private boolean withAir = false;
public void setWithAir(boolean x) { withAir = x; }
    
    public int nofBombsInFlight()
    {
        StromboliCurveDescriptor c;
        int i, n=0;
        
        for (i=0; i<curves.size(); i++) {
            try {
                c = (StromboliCurveDescriptor)curves.elementAt(i);
                if (c.inFlight()) n++;
            } catch (Exception e) { }
        }
        
        return n;
    }
    
    public void tick()
    {
        if (nofBombsInFlight() > 0) {
            setChanged();
            notifyObservers(new StromboliCurvesMessage(StromboliCurvesMessage.REALTIME_TICK));
        }
    }
    
    // This container takes no curves from outside but calculates them
    // on request, and removes them on request.
    
    public void calculateCurveFromParams(StromboliCalculationParams p, StromboliTerrain t)
    {
        StromboliCurveDescriptor c0 = new StromboliCurveDescriptor(p, t, simulateRealtimeCurves);
// todo
c0.setWithAir(withAir);
        c0.calculate();
        curves.addElement(c0);
        setChanged();
        notifyObservers(new StromboliCurvesMessage(StromboliCurvesMessage.CURVE_ADDED));
        if (selectedCurve != null) selectCurve(c0);
    }
    
    public StromboliCurveDescriptor findCurveAt(double azDeg_t, double x, double y, double maxDelta)
    {
        StromboliCurveDescriptor c, c1 = null;
        double yc1;
        int i;
        
        for (i=curves.size()-1; i>=0; i--) {
            try {
                c = (StromboliCurveDescriptor)curves.elementAt(i);
                if (isAngleCloseEnough(azDeg_t, c.azDeg_t())) {
                    yc1 = c.heightAtDistance(x);
                    if (Math.abs(yc1-y) <= maxDelta) {
                        c1 = c;
                        i = -1;
                    }
                }
            } catch (Exception e) { }
        }
        
        return c1;
    }
    
    public void removeCurve(StromboliCurveDescriptor c)
    {
        if (c == selectedCurve) { selectedCurve = null; }
        curves.removeElement(c);
        setChanged();
        notifyObservers(new StromboliCurvesMessage(StromboliCurvesMessage.CURVE_REMOVED));
        if (selectedCurve() != null) selectCurve(null);
    }
    
    public void removeLastCurve()
    {
        StromboliCurveDescriptor c;
        
        if (curves.size() > 0) {
            c = (StromboliCurveDescriptor)curves.elementAt(curves.size()-1);
            removeCurve(c);
        }
    }
    
    public void removeAllCurves()
    {
        curves.removeAllElements();
        setChanged();
        notifyObservers(new StromboliCurvesMessage(StromboliCurvesMessage.ALL_CURVES_REMOVED));
        selectCurve(null);
    }
    
    public void selectCurve(StromboliCurveDescriptor c)
    {
        if (selectedCurve != null) selectedCurve.setSelected(false);
        selectedCurve = c;
        if (c != null) c.setSelected(true);
        setChanged();
        notifyObservers(new StromboliCurvesMessage(StromboliCurvesMessage.SELECTION_CHANGED));
    }
    
    public StromboliCurveDescriptor selectedCurve()
    {
        return selectedCurve;
    }
    
    public int selectedCurveIndex()
    {
        return indexOf(selectedCurve);
    }
    
    public void selectCurveAtIndex(int ix)
    {
        selectCurve(elementAt(ix));
    }
    
    public boolean isAngleCloseEnough(double a, double b)
    {
        double c = a-b;
        if (c<0) c = -c;
        return (c <= 0.5);
    }
    
    public void drawForTerrainPanel(Graphics g, double azDeg_t, int yOffset, double scaleMtoPix)
    {
        StromboliCurveDescriptor c;
        int i;
        
        for (i=0; i<curves.size(); i++) {
            try {
                c = (StromboliCurveDescriptor)curves.elementAt(i);
                if (isAngleCloseEnough(azDeg_t, c.azDeg_t())) {
                    c.drawForTerrainPanel(g, yOffset, scaleMtoPix);
                }
            } catch (Exception e) { }
        }
    }
    
    public void setAxialLinesForMapPanel(boolean fl)
    {
        axialLinesForMapPanel = fl;
    }
    
    public void setRadialLinesForMapPanel(boolean fl)
    {
        radialLinesForMapPanel = fl;
    }

    public void drawForMapPanel(Graphics g, int x0, int y0, double xMtoPix, double yMtoPix)
    {
        StromboliCurveDescriptor c;
        Vector vOld, vNew;
        double azDegOld;
        int i;
        
        vOld = null;
        azDegOld = 0.0;
        for (i=0; i<curves.size(); i++) {
            try {
                c = (StromboliCurveDescriptor)curves.elementAt(i);
                c.drawForMapPanel(g, x0, y0, xMtoPix, yMtoPix);
                vNew = c.xyForMapPanel(x0, y0, xMtoPix, yMtoPix);
                if (radialLinesForMapPanel) {
                    g.setColor(((Boolean)(vNew.elementAt(2))).booleanValue() ? Color.red : Color.blue);
                    g.drawLine(x0, y0, 
                            ((Integer)vNew.elementAt(0)).intValue(), ((Integer)vNew.elementAt(1)).intValue());
                }
                if (axialLinesForMapPanel) {
                    if (vOld != null && azDegOld < c.azDeg_m()) {
                        g.setColor(Color.blue);
                        g.drawLine(((Integer)vOld.elementAt(0)).intValue(), ((Integer)vOld.elementAt(1)).intValue(), 
                            ((Integer)vNew.elementAt(0)).intValue(), ((Integer)vNew.elementAt(1)).intValue());
                    }
                    vOld = vNew;
                    azDegOld = c.azDeg_m();
                }
            } catch (Exception e) { }
        }
    }
}
