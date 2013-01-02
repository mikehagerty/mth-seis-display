/*
 *  StromboliApplet.java
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
import java.awt.event.*;
import java.applet.Applet;

public class StromboliApplet extends Applet
implements ActionListener
{
    static final private String _s[] = {
        "Startwinkel", "Launch Angle", "Angolo di lancio", 
        "Startgeschwindigkeit", "Start velocity", "Velocitˆ di lancio", 
        "Bombenform", "Bomb shape", "Forma della bomba", 
        "Kugel", "Sphere", "Sfera", 
        "Tropfen", "Drop", "A goccia", 
        "Durchmesser", "Diameter", "Diametro", 
        "Dichte der Bombe", "Specific weight of bomb", "Peso spec. della bomba", 
        "Bombengewicht", "Weight of bomb", "Peso della bomba", 
        "Dichte der Luft", "Density of air", "Densitˆ dell'aria", 
        "Erdbeschleunigung", "Gravitational acceleration", "Accelerazione gravitazionale", 
        "Letzte Bahn lšschen", "Delete last trajectory", "Cancella l'ultima traiettoria", 
        "Alle Bahnen loeschen", "Delete all trajectories", "Cancella tutte le traiettorie", 
        "Berechnen", "Calculate", "Calcola", 
        "Auswurfrichtung", "Direction of trajectory", "Direzione della traiettoria", 
        "Bombe fliegt in Echtzeit", "Bomb flies in real time", "I voli delle bombe in tempo reale", 
        "Luftwiderstand berŸcksichtigen", "Include air resistance", "Includi resistenza dell'aria", 
        "   -> Barometerformel", "?", "?", 
        "Kurve", "Path", "Traiettoria", 
        "Form", "Shape", "Forma", 
        "Flugdistanz", "Flight distance", "Distanza del volo", 
        "Flugdauer", "Flight duration", "Durata del volo", 
        "h(max)", "h(max)", "h(max)", 
        "bei Distanz", "at distance", "alla distanza", 
        "Vakuum", "vacuum", "vacuum", 
        "Startenergie", "Start energy", "Energia iniziale", 
    };
    static final public int _slen = 3;
    static public int _six = 0;
    static final public int _s_launchAngle = 0;
    static final public int _s_launchVeloc = 1;
    static final public int _s_bombShape   = 2;
    static final public int _s_sphere      = 3;
    static final public int _s_dropShape   = 4;
    static final public int _s_diameter    = 5;
    static final public int _s_specBombWgt = 6;
    static final public int _s_bombWgt     = 7;
    static final public int _s_airDensity  = 8;
    static final public int _s_gravAccel   = 9;
    static final public int _s_deleteLast  = 10;
    static final public int _s_deleteAll   = 11;
    static final public int _s_calculate   = 12;
    static final public int _s_ejectionDeg = 13;
    static final public int _s_realtime    = 14;
    static final public int _s_withCw      = 15;
    static final public int _s_baromF      = 16;
    static final public int _s_curvenr     = 17;
    static final public int _s_shape       = 18;
    static final public int _s_flightDist  = 19;
    static final public int _s_flightTime  = 20;
    static final public int _s_hMax        = 21;
    static final public int _s_atDist      = 22;
    static final public int _s_vacuum      = 23;
    static final public int _s_startEnergy = 24;

    static final private int _t[] = {
        340, 340, 350, 				// xsize-340
        80,  100, 108,				// tab1
        141, 161, 171, 				// tab2
        230, 260, 271, 				// tab3
        261, 260, 271, 				// tab4
        0,   1,   1, 				// azdegNewLine
    };
    static final public int _t_hsize       = 0;
    static final public int _t_tab1        = 1;
    static final public int _t_tab2        = 2;
    static final public int _t_tab3        = 3;
    static final public int _t_tab4        = 4;
    static final public int _t_azdegnl     = 5;

    private Frame dummyFrame;
    private Color appletBgColor;
    private StromboliCalculationParams cp;

    private StromboliMapPanel mapPanel;
    private StromboliSettingsPanel settingsPanel;
    private Panel buttonPanel;
    private StromboliCurvePanel curvePanel;

    private ScrollPane curveScrollPane;    

    private Button startCalculationBUT;
    private Button removeLastCurveBUT;
    private Button removeAllCurvesBUT;
    
    private StromboliTerrain terrain;
    private StromboliCurves curves;	// curves container (MVC model)
        
    static public boolean _debug_check_map_vs_terrain = false;
    static public boolean _debug_loop_ign_az_deg = false;
    static public double _debug_loop_az_deg_step = 15.0;
    static public boolean _easteregg1 = false;


    static public String _s(int ix)
    {
        return _s[_slen*ix+_six];
    }

    static public int _t(int ix)
    {
        return _t[_slen*ix+_six];
    }
    
    public void init()
    {
        curves = new StromboliCurves(true);	// true: 'realtime' curves
        terrain = new StromboliTerrain(new StromboliTerrainData());
        
        String language = this.getParameter("language");
        if (language != null) {
            if (language.equals("de")) _six = 0; 
            else if (language.equals("en")) _six = 1;
            else if (language.equals("it")) _six = 2;
        }

        String debug1 = this.getParameter("debug_check_map_vs_terrain");
        if (debug1 != null) {
            if (debug1.equals("yes")) _debug_check_map_vs_terrain = true;
        }
        
        String debug2 = this.getParameter("debug_loop_ign_az_deg");
        if (debug2 != null) {
            if (debug2.equals("yes")) _debug_loop_ign_az_deg = true;
        }
        
        String s = getParameter("debug_loop_az_deg_step");
        if (s != null) {
            try { _debug_loop_az_deg_step = Double.valueOf(s).doubleValue(); }
            catch (Exception e) { _debug_loop_az_deg_step = 15.0; }
            if (_debug_loop_az_deg_step < 2.0) _debug_loop_az_deg_step = 2.0;
            else if (_debug_loop_az_deg_step > 180.0) _debug_loop_az_deg_step = 180.0;
        }

        String eegg1 = this.getParameter("enable_easteregg1");
        if (eegg1 != null) {
            if (eegg1.equals("yes")) _easteregg1 = true;
        }        

        cp = new StromboliCalculationParams(this);
        appletBgColor = new Color(.933f, .933f, .933f);
        
        this.setLayout(new GridBagLayout());
        
        dummyFrame = new Frame();
        
        settingsPanel = new StromboliSettingsPanel(this, cp, appletBgColor);        
        buttonPanel = new Panel();
        GridBagLayout buttonGridbag = new GridBagLayout();
        buttonPanel.setLayout(buttonGridbag);

        startCalculationBUT = new Button(StromboliApplet._s(_s_calculate));
        startCalculationBUT.setBackground(appletBgColor);
        startCalculationBUT.addActionListener(this);

        removeLastCurveBUT = new Button(StromboliApplet._s(_s_deleteLast));
        removeLastCurveBUT.setBackground(appletBgColor);
        removeLastCurveBUT.addActionListener(this);

        removeAllCurvesBUT = new Button(StromboliApplet._s(_s_deleteAll));
        removeAllCurvesBUT.setBackground(appletBgColor);
        removeAllCurvesBUT.addActionListener(this);

        constrain(buttonPanel, removeLastCurveBUT,
            1,0,1,1,GridBagConstraints.NONE,GridBagConstraints.SOUTH,1.0,1.0,2,13,2,2);
        constrain(buttonPanel, startCalculationBUT,
            0,1,1,1,GridBagConstraints.NONE,GridBagConstraints.SOUTH,1.0,1.0,2,2,2,2);
        constrain(buttonPanel, removeAllCurvesBUT,
            1,1,1,1,GridBagConstraints.NONE,GridBagConstraints.SOUTH,1.0,1.0,2,13,2,2);
            
        Panel topPanel = new Panel();
        GridBagLayout mainGridbag = new GridBagLayout();
        topPanel.setLayout(mainGridbag);
        
        constrain(topPanel, settingsPanel,
            0,0,1,1,GridBagConstraints.HORIZONTAL,GridBagConstraints.CENTER,0.0,0.0,2,2,2,2);
        constrain(topPanel, buttonPanel,
            0,1,1,1,GridBagConstraints.NONE,GridBagConstraints.EAST,0.0,0.0,2,2,2,2);
                                    
        mapPanel = new StromboliMapPanel(this, cp, curves);
        curvePanel = new StromboliCurvePanel(cp, curves, terrain, this);
        
//        curveScrollPane = new ScrollPane();
//        curveScrollPane.add(curvePanel);
//        curveScrollPane.setSize(650, 261);
        
        constrain(this, mapPanel,
            0,0,1,2,GridBagConstraints.NONE,GridBagConstraints.WEST,0.0,0.0,13,13,13,13);

        constrain(this, topPanel,
            1,0,1,1,GridBagConstraints.HORIZONTAL,GridBagConstraints.NORTHWEST,0.0,0.0,13,0,13,13);
        constrain(this, buttonPanel,
            1,1,1,1,GridBagConstraints.HORIZONTAL,GridBagConstraints.SOUTH,0.0,0.0,13,0,13,13);

        constrain(this, curvePanel,
            0,2,2,1,GridBagConstraints.NONE,GridBagConstraints.CENTER,0.0,0.0,2,2,2,2);
//        this.add(mapPanel);
//        this.add(topPanel);
//        this.add(curveScrollPane);
//        this.add(buttonPanel);

        this.setBackground(appletBgColor);
        cp.notifyNow();
    }

    public void paint(Graphics g)
    {
        super.paint(g);    
    }
    
    public void calculate()
    {
        double curAzDeg = 0.0;
        double initialAzDeg;
        
//        cp.adaptiveAirPressure = settingsPanel.baromF();
        curves.setUsesRealtime(settingsPanel.inRealtime());
        curves.setWithAir(settingsPanel.withAir());
        initialAzDeg = cp.azDeg_m();
        do {
            if (_debug_loop_ign_az_deg) cp.setAzDeg_m(curAzDeg);
            curves.calculateCurveFromParams(cp, terrain);
            curAzDeg += _debug_loop_az_deg_step;
        } while (_debug_loop_ign_az_deg && curAzDeg < 360.0);
        cp.setAzDeg_m(initialAzDeg);
    }

    public void actionPerformed(ActionEvent e)
    {
        double m0, alpha0, v0;
        Object s = e.getSource();
        
        if (s == startCalculationBUT) {
            calculate();
                
        } else if (s == removeLastCurveBUT) {
            curves.removeLastCurve();
            
        } else if (s == removeAllCurvesBUT) {
            curves.removeAllCurves();
        }
    }

    public void constrain(Container container, Component component,
                          int grid_x, int grid_y, 
                          int grid_width, int grid_height,
                          int fill, int anchor,
                          double weight_x, double weight_y,
                          int top, int left, int bottom, int right) {
        // This is a part of GridBagLayout to constrain how components are
        // layed out in a frame (allowing for window enlargement etc.)

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = grid_x; c.gridy = grid_y;
        c.gridwidth = grid_width; c.gridheight = grid_height;
        c.fill = fill; c.anchor = anchor;
        c.weightx = weight_x; c.weighty = weight_y;
        if (top + bottom + left + right > 0)
        {
          c.insets = new Insets(top,left,bottom,right);
        }
        
        ((GridBagLayout)container.getLayout()).setConstraints(component,c);
        container.add(component);
    }
}
