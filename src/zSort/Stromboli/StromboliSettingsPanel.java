/*
 *  StromboliSettingsPanel.java
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
 *  Created by Andreas Schweizer on Sat Nov 30 2002.
 *  Copyright (c) 2002 Andreas Schweizer.
 *
 */

import java.lang.Math.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;


public class StromboliSettingsPanel extends Panel
implements ItemListener, TextListener, ActionListener, java.util.Observer
{
    private StromboliApplet appl;
    private StromboliCalculationParams cp;
    private TextField bombElevationTF;
    private TextField bombV0TF;
    private Choice cwSelector;
    private TextField bombDiameterTF;
    private TextField bombDensityTF;
    private Label bombM0NameLabel;
    private Label bombM0Label;
    private TextField airDensityTF;
    private TextField gTF;
    private Checkbox realtimeCheckbox;
    private Checkbox withCwCheckbox;
    private Checkbox baromFCheckbox;
    
    private double curBombElevation, curBombV0;
    private int curCwSel;
    private double curBombDiameter, curBombDensity, curBombM0;
    private double curAirDensity, curG;

    public StromboliSettingsPanel(StromboliApplet appl, StromboliCalculationParams cp, Color bgColor)
    {
        this.cp = cp;
        this.appl = appl;
        cp.addObserver(this);
        this.setLayout(new GridBagLayout());

        bombElevationTF = new TextField("0", 6);	curBombElevation = 0;
        bombV0TF = new TextField("0", 6);		curBombV0 = 0;
        cwSelector = new Choice();			curCwSel = 0;
        cwSelector.setBackground(bgColor);
        cwSelector.addItem(StromboliApplet._s(StromboliApplet._s_sphere));
        cwSelector.addItem(StromboliApplet._s(StromboliApplet._s_dropShape) + " 2:1");
        cwSelector.addItem(StromboliApplet._s(StromboliApplet._s_dropShape) + " 3:1");
        cwSelector.addItemListener(this);
        bombDiameterTF = new TextField("0", 6);	curBombDiameter = 0;
        bombDensityTF = new TextField("0", 6);		curBombDensity = 0;
        bombM0Label = new Label("0");			curBombM0 = 0;
        airDensityTF = new TextField("0", 6);		curAirDensity = 0;
        gTF = new TextField("0", 6);			curG = 0;

        realtimeCheckbox = new Checkbox(StromboliApplet._s(StromboliApplet._s_realtime));
        realtimeCheckbox.setState(true);
        realtimeCheckbox.setBackground(bgColor);

        withCwCheckbox = new Checkbox(StromboliApplet._s(StromboliApplet._s_withCw));
        withCwCheckbox.setState(true);
        withCwCheckbox.setBackground(bgColor);

        baromFCheckbox = new Checkbox(StromboliApplet._s(StromboliApplet._s_baromF));
        baromFCheckbox.setState(false);
        baromFCheckbox.setBackground(bgColor);

        bombElevationTF.addTextListener(this);
        bombElevationTF.addActionListener(this);
        bombV0TF.addTextListener(this);
        bombV0TF.addActionListener(this);
        bombDiameterTF.addTextListener(this);
        bombDiameterTF.addActionListener(this);
        bombDensityTF.addTextListener(this);
        bombDensityTF.addActionListener(this);
        airDensityTF.addTextListener(this);
        airDensityTF.addActionListener(this);
        gTF.addTextListener(this);
        gTF.addActionListener(this);

        Label l;
        int i = 0;

        l = new Label(StromboliApplet._s(StromboliApplet._s_launchAngle));
        l.setBackground(bgColor);
        constrain(this, l,
            0,i,1,1,GridBagConstraints.NONE,GridBagConstraints.WEST,1.0,0.0,2,2,2,2);
        constrain(this, bombElevationTF,
            1,i++,1,1,GridBagConstraints.HORIZONTAL,GridBagConstraints.EAST,0.8,0.0,2,22,2,2);

        l = new Label(StromboliApplet._s(StromboliApplet._s_launchVeloc)+" [m/s]:");
        l.setBackground(bgColor);
        constrain(this, l,
            0,i,1,1,GridBagConstraints.NONE,GridBagConstraints.WEST,1.0,0.0,2,2,2,2);
        constrain(this, bombV0TF,
            1,i++,1,1,GridBagConstraints.HORIZONTAL,GridBagConstraints.EAST,0.8,0.0,2,22,2,2);

        l = new Label(StromboliApplet._s(StromboliApplet._s_bombShape)+": ");
        l.setBackground(bgColor);
        constrain(this,l,
            0,i,1,1,GridBagConstraints.NONE, GridBagConstraints.WEST,1.0,0.0,18,2,2,2);
        constrain(this,cwSelector,
            1,i++,1,1,GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,0.8,0.0,18,2,2,2);

        l = new Label(StromboliApplet._s(StromboliApplet._s_diameter)+" [cm]: ");
        l.setBackground(bgColor);
        constrain(this,l,
            0,i,1,1,GridBagConstraints.NONE, GridBagConstraints.WEST,1.0,0.0,2,2,2,2);
        constrain(this,bombDiameterTF,
            1,i++,1,1,GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST,0.6,0.0,2,22,2,2);

        l = new Label(StromboliApplet._s(StromboliApplet._s_specBombWgt)+" [kg/m3]:");
        l.setBackground(bgColor);
        constrain(this, l,
            0,i,1,1,GridBagConstraints.NONE,GridBagConstraints.WEST,1.0,0.0,2,2,2,2);
        constrain(this, bombDensityTF,
            1,i++,1,1,GridBagConstraints.HORIZONTAL,GridBagConstraints.EAST,0.8,0.0,2,22,2,2);

        bombM0NameLabel = new Label(StromboliApplet._s(StromboliApplet._s_bombWgt)+" [kg]:");
        bombM0NameLabel.setBackground(bgColor);
        bombM0Label.setBackground(bgColor);
        constrain(this, bombM0NameLabel,
            0,i,1,1,GridBagConstraints.NONE,GridBagConstraints.WEST,1.0,0.0,2,2,2,2);
        constrain(this, bombM0Label,
            1,i++,1,1,GridBagConstraints.HORIZONTAL,GridBagConstraints.EAST,0.8,0.0,2,22,2,2);

/*
        l = new Label("Dichte der Luft [kg/m3]:");
        l.setBackground(bgColor);
        constrain(this, l,
            0,i,1,1,GridBagConstraints.NONE,GridBagConstraints.WEST,1.0,0.0,18,2,2,2);
        constrain(this, airDensityTF,
            1,i++,1,1,GridBagConstraints.HORIZONTAL,GridBagConstraints.EAST,0.8,0.0,18,22,2,2);
*/
/*
        l = new Label("Erdbeschleunigung [m/s2]:");
        l.setBackground(bgColor);
        constrain(this, l,
            0,i,1,1,GridBagConstraints.NONE,GridBagConstraints.WEST,1.0,0.0,2,2,2,2);
        constrain(this, gTF,
            1,i++,1,1,GridBagConstraints.HORIZONTAL,GridBagConstraints.EAST,0.8,0.0,2,22,2,2);
*/
        constrain(this, realtimeCheckbox,
            0,i++,2,1,GridBagConstraints.HORIZONTAL,GridBagConstraints.EAST,0.8,0.0,18,22,2,2);

        constrain(this, withCwCheckbox,
            0,i++,2,1,GridBagConstraints.HORIZONTAL,GridBagConstraints.EAST,0.8,0.0,8,22,2,2);

/*
        constrain(this, baromFCheckbox,
            0,i++,2,1,GridBagConstraints.HORIZONTAL,GridBagConstraints.EAST,0.8,0.0,8,22,2,2);
*/            
        // force us to read the current field values from data source
        this.update(cp, null);
    }
    
    protected void setFieldColors()
    {
        if (curBombElevation > 0.0 && curBombElevation < 90.0) bombElevationTF.setForeground(Color.black);
        else bombElevationTF.setForeground(Color.red);
        if (curBombV0 > 0.0 && curBombV0 < 2000.0) bombV0TF.setForeground(Color.black);
        else bombV0TF.setForeground(Color.red);
        if (curBombDiameter > 0.0) bombDiameterTF.setForeground(Color.black);
        else bombDiameterTF.setForeground(Color.red);
        if (curBombDensity > 0.0) bombDensityTF.setForeground(Color.black);
        else bombDensityTF.setForeground(Color.red);
        if (curAirDensity > 0.0) airDensityTF.setForeground(Color.black);
        else airDensityTF.setForeground(Color.red);
        if (curG > 0.0) gTF.setForeground(Color.black);
        else gTF.setForeground(Color.red);
    }
    
    public void update(java.util.Observable obs, Object o)
    {
        double x;
        int y;
        
        if (obs == cp) {
            x = cp.alpha0(); if (curBombElevation != x) {
                bombElevationTF.setText(""+x); curBombElevation = x; }
            x = cp.v0(); if (curBombV0 != x) {
                bombV0TF.setText(""+x); curBombV0 = x; }
            y = cp.cwSel(); if (curCwSel != y) {
                cwSelector.select(y); curCwSel = y; }
            x = cp.diam(); if (curBombDiameter != x) {
                bombDiameterTF.setText(""+(x*100.0)); curBombDiameter = x; }
            x = cp.density(); if (curBombDensity != x) {
                bombDensityTF.setText(""+x); curBombDensity = x; }
            x = cp.m0(); if (curBombM0 != x) {
                bombM0Label.setText(""+(float)(0.1*Math.round(10.0*x))); curBombM0 = x; }
            x = cp.rho(); if (curAirDensity != x) {
                airDensityTF.setText(""+x); curAirDensity = x; }
            x = cp.g(); if (curG != x) {
                gTF.setText(""+x); curG = x; }
            setFieldColors();
        }
    }
    
    public boolean inRealtime()
    {
        return realtimeCheckbox.getState();
    }
    
    public boolean withAir()
    {
        return withCwCheckbox.getState();
    }
       
    public boolean baromF()
    {
        return baromFCheckbox.getState();
    }

    public void actionPerformed(ActionEvent e)
    {
        Object s = e.getSource();

        // should do this for "enter" in text fields
        appl.calculate();
//        if (s == startCalculationBUT) {
//        }
    }
 
    public void itemStateChanged(ItemEvent e)
    {
        Object s = e.getSource();
        if (s == cwSelector) {
            cp.setCwSel(cwSelector.getSelectedIndex());
            setFieldColors();
            cp.notifyNow();
        }
    }
    
    public void textValueChanged(TextEvent e)
    {
        double x;
        Object s = e.getSource();
        if (s == bombElevationTF) {
            x = Double.valueOf(bombElevationTF.getText()).doubleValue();
            curBombElevation = x; cp.setAlpha0(x);
            cp.notifyNow();
        } else if (s == bombV0TF) {
            x = Double.valueOf(bombV0TF.getText()).doubleValue();
            curBombV0 = x; cp.setV0(x);
            cp.notifyNow();
        } else if (s == bombDiameterTF) {
            x = 0.01*Double.valueOf(bombDiameterTF.getText()).doubleValue();
            curBombDiameter = x; cp.setDiam(x);
            cp.notifyNow();
        } else if (s == bombDensityTF) {
            x = Double.valueOf(bombDensityTF.getText()).doubleValue();
            curBombDensity = x; cp.setDensity(x);
            cp.notifyNow();
        } else if (s == airDensityTF) {
            x = Double.valueOf(airDensityTF.getText()).doubleValue();
            curAirDensity = x; cp.setRho(x);
            cp.notifyNow();
        } else if (s == gTF) {
            x = Double.valueOf(gTF.getText()).doubleValue();
            curG = x; cp.setG(x);
            cp.notifyNow();
        }
        setFieldColors();
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
