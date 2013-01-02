/*
 *
 *
 */ 
package mth.seisdrum;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import java.io.*;
import java.util.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.*;

import javax.swing.Timer;

import java.awt.geom.AffineTransform;

import java.lang.reflect.Field;

public class SeismicDrum extends JFrame {

    private Color panelBackgroundColor = new Color(220, 200, 180);

 /** User Display Controls **/
    private JSlider timeSlider  = null;
    private JSlider gainSlider  = null;
    private JSlider speedSlider = null;
    private JButton play = null;
    private JButton stop = null;

 /** Default Display Control Settings **/
    private static final int DEFAULT_TIMESCALE  = 100;
    private static final int DEFAULT_VERTGAIN   = 1;
    private static final int DEFAULT_FRAMEDELAY = 250;
 /** The default color of the seismogram line **/
    private static final Color DEFAULT_LINE_COLOR  = Color.RED;
    private static final Color DEFAULT_PANEL_COLOR = Color.BLACK;

    private DisplayPanel displayPanel = null;

    public SeismicDrum() {              // Lay out the JFrame
        super("File Read: No File");
        setLayout( new BorderLayout());
     // Add main display panel
        displayPanel = new DisplayPanel();
        displayPanel.setPreferredSize( new Dimension(1200,450) );
        add(displayPanel, BorderLayout.CENTER);
        displayPanel.setBackground( DEFAULT_PANEL_COLOR );

     // Add JMenuBar to this JFrame:
        setJMenuBar(displayPanel.createMenuBar());

     // Add the Control Panel to hold various button controls & sliders:
        ControlPanel controlPanel = new ControlPanel();
        controlPanel.setBackground(panelBackgroundColor);
        controlPanel.setPreferredSize( new Dimension(1200,100) );
        add(controlPanel, BorderLayout.SOUTH);

    } // end SeismicDrum() constructor

/**
 *  The main (seismic drum) display panel
 *  It knows how to draw itself
 *  It receives action events from any menu, slider, button, etc. and responds
 */
    private class DisplayPanel extends JPanel
                               implements ActionListener, ChangeListener {

     // Set up drawing area within this JPanel
        private static final int XOFF = 10;           // x-indent pixels from left side of JPanel
        private static final int XEND =120;           // x-indent pixels from right side of JPanel
        private static final int YOFF = 40;           // y-indent pixels from top of JPanel
        private static final int NUMBER_OF_DIVS = 12; // Number of xtic marks passed to Axes

        private boolean drawAxes;
        private boolean drawGrid;
        private boolean drawSeis;
        private boolean drawTics;

        private Axes axes = null;
        private Seis seis = null;

        private javax.swing.Timer timer=null;

        private JFileChooser fileDialog;

        // Drawing coordinates (pixels) within this JPanel --> Initialized when window opens
        private int height, width;
        private int Xlen, Ylen;

        private boolean firstTime = true;

        // constructor(s)
        public DisplayPanel(){
            ActionListener timerAction = new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    repaint();
                }
            };
            timer = new Timer( 1000, timerAction ); // Create timer with default delay
            timer.setCoalesce(false); 
        }

        public void setPanelSizeCoordinates() {
         // Set up length variables that depend on current window size:
         // For a non-resizable window we only need to do this once, the first time through
            width   = getWidth();
            height  = getHeight();
            Xlen    = width  - XOFF - XEND; // Total Width of drawing panel in pixels
            Ylen    = height  -YOFF - YOFF; // Total Height of drawing panel in pixels
        }

        public void paintComponent( Graphics g ) {
            if (firstTime) {
                setPanelSizeCoordinates();
                firstTime = false;
            }
        // Paint a solid background
            super.paintComponent(g);

        // Translate g from <left,top> window coords to <0,0> of our coord axes within the JPanel:
            g.translate(XOFF, height/2);

            if (axes == null) { 
                axes = new Axes(Xlen,Ylen);
                axes.setNumberOfDivs(NUMBER_OF_DIVS);
                axes.setSecondsPerDiv(timeSlider.getValue() );
            }

            if (seis == null) { 
                seis = new Seis(Xlen,Ylen);
                seis.setNumberOfDivs(NUMBER_OF_DIVS);
                seis.setSecondsPerDiv(timeSlider.getValue() );
                seis.setVerticalGain( gainSlider.getValue() );
                seis.setColor( DEFAULT_LINE_COLOR );
            }
//System.out.format("== paintComponent(g): iframe=%d\n", seis.iframe);

            if (drawAxes) {
                axes.draw(g);
            }
            if (drawTics) {
                axes.drawTics(g);
            }
            if (drawGrid) {
                axes.drawGrid(g);
            }
            if (drawSeis) {
                seis.draw(g);
            }
            if (seis.timerOn && !seis.timerPause) seis.iframe++;
            //if (timer.isRunning()) seis.iframe++;

        } // end paintComponent()

        public void actionPerformed(ActionEvent evt) {

            String command = evt.getActionCommand();
            System.out.format("== command=[%s]\n", command);

            if (command.equals("Play")) {
                seis.timerOn = true;
                seis.timerPause = false;
                drawSeis = true;
                play.setText("Pause"); // Change button from Play to Pause
                timer.restart();
System.out.println("== PLAY ==");
            }
            else if (command.equals("Pause")) {
                timer.stop();
                seis.timerPause = true;
                play.setText("Play");  // Change button from Pause to Play
System.out.println("== PAUSE ==");
            }
            else if (command.equals("Stop")) {
                timer.stop();
                seis.timerOn = false;
                seis.timerPause = false;
                seis.iframe = 1; // Reset
                drawSeis = false;
                play.setText("Play"); // Change button from Pause to Play
            }

            else if (command.equals("Open (ASCII File)...")) {
            }
            else if (command.equals("Open (Sac File)...")) {
                seis.readSacFile(this, fileDialog);
                setTitle( String.format("SAC File Read: %s [delta=%.2f]", seis.sourceString, seis.dT) );
                play.setEnabled(true);
                int delay = (int) (seis.dT * 1000.); // Set initial delay = sample rate in millisecs
                speedSlider.setValue(delay);
                timer.setDelay(delay);
            }
            else if (command.equals("Draw Axes")){
                drawAxes = ((JCheckBoxMenuItem)evt.getSource()).isSelected() ;
            }
            else if (command.equals("Draw Tics")){
                drawTics = ((JCheckBoxMenuItem)evt.getSource()).isSelected() ;
            }
            else if (command.equals("Draw Grid")){
                drawGrid = ((JCheckBoxMenuItem)evt.getSource()).isSelected() ;
            }
            else if (command.equals("Draw Seis")){
                drawSeis = ((JCheckBoxMenuItem)evt.getSource()).isSelected() ;
            }
            else if (command.equals("Quit")){
                System.out.println("== Quit Requested ... We should probably tidy up first");
                System.exit(0);
            }

// Probably here you'd check to see if you were in animation_mode and if so,
// Skip the repaint() and just wait until the next Timer fire causes repaint
            repaint();
        }


        public JMenuBar createMenuBar() {

            String[] colorNames = {"Black", "White", "Red", "Green", "Blue", "Yellow", "Select Color..."};

            ActionListener listener = this;

            JMenuBar menuBar = new JMenuBar();

            JMenu fileMenu    = new JMenu("File");
            JMenu controlMenu = new JMenu("Control");
            JMenu colorMenu   = new JMenu("Color");
            JMenu bgColorMenu = new JMenu("BackgroundColor");
            menuBar.add(fileMenu);
            menuBar.add(controlMenu);
            menuBar.add(colorMenu);
            menuBar.add(bgColorMenu);
            final JCheckBoxMenuItem axesDraw = new JCheckBoxMenuItem("Draw Axes");
            final JCheckBoxMenuItem ticsDraw = new JCheckBoxMenuItem("Draw Tics");
            final JCheckBoxMenuItem gridDraw = new JCheckBoxMenuItem("Draw Grid");
            final JCheckBoxMenuItem seisDraw = new JCheckBoxMenuItem("Draw Seis");
            axesDraw.addActionListener(listener);
            ticsDraw.addActionListener(listener);
            gridDraw.addActionListener(listener);
            seisDraw.addActionListener(listener);

            JMenuItem openText = new JMenuItem("Open (ASCII File)...");
            JMenuItem openSac = new JMenuItem("Open (Sac File)...");
            openSac.addActionListener(listener);
            openText.addActionListener(listener);
            fileMenu.add(openText);
            fileMenu.add(openSac);

            fileMenu.addSeparator();

            JMenuItem quitCommand = new JMenuItem("Quit");
            fileMenu.add(quitCommand);
            //quitCommand.addActionListener(listener);
            quitCommand.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("== Quit Requested ... We should probably tidy up first");
                    System.exit(0);
                }
            });

            JMenuItem clearCommand = new JMenuItem("Clear");
            clearCommand.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    axesDraw.setSelected(false);    // Uncheck the Checkboxes
                    ticsDraw.setSelected(false);
                    gridDraw.setSelected(false);
                    seisDraw.setSelected(false);
                    drawAxes = false;               // And unset the drawing flags
                    drawTics = false;
                    drawGrid = false;
                    drawSeis = false;
                    repaint();
                }
            });
            controlMenu.add(clearCommand);
            controlMenu.add(axesDraw);
            controlMenu.add(ticsDraw);
            controlMenu.add(gridDraw);
            controlMenu.add(seisDraw);

    // Create Color Menu and make sure current line color is selected (=checked)
            ButtonGroup group = new ButtonGroup();
            ActionListener colorListener = new colorListener(group);
            JRadioButtonMenuItem jitem = null;

            for (String colorString : colorNames){
                jitem = new JRadioButtonMenuItem(colorString);
                Color color = getColor(colorString);
                if (color != null) {
                    if (color.equals(DEFAULT_LINE_COLOR)){
                        System.out.format("== ** Matches default currentColor!");
                        jitem.setSelected(true);
                    }
                }
                jitem.addActionListener(colorListener);
                group.add(jitem);
                colorMenu.add(jitem);
            }


    // Create BackgroundColor Menu and make sure current background color is selected (=checked)
            ButtonGroup buttonGroup = new ButtonGroup();
            ActionListener bgColorListener = new bgColorListener(buttonGroup);

            for (String colorString : colorNames){
                jitem = new JRadioButtonMenuItem(colorString);
                if ( matchesBgColor(colorString) ) {
                    jitem.setSelected(true);
                }
                jitem.addActionListener(bgColorListener);
                buttonGroup.add(jitem);
                bgColorMenu.add(jitem);
            }

            return menuBar;

        } // end createMenuBar


        private class colorListener implements ActionListener {
            private ButtonGroup buttonGroup = null;
            public colorListener(ButtonGroup buttonGroup) {
                this.buttonGroup = buttonGroup;
            }
            public void actionPerformed(ActionEvent evt) {
                Color newColor = null;
                String command = evt.getActionCommand();
                if (command.contains("Select Color") ) {
                    newColor = JColorChooser.showDialog(displayPanel, "Select Drawing Color", displayPanel.getBackground());
                    if (newColor == null){
                // If we cancel our color selection, we want the current color
                //   to be selected in the menu (=checked), NOT "Select Color ..."
                        Enumeration<AbstractButton> buttons = buttonGroup.getElements();
                        while (buttons.hasMoreElements()) {
                            AbstractButton button = (AbstractButton)buttons.nextElement();
                            if (matchesBgColor( button.getText()) ){
                                button.setSelected(true);
                            }
                        }
                        return;
                    }
                }
                else {
                    newColor = getColor(command);
                }

                if (newColor != null) {
                    seis.setColor(newColor);
                    repaint();
                }
            } // end actionPerformed
        } // end ActionListener


        private class bgColorListener implements ActionListener {
            private ButtonGroup buttonGroup = null;
            public bgColorListener(ButtonGroup buttonGroup) {
                this.buttonGroup = buttonGroup;
            }
            public void actionPerformed(ActionEvent evt) {
                Color newColor = null;
                String command = evt.getActionCommand();
                if (command.contains("Select Color") ) {
                    newColor = JColorChooser.showDialog(displayPanel, "Select Drawing Color", displayPanel.getBackground());
                    if (newColor == null){
                // If we cancel our color selection, we want the current background color
                //   to be selected in the menu (=checked), NOT "Select Color ..."
                        Enumeration<AbstractButton> buttons = buttonGroup.getElements();
                        while (buttons.hasMoreElements()) {
                            AbstractButton button = (AbstractButton)buttons.nextElement();
                            if (matchesBgColor( button.getText()) ){
                                button.setSelected(true);
                            }
                        }
                        return;
                    }
                }
                else {
                    newColor = getColor(command);
                }

                if (newColor != null) {
                    displayPanel.setBackground(newColor);
                }
            } // end actionPerformed
        } // end ActionListener



        private Color getColor( String colorString) {
            Color color = null;
            try {              // e.g, colorString="Black" --> Color.BLACK
                Field field = Class.forName("java.awt.Color").getField(colorString.toLowerCase());
                color = (Color)field.get(null);
            }
            catch (Exception e) {
                System.out.format("== getColor: Unable to convert colorString=%s\n", colorString);
            }
            return color;
        }

        /** 
        * matchesBgColor
        * If you haven't explicitly set JPanel.setBackground( Color. ...) then the JPanel will inherit the
        * color of the container (=JFrame), however, JPanel.getBackground() will NOT return a Color object!
        * and it WON'T be possible to check for the background color ...
        */
        private Boolean matchesBgColor( String colorString) {
            Color color = getColor(colorString);
            if (color == null) {
                return false;
            }
            if (displayPanel.isBackgroundSet() ){  // BackgroundColor has been set on the JPanel
                if (color.equals( displayPanel.getBackground()) ) {
                    return true;
                }
            }
            else { // JPanel will inherit the background color of its parent --> we must test the parent
                if (color.equals( getBackground()) ) {
                    return true;
                }
            }
            return false;
        } // end matchesBgColor


        public void stateChanged( ChangeEvent evt) {

            if (evt.getSource() == gainSlider){
                int gain = gainSlider.getValue();
                if (gain == 0) { // Don't let gain==0 ---> set gain=1 in this case
                    gain = 1;
                    gainSlider.setValue(1);
                }
                seis.setVerticalGain(gain);
            }
            else if (evt.getSource() == timeSlider){
                int secondsPerDiv = timeSlider.getValue();
                if (secondsPerDiv <= 0) {   // I let the slider go from 0 for aesthetics
                    secondsPerDiv = 1;
                    timeSlider.setValue(1);
                }
                axes.setSecondsPerDiv(secondsPerDiv);
                seis.setSecondsPerDiv(secondsPerDiv);

                //System.out.format("== Change secondsPerDiv=%d\n", secondsPerDiv);
            }
            else if (evt.getSource() == speedSlider){
                if (!speedSlider.getValueIsAdjusting() ) { // Don't change speed until slider stops
                    int delay = speedSlider.getValue(); // Get timer delay (millisecs) from Slider
                    if (delay <= 0) {   // I let the slider go from 0 for aesthetics
                        delay = 1;
                        speedSlider.setValue(1);
                    }
                    //System.out.format("== Change delay=%d\n", delay);
                    timer.setDelay(delay);
                }
            }

            myRepaint();

        } // end stateChanged()


        public void myRepaint() {
        // If the timer is on we don't want to force repaint() - just set changes and wait for the next
        // timer fire to update the panel
            if (!seis.timerOn || seis.timerPause) {
                repaint();
            }
        }


    } // end DisplayPanel


    public class ControlPanel extends JPanel {

        // constructor(s)
        public ControlPanel(){      // Layout the Control Panel

        // Set up the "Stop", "Play", "Pause" buttonPanel
            JPanel buttonPanel = new JPanel();
            buttonPanel.setBackground(panelBackgroundColor);

        // Set up the main display panel to listen for all Action & Change Events
            ActionListener listener       = displayPanel;
            //ActionListener listener       = null;
            ChangeListener changeListener = displayPanel;

            stop = new JButton("Stop");
            stop.addActionListener(listener);
            //JButton pause = new JButton("Pause");
            //pause.addActionListener(listener);
            play = new JButton("Play");
            play.addActionListener(listener);
            play.setEnabled(false);

	        buttonPanel.add(stop);
            //buttonPanel.add(pause);
            buttonPanel.add(play);

        // Set up the "Horiz Scale", "Vert Gain", etc. JSlider Controls
            timeSlider = new JSlider(0, 400, 1);
            timeSlider.setValue(DEFAULT_TIMESCALE);
            timeSlider.setBackground(panelBackgroundColor);
            timeSlider.setMajorTickSpacing(100);
            timeSlider.setMinorTickSpacing(25);
            timeSlider.setPaintTicks(true);
            //timeSlider.setLabelTable(timeSlider.createStandardLabels(25));
            timeSlider.setLabelTable(timeSlider.createStandardLabels(100));
            timeSlider.setPaintLabels(true);
            timeSlider.addChangeListener(changeListener);

            gainSlider = new JSlider(-100,100,1);
            gainSlider.setValue(DEFAULT_VERTGAIN);
            gainSlider.setBackground(panelBackgroundColor);
            gainSlider.setMajorTickSpacing(100);
            gainSlider.setMinorTickSpacing(10);
            gainSlider.setPaintTicks(true);
            gainSlider.setLabelTable(gainSlider.createStandardLabels(100));
            gainSlider.setPaintLabels(true);
            gainSlider.addChangeListener(changeListener);

            speedSlider = new JSlider(0,1000,1);
            speedSlider.setValue(DEFAULT_FRAMEDELAY);
            speedSlider.setMajorTickSpacing(250);
            speedSlider.setMinorTickSpacing(50);
            speedSlider.setLabelTable(speedSlider.createStandardLabels(250));
            speedSlider.setBackground(panelBackgroundColor);
            speedSlider.setPaintTicks(true);
            speedSlider.setPaintLabels(true);
            speedSlider.addChangeListener(changeListener);

        // Layout this Control Panel and add the various controls above
            setLayout( new GridLayout(2, 4, 50, 0));

            add( new JLabel("Controls", JLabel.CENTER) );
            add( new JLabel("Horiz Scale  [Secs/Div]", JLabel.CENTER) );
            add( new JLabel("Vert Gain", JLabel.CENTER) );
            add( new JLabel("Frame Delay [millisecs]", JLabel.CENTER) ); 
            //add( new JLabel("Frame Speed [1=dT --> 100=dT/100]", JLabel.CENTER) ); 
            add(buttonPanel);
            add(timeSlider);
            add(gainSlider);
            add(speedSlider);

        }
    } // end class ControlPanel



    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        // Create and set up the window.
        JFrame frame = new SeismicDrum();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Display the window.
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);
    }

    public static void main(String[] args) {
        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        // Note that a new anonymous inner-class that extends Runnable is declared below
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
