package mth.seisdrum;

import java.awt.Color;
import java.awt.geom.Line2D;
import java.awt.Graphics;
import java.awt.Graphics2D;
/**
 * Axes is a nested class within DisplayPanel and is responsible for correctly drawing
 * the axes on the screen using the current settings
 */
    public class Axes {
        private int Xlen; // Length (pixels) of x-axis
        private int Ylen; // Length (pixels) of y-axis
        private int numberOfDivs;  // Number of x-axis tics to draw - set in DisplayPanel
        private int secondsPerDiv; // User-selected via JSlider

        public Axes( int Xlen, int Ylen) {  // constructor
            this.Xlen = Xlen;
            this.Ylen = Ylen;
        }
        public void setNumberOfDivs ( int numberOfDivs) {
            this.numberOfDivs = numberOfDivs;
        }
        public void setSecondsPerDiv ( int secondsPerDiv) {
            this.secondsPerDiv = secondsPerDiv;
        }


        void draw(Graphics g){
            Graphics2D g2 = (Graphics2D)g.create();
            g2.drawLine(0,0,Xlen,0);         // Draw x-axis
            g2.drawLine(0,-Ylen/2,0,Ylen/2); // Draw y-axis
            g2.dispose();
        } // end Axes.draw()

        void drawTics(Graphics g){
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setColor(Color.WHITE);
            double xticMajor = (double)Xlen/(double)numberOfDivs;
            double xticMinor = xticMajor/10.;
            double ticSecs   = (double)secondsPerDiv;
            double xx = xticMajor;
        // Draw Major tic marks and tic labels
        // We're working in pixels here, the -2 is to keep the final label from ever plotting
            while (xx < Xlen-2) { 
                Line2D line = new Line2D.Double(xx, -10.0, xx, 10.0);
                g2.draw(line);
                String ticString = Double.toString(ticSecs);
                g2.drawString(""+ticString,(int)xx-10,25);
                xx += xticMajor;
                ticSecs += secondsPerDiv;
            }
        // Draw Minor tic marks
            xx = xticMinor;
            while (xx < Xlen-2){ // We're working in pixels here
                Line2D line = new Line2D.Double(xx, -5.0, xx, 5.0);
                g2.draw(line);
                xx += xticMinor;
            }
            g2.dispose();
        } // end drawTics

        void drawGrid(Graphics g){
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setColor(Color.RED);

            final int NUMBER_OF_XBOXES = 20;
            final int NUMBER_OF_YBOXES = 20;

            double dx = Xlen/(double)NUMBER_OF_XBOXES;
            double dy = Ylen/(double)NUMBER_OF_YBOXES;

            double x = 0. ;
	        while (x <= Xlen){
		        g2.draw( new Line2D.Double(x, -Ylen/2, x, Ylen/2) );
	  	        x += dx;
	        }
	        double y = -(double)Ylen/2;
	        while (y <= Ylen/2){
		         g2.draw( new Line2D.Double(0, y, Xlen, y) );
	  	         y += dy;
	        }
            g2.dispose();

        } // end drawGrid()

    } // end Axes
