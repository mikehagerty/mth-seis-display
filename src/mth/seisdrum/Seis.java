package mth.seisdrum;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Line2D;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Component;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import edu.sc.seis.seisFile.sac.SacTimeSeries;

    public class Seis {
        public int iframe; // Really the frame count
        public int Npts;
        public double dT;
        public double y[];
        public boolean timerOn, timerPause;
        public String sourceString = null; 
        private Color lineColor = null;

        private int Xlen; // Length (pixels) of x-axis
        private int Ylen; // Length (pixels) of y-axis
        private int numberOfDivs; // Number of x-axis tics to draw
        private int secondsPerDiv; // User-selected via JSlider
        private int verticalGain;  // User-selected via JSlider


        public Seis( int Xlen, int Ylen) {  // constructor
            this.Xlen = Xlen;
            this.Ylen = Ylen;
            iframe=1;
            this.lineColor = Color.BLACK; // Default
        }
        public void setNumberOfDivs ( int numberOfDivs) {
            this.numberOfDivs = numberOfDivs;
        }
        public void setSecondsPerDiv ( int secondsPerDiv) {
            this.secondsPerDiv = secondsPerDiv;
        }
        public void setVerticalGain ( int verticalGain) {
            this.verticalGain = verticalGain;
        }
        public void setColor ( Color lineColor) {
            this.lineColor = lineColor;
        }

        public void draw(Graphics g) { 
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setColor( lineColor );
            double secondsToPlot = (double)secondsPerDiv * numberOfDivs;
            int pointsToPlot = (int)(secondsToPlot / dT) + 1;
            double xscale = (double)Xlen/(double)(pointsToPlot-1);
            double yscale = (double)Ylen/2.0;
            //double yscale = height - YOFF - yctr; // max value in pixels
            // The trace y[] should always be normalized to max amp = 1
            // The panel drawing area has ymax pixels = Ylen/2 (from yctr to ymax)

            if (verticalGain > 0){
                yscale *= (double)verticalGain;
            }
            else { 
                yscale /= (double)Math.abs(verticalGain);
            }

            //System.out.format("== Seis: secondsToPlot=%f Xlen=%d Ylen=%d xscale=%f yscale=%f verticalGain=%d\n", secondsToPlot,
                                //Xlen, Ylen, xscale, yscale, verticalGain);

            // Default behavior is to plot the entire trace at once:
            boolean staticDisplay = true;

            if (timerOn || timerPause) { 
                staticDisplay = false;
            }
            else { 
                staticDisplay = true;
            }

            if (staticDisplay){         // We are in static display
                int istart = 1;
                int iend   = pointsToPlot;
                if (pointsToPlot > Npts) { 
                    iend   = Npts;      // Make sure we don't exceed array bounds
                }

             // Loop over points and plot
                for (int i = istart; i < iend; i++) {
                // Draw a line segment from point number i-1 to point number i.
                    double x1 = xscale * (i-1);
                    double x2 = xscale * (i) ;
                    //double y1 = (yscale * y[i-1]) + yctr;
                    //double y2 = (yscale * y[i])   + yctr;
                    double y1 = (yscale * y[i-1]);
                    double y2 = (yscale * y[i]);
                    g2.draw( new Line2D.Double(x1,y1,x2,y2) );
                } // end for points
            } // end staticDisplay

            else {                      // We are in animation mode
                //istart = iframe; iend = iframe+1;
                //g2.drawString("< " + iframe + " >", 100, 20);
	            int nLineSegs = iframe;
	            double xPen = (double)Xlen;
                g2.setStroke( new BasicStroke(1.0F) );
                double x1=0,x2=0,y1=0,y2=0;
                if (iframe >= pointsToPlot) {
                    nLineSegs = pointsToPlot - 1;
                }
                for (int iLineSeg = 1; iLineSeg <= nLineSegs; iLineSeg++) {
                  // Draw a line segment from point number i-1 to point number i.
                    x1 = xPen - (xscale * (iLineSeg-1)) ;
                    x2 = xPen - (xscale * (iLineSeg  )) ;
                    int index = iframe - (iLineSeg-1);
                    if (index >= Npts){ // Make sure we don' exceed array bounds
                        y1 = 0;        // Continue drawing a flat line to run the trace off to the left
                        y2 = 0;
                    }
                    else {
                        y1 = yscale * y[ iframe - (iLineSeg-1) ] ;
                        y2 = yscale * y[ iframe - (iLineSeg) ] ;
                    }
                    g2.draw( new Line2D.Double(x1,y1,x2,y2) );
                } // end Loop over LineSegs

            // Draw Pen - if we've run out of points to plot then put the pen along the x-axis
                double yPen;
                if (iframe >= Npts) { 
                    yPen = 0;
                }
                else {
                    yPen = yscale * y[ iframe ] ;
                }
                    g2.setColor( Color.BLACK );
double xBase = xPen + 100;
double yBase = 0;
                    g2.draw( new Line2D.Double(xBase,yBase,xPen,yPen) );
                    //g2.draw( new Line2D.Double(width,yctr,xPen,0) );

            } // else animation mode

            g2.dispose();
        } // end Seis.draw()



        public void readSacFile( Component parent, JFileChooser fileDialog) {
            if (fileDialog == null)
                fileDialog = new JFileChooser( System.getProperty("user.dir") );
            fileDialog.setDialogTitle("Select File to be Opened");
            fileDialog.setSelectedFile( new File("bks.sac") );
            //fileDialog.setSelectedFile(null);  // No file is initially selected.
            int option = fileDialog.showOpenDialog(parent);
            if (option != JFileChooser.APPROVE_OPTION)
                return;  // User canceled or clicked the dialog's close box.
            File selectedFile = fileDialog.getSelectedFile();

            SacTimeSeries sac = new SacTimeSeries();
            try {
                sac.read(selectedFile);
            }
            catch (Exception e) {
                JOptionPane.showMessageDialog(parent,"Sorry, but an error occurred while trying to open the SAC file:" 
                                              + selectedFile.toString() + "\n" + e);
                return;
            }

            //sac.printHeader();

        // Loop through the sac file and read in the points

            float mean = 0.F;
            double ymax = 0.;

    // ** MTH: Check SacTimeSeries to see if mean is already a header var
        // Remove the mean and scale to ymax = 1
            for (int i = 0; i < sac.npts; i++) {
                mean += sac.y[i];
            }
            mean /= (float)sac.npts;
            for (int i = 0; i < sac.npts; i++) {
                sac.y[i] -= mean;
                if (Math.abs(sac.y[i]) > ymax) ymax = Math.abs(sac.y[i]);
            }
// Check that ymax > 0 ...

// Also, need to handle creation of seis.y[] array better ...
            this.y = new double[sac.npts];

            for (int i = 0; i < sac.npts; i++) {
                this.y[i] = (double)(sac.y[i])/ymax;
            }
            this.Npts = sac.npts;
            this.dT   = sac.delta;
            this.iframe = 1; // Reset

            this.sourceString = selectedFile.getName();

        }//end readSacFile


     } // end class Seis

