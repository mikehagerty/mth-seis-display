/*
 *  StromboliCalculationParams.java
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

import java.applet.Applet;


public class StromboliCalculationParams extends java.util.Observable
implements java.lang.Cloneable
{
    // MVC-controlled private data members
    private double azDeg;	// map angle
    private double alpha0;	// initial elevation
    private double v0;		// initial velocity
    private int cwSel;		// shape index
    private double cw;		// shape resistance value, read-only
    private double diam;	// bomb diameter (meters!)
    private double density;	// bomb density (kg/m3)
    private double rho;		// air density
    private double g;		// gravitative acceleration
    private double dt;		// iteration time resolution
    private double stepWidth;	// map calculation resolution
    // double volumen;		// of bomb; calculated on request
    // double m0;		// of bomb; calculated on request
    // double acr;		// bomb front area, calc-on-req

    // Bomb-instance specific parameters, change during flight time
    public int it, maxIt;
    public double mr;		// frictive module
    public double fgx, fgy;
    public double hght,dst;
    public double vex, vey;
    public double tTot;
    private final int kBarometric = 1;
    private final int kInternational = 2;
    private final int kDisabled = 0;
    public int adaptiveAirPressure = kBarometric;
    public boolean pValid;

    private StromboliApplet app;

    public double getDefaultForParam(String param)
    {
        double pvalNum = 0.0;
        String pval;
        
        pValid = false;
        pval = app.getParameter(param);
        if (pval != null) {
            try {
                pvalNum = Double.valueOf(pval).doubleValue();
                pValid = true;
            } catch (Exception e) { }
        }    
        return pvalNum;
    }
    
    public StromboliCalculationParams(StromboliApplet app)
    {
        this.app = app;
        azDeg  = getDefaultForParam("dflt_az_deg"); if (!pValid) azDeg = 0.0; 
        alpha0 = getDefaultForParam("dflt_alpha0"); if (!pValid) alpha0 = 30.0; 
        v0 = getDefaultForParam("dflt_v0"); if (!pValid) v0 = 100.0; 
        cwSel = (int)(getDefaultForParam("dflt_cw_idx")+0.5); if (!pValid) cwSel = 0; 
        setCwSel(cwSel);
        diam = getDefaultForParam("dflt_diam_m"); if (!pValid) diam = 0.1; 
        density = getDefaultForParam("dflt_bomb_density_kg_m3"); if (!pValid) density = 2000.0; 
        rho = getDefaultForParam("dflt_air_density_kg_m3"); if (!pValid) rho = 1.293; 
        g = getDefaultForParam("dflt_grav_m_s2"); if (!pValid) g = 9.81; 
        dt = getDefaultForParam("dflt_dt_s"); if (!pValid) dt = 0.1; 
        stepWidth = getDefaultForParam("dflt_terrain_dx_m"); if (!pValid) stepWidth = 50.0; 
        it = 0; 
        maxIt = (int)(getDefaultForParam("dflt_max_iterations")+0.5); if (!pValid) maxIt = 5000; 
        tTot = 0.0;
        
        adaptiveAirPressure = kBarometric;
        String adcStr = app.getParameter("air_density_control");
        if (adcStr != null) {
            if (adcStr.equals("international")) adaptiveAirPressure = kInternational; 
            else if (adcStr.equals("constant")) adaptiveAirPressure = kDisabled;
        }
    }
    
    // We have *two* angle functions - one for the terrain data set, the
    // second one for the displayed map. As these might be slightly rotated
    // against each other, we need to be able to correct that. To that purpose, 
    // we modify the angle for the terrain ad-hoc (not the map angle because
    // the user sets the angle in the *map*)
    public double azDeg_t() { return azDeg + 3.0; /* for terrain */ }
    public double azDeg_m() { return azDeg; }
    public double alpha0() { return alpha0; }
    public double v0() { return v0; }
    public int cwSel() { return cwSel; }
    public double cw() { return cw; }
    public double diam() { return diam; }
    public double density() { return density; }
    public double rho()
    { 
        if (adaptiveAirPressure == kBarometric) {
            return rho*Math.exp(-rho*g()*hght/101325);
        } else if (adaptiveAirPressure == kInternational) {
            return 1.2255*Math.pow(1.0-(0.00651/288.0)*hght, 4.255);
        } else return rho; 
    }
    public double g() { return g; }
    public double dt() { return dt; }
    public double stepWidth() { return stepWidth; }
    
    public String shapeDescriptor()
    {
        String shname = "???", nkss;
        double _m0;
        int _m0_int;
        
        switch (cwSel) {
            case 0: shname = StromboliApplet._s(StromboliApplet._s_sphere); break;
            case 1: shname = StromboliApplet._s(StromboliApplet._s_dropShape)+" 2:1"; break;
            case 2: shname = StromboliApplet._s(StromboliApplet._s_dropShape)+" 3:1"; break;            
        }
        _m0 = m0()+0.0005;
        _m0_int = (int)_m0;
        _m0 -= _m0_int;
        nkss = ""+(int)(1000*_m0);
        while (nkss.length() < 3) nkss = "0"+nkss;
        return shname + "   (d="+(int)(100*(diam+0.005))+"cm, m="+_m0_int+"."+nkss+"kg"+
            (rho()==0 ? ", "+StromboliApplet._s(StromboliApplet._s_vacuum) : "") + ")";
    }
    
    public void setAzDeg_m(double x) { azDeg = x; setChanged(); }
    public void setAzDeg_t(double x) { azDeg = x-3.0; setChanged(); }
    public void setAlpha0(double x) { alpha0 = x; setChanged(); }
    public void setV0(double x) { v0 = x; setChanged(); }
    public void setCwSel(int x) { cwSel = x; 
        switch (cwSel) {
            case 0: cw = 0.47; break;
            case 1: cw = 0.2; break;
            case 2: cw = 0.1; break;
            case 3: cw = 0.05; break;
            default: cw = 0; break;
        }
        setChanged(); }
    public void setDiam(double x) { diam = x; setChanged(); }
    public void setDensity(double x) { density = x; setChanged(); }
    public void setRho(double x) { rho = x; setChanged(); }
    public void setG(double x) { g = x; setChanged(); }
    public void setDt(double x) { dt = x; setChanged(); }
    public void setStepWidth(double x) { stepWidth = x; setChanged(); }
    
    public double volumen()
    {
        double r = diam() / 2.0;
        double vol = 0.0;

        switch (cwSel()) {
            case 0: vol = 4.0/3.0 * Math.PI * Math.pow(r,3); break;            
            case 1: vol = 5.0/3.0 * Math.PI * Math.pow(r,3); break;
            case 2: vol = 7.0/3.0 * Math.PI * Math.pow(r,3); break;
            default: vol = 4.0/3.0 * Math.PI * Math.pow(r,3); break;
        }

        return vol;
    }
    public double m0()
    {
        return volumen() * density();
    }
    public double acr()
    {
        double r = diam() / 2.0;
        return (r*r*Math.PI);
    }

    public void notifyNow()
    {
        notifyObservers();
    }
    
    protected Object clone()
    throws java.lang.CloneNotSupportedException
    {
        return super.clone();
    }
}
