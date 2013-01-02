/* 
   Ver. 12 - This is a good, working version.  The only thing is that it still has slow animation
               because it repaints the entire JPanel each frame -- e.g., every 1/40 = 25 millisecs
               it has to redraw the entire 30 min x 60 sec x 40 samples/sec = 72,000 points.

             For the next version I'll try just repainting the used pixels each time, not the whole background
*/

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

import java.text.DecimalFormat;

import java.awt.geom.*;

import java.awt.image.BufferedImage;

//import edu.sc.seis.seisFile;

public class SimplePaintWithFiles extends JFrame {
   
private JSlider gainSlider;
private JSlider timeSlider;
private JSlider speedSlider;
private JButton play;
private JButton stop;

   public static void main(String[] args) {
      JFrame window = new SimplePaintWithFiles();
      window.pack();
      window.setLocation(50,100);  
      window.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
      window.setVisible(true);
   }
      
   public SimplePaintWithFiles() {
      super("File Read: No File");
      setBackground( Color.BLUE);
      setLayout( new BorderLayout());
      SimplePaintPanel content = new SimplePaintPanel();
      setJMenuBar(content.createMenuBar());
      content.setCursor( Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR) );
      JScrollPane scroller = new JScrollPane(content);
      scroller.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED );
      scroller.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS );
      //add(scroller,     BorderLayout.CENTER);
      add(content,     BorderLayout.CENTER);

      JPanel buttonPanel = new JPanel();
      add(buttonPanel, BorderLayout.SOUTH);
      Color panelBackground = new Color(220,200,180);
      buttonPanel.setLayout( new GridLayout(2,4,50,0));
      buttonPanel.setBackground(panelBackground);

      JPanel controlPanel = new JPanel();
      controlPanel.setBackground(panelBackground);
      stop = new JButton("Stop");
      stop.addActionListener(content);
      JButton pause = new JButton("Pause");
      pause.addActionListener(content);
    //JButton play = new JButton("Play");
      play = new JButton("Play");
      play.addActionListener(content);
      play.setEnabled(false);

	controlPanel.add(stop);
    //controlPanel.add(pause);
      controlPanel.add(play);

      timeSlider = new JSlider(0,400,1);
      timeSlider.setValue(120);
      timeSlider.setBackground(panelBackground);
      timeSlider.setMajorTickSpacing(100);
      timeSlider.setMinorTickSpacing(25);
      timeSlider.setPaintTicks(true);
      //timeSlider.setLabelTable(timeSlider.createStandardLabels(25));
      timeSlider.setLabelTable(timeSlider.createStandardLabels(100));
      timeSlider.setPaintLabels(true);
      timeSlider.addChangeListener(content);

      gainSlider = new JSlider(-100,100,1);
      gainSlider.setValue(0);
      gainSlider.setBackground(panelBackground);
      gainSlider.setMajorTickSpacing(100);
      gainSlider.setMinorTickSpacing(10);
      gainSlider.setPaintTicks(true);
      gainSlider.setLabelTable(gainSlider.createStandardLabels(100));
      gainSlider.setPaintLabels(true);
      gainSlider.addChangeListener(content);
/*
      speedSlider = new JSlider(0,100,1);
      speedSlider.setMajorTickSpacing(25);
      speedSlider.setMinorTickSpacing(5);
      speedSlider.setLabelTable(speedSlider.createStandardLabels(25));
*/
      speedSlider = new JSlider(0,1000,1);
      speedSlider.setMajorTickSpacing(250);
      speedSlider.setMinorTickSpacing(50);
      speedSlider.setLabelTable(speedSlider.createStandardLabels(250));

      speedSlider.setValue(1);
      speedSlider.setBackground(panelBackground);
      speedSlider.setPaintTicks(true);
      speedSlider.setPaintLabels(true);
      speedSlider.addChangeListener(content);


      buttonPanel.add( new JLabel("Controls", JLabel.CENTER) );
      buttonPanel.add( new JLabel("Horiz Scale  [Secs/Div]", JLabel.CENTER) );
      buttonPanel.add( new JLabel("Vert Gain", JLabel.CENTER) );
      buttonPanel.add( new JLabel("Frame Delay [millisecs]", JLabel.CENTER) ); 
      //buttonPanel.add( new JLabel("Frame Speed [1=dT --> 100=dT/100]", JLabel.CENTER) ); 
      buttonPanel.add(controlPanel);
      buttonPanel.add(timeSlider);
      buttonPanel.add(gainSlider);
      buttonPanel.add(speedSlider);
   }

   private static class CurveData implements Serializable {
      Color color;  // The color of the curve.
      boolean symmetric;  // Are horizontal and vertical reflections also drawn?
      ArrayList<Point2D.Double> points;  // The points on the curve.
   }
   
   private class SimplePaintPanel extends JPanel implements ActionListener, ChangeListener {

      private ArrayList<CurveData> curves;  // A list of all curves in the picture.

      private Color currentColor;   // When a curve is created, its color is taken
      private boolean useSymmetry;  // When a curve is created, its "symmetric"
      private File editFile;        // The file that is being edited, if any.
      private JFileChooser fileDialog;   // The dialog box for all open/save commands.

      private int width, height;
      private Axes axes;
      private Seis seis;
      private int xoff, xend, yoff, yctr;
      private boolean drawAxes;
      private boolean drawSeis;

      private Timer timer;

      private int gain, delay;

      private boolean timerpause;
      private boolean file_is_read;

      int mouseX, mouseY;

      private int Xlen, Ylen;
      private int numberOfDivs;
      private double secondsToPlot;
      private double secondsPerDiv;
/* Here's how I see the horizontal scale working, mapping time to x-pixels:
   (N-1)dT = secondsToPlot = (secondsPerDiv x numberOfDivs)   =  Xlen = (N-1)dX
   where N may be > or < seis.Npts
*/

      public SimplePaintPanel() {
         curves = new ArrayList<CurveData>();
         currentColor = Color.BLACK;
         setBackground(Color.GREEN);
         setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));

         MouseHandler listener = new MouseHandler();
         addMouseListener(listener);
         addMouseMotionListener(listener);

         setPreferredSize( new Dimension(1200,450) );
	 delay = 1; // Set the default delay


// MTH: add timer to JPanel:
      ActionListener action = new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
            repaint();
         }
      };

      timer = new Timer( delay, action );  // Fires every 30 milliseconds.
	timer.setCoalesce(true); // This will coalesce pending firings - I'm not sure what it will do

      } // end SimplePaintPanel() Constructor


// MTH: does this conflict with the timer actionlistener above ?
      public void actionPerformed(ActionEvent evt) {
	String command = evt.getActionCommand();
	if (command.equals("Play")) {
          drawSeis = true; // This is only needed so that it redraws afterwards when resizing;
          timerpause = false;
          if (file_is_read) {
	      timer.start();  // Only start timer if data is in memory
	      play.setText("Pause"); // Change button from Play to Pause
	    }
        }
	else if (command.equals("Stop")) {
          timer.stop();
          drawSeis = false;
          seis.iframe = 1; // Reset
          repaint();
	    play.setText("Play"); // Change button from Pause to Play
	}
	else if (command.equals("Pause")) {
          timerpause = true;
          if (timer.isRunning()){
            timer.stop();
          } // if the timer isn't running then don't do anything
	    play.setText("Play"); // Change button from Pause to Play
	}
      } // end actionPerformed


// MTH: Implement ChangeListener to listen to JSlider(s):
      public void stateChanged(ChangeEvent evt) {
	  if (evt.getSource() == gainSlider){
	    gain = gainSlider.getValue(); 
	    repaint();
	  }
	  else if (evt.getSource() == timeSlider){
	    secondsPerDiv = timeSlider.getValue(); 
	    if (secondsPerDiv <= 0) secondsPerDiv=1; // I let the slider go from 0 for aesthetics
	    if (timer.isRunning()){ // Timer is running and we've changed the Secs/Div scale
            timer.stop();
//MTH
	      timerpause = true;
	      play.setText("Play"); // Change button from Pause to Play
	      play.doClick();
	    }
	      repaint();
	  }
	  else if (evt.getSource() == speedSlider){
	    if (!speedSlider.getValueIsAdjusting() ) { // Don't change speed until slider stops
	      delay = speedSlider.getValue(); // Get timer delay (millisecs) from Slider 
	      if (delay <= 0) delay = 1;  // Let slider go to 0 for aesthetics
              setTitle("delta=" + seis.dT + "  delay="+delay);
	      timer.setDelay(delay);
	      repaint();
	    }
	  }
      } // end stateChanged


      private class MouseHandler implements MouseListener, MouseMotionListener {
         boolean dragging;
         public void mousePressed(MouseEvent evt) {
            if (dragging)
               return;
            dragging = true;
            //currentCurve.points.add( new Point(evt.getX(), evt.getY()) );
            //currentCurve.points.add( new Point2D.Double(evt.getX(), evt.getY()) );
         }
         public void mouseDragged(MouseEvent evt) {
            if (!dragging)
               return;
          //currentCurve.points.add( new Point(evt.getX(),evt.getY()) );
          // currentCurve.points.add( new Point2D.Double(evt.getX(),evt.getY()) );
          //  repaint();  // redraw panel with newly added point.
         }
         public void mouseReleased(MouseEvent evt) {
            if (!dragging)
               return;
            dragging = false;
         }
         public void mouseClicked(MouseEvent evt) { }
         public void mouseEntered(MouseEvent evt) { } 
         public void mouseExited(MouseEvent evt) { }
         public void mouseMoved(MouseEvent evt) { 
	   mouseX = evt.getX();
	   mouseY = evt.getY();
           repaint();
         }
      } // end nested class MouseHandler


      /**
       * Fills the panel with the current background color and draws all the
       * curves that have been sketched by the user.
       */
      public void paintComponent(Graphics g) {

	xoff   = 10;
	xend   = 120;
        yoff   = 40;
        height = getHeight();
        width  = getWidth();
        yctr   = height/2;
	Xlen   = width - xoff - xend; // Total Width of the drawing panel in pixels  = x-axis length
	Ylen   = height -yoff - yoff; // Total Height of the drawing panel in pixels = y-axis length

	numberOfDivs = 12;
        secondsToPlot = secondsPerDiv * numberOfDivs;

	Graphics2D g2 = (Graphics2D)g;

	if (axes == null) axes = new Axes();
	if (seis == null) seis = new Seis();

	seis.timerOn = timer.isRunning();

          super.paintComponent(g);
	  if (drawAxes){
            axes.draw(g2);
          }
          g.drawString("< " + seis.iframe + " >", 100, yctr -50);
	  if (drawSeis){
            seis.draw(g2);
          }

	 if (seis.timerOn) seis.iframe++;

      } // end paintComponent()


      private class Axes {
        boolean drawTics;
        boolean drawGrid;

        Axes() { // Constructor
          secondsPerDiv = timeSlider.getValue();
        }
       
	  void draw(Graphics2D g) { // We are really passing in a Graphics2D object, g

        //MTH: we are still working in units of pixels, but I've shifted the origin to <xoff,yctr=height/2>
        // First Draw the axes:
            g.translate(xoff,yctr);
	    int ymax = yctr - yoff; // The y-axis now runs from +-ymax, where -ymax is the top and +ymax if the bottom
            g.drawLine(0,0,Xlen,0);     // Draw x-axis
            g.drawLine(0,-ymax,0,ymax); // Draw y-axis
	    double xticMajor = (double)Xlen/numberOfDivs;
	    double xticMinor = xticMajor/10.;
            double ticSecs = secondsPerDiv;
	    double xx,yy;

	  //We've translated the axes for plotting purposes, but not for calculations since mouseX is in orig coords
	    double timeX = (secondsToPlot * (double)(mouseX-xoff) ) / (double)Xlen;
	    String fmt = "0.00";
	    //String fmt = "0.##";
	    DecimalFormat df = new DecimalFormat(fmt);
	    String timestr = df.format(timeX);
         //   g.drawString("<" + timestr + ">", -xoff +10, yctr -10);
            //g.drawString("<" + mouseX + "," + mouseY + ">", 200,160);


          if (drawTics){
            g.setColor(Color.WHITE);
           // Draw Major tic marks and tic labels
            xx = xticMajor;
	      while (xx < Xlen-2){ // We're working in pixels here, the -2 is to keep the final label from ever plotting
		  Line2D line = new Line2D.Double(xx, -10.0, xx, 10.0);
		  g.draw(line);
              String ticString = Double.toString(ticSecs);
              g.drawString(""+ticString,(int)xx-10,25);
	  	  xx += xticMajor;
              ticSecs += secondsPerDiv;
	      }
           // Draw Minor tic marks
            xx = xticMinor;
	      while (xx < Xlen-2){ // We're working in pixels here
		  Line2D line = new Line2D.Double(xx, -5.0, xx, 5.0);
		  g.draw(line);
	  	  xx += xticMinor;
	      }
          } // end if

          if (drawGrid){ // Draw Grid
            g.setColor(Color.RED);
            xx = Xlen/20. ;
            yy = Ylen/10. ;
            double x = 0. ;
	      while (x <= Xlen){
		  g.draw( new Line2D.Double(x, -ymax, x, ymax) );
	  	  x += xx;
	      }
	      double y = -(double)ymax;
	      while (y <= ymax){
		  g.draw( new Line2D.Double(0, y, Xlen, y) );
	  	  y += yy;
	      }

          } // end if
         g.setColor(Color.BLACK);
         g.translate(-xoff,-yctr); // Translate coords back
        } // end draw Axes
      } // end nested class Axes



      private class Seis {
	int iframe; // Really the frame count
	int Npts;
	double dT;
	boolean timerOn;
        Seis() { // Constructor
	  iframe=1;
        }

	void draw(Graphics2D g) { // We are really passing in a Graphics2D object, g
          int pointsToPlot = (int)(secondsToPlot / dT) + 1;
          double dX = (double)Xlen/(double)(pointsToPlot-1);
          double xscale=dX;
          double yscale = height - yoff - yctr; // max value in pixels
          g.translate(xoff,0);

	  if (gain != 0){
	    if (gain < 0){
	       yscale /= (double)(-gain);
	    }
	    else{
  	       yscale *= (double)gain;
	    }
	  }

// Default behavior is to plot the entire trace at once:
	 int istart = 1;
	 int iend   = Npts;
	 iend   = pointsToPlot;
	 if (pointsToPlot > Npts) iend   = Npts; // Make sure we don't exceed array bounds

	 if (timerOn || timerpause){
	   istart = iframe; iend = iframe+1;
         }

	 if (!timerOn && !timerpause){
         for ( CurveData curve : curves) {
            g.setColor(curve.color);
            for (int i = istart; i < iend; i++) {
            // Draw a line segment from point number i-1 to point number i.
               double x1 = xscale * (i-1);
               double x2 = xscale * (i) ;
               double y1 = (yscale * curve.points.get(i-1).y) + yctr;
               double y2 = (yscale * curve.points.get(i).y)   + yctr;
               g.draw( new Line2D.Double(x1,y1,x2,y2) );
	    } // end for points
	 } // end for curves
        } //if

	else{
	 int nLineSegs = iframe;
	 double xPen = (double)Xlen;

	 //g.setStroke( new BasicStroke(1.0F) );
	 double x1,x2,y1,y2=0;
	 if (iframe >= pointsToPlot) nLineSegs = pointsToPlot - 1;

         for ( CurveData curve : curves) {
            g.setColor(curve.color);
            for (int iLineSeg = 1; iLineSeg <= nLineSegs; iLineSeg++) {
            // Draw a line segment from point number i-1 to point number i.
               x1 = xPen - (xscale * (iLineSeg-1)) ;
               x2 = xPen - (xscale * (iLineSeg  )) ;
	       int index = iframe - (iLineSeg-1);
	       if (index >= Npts){ // Make sure we don' exceed array bounds
                 y1 = yctr;        // Continue drawing a flat line to run the trace off to the left
                 y2 = yctr;
               }
               else {
                 y1 = (yscale * curve.points.get( iframe - (iLineSeg-1) ).y) + yctr;
                 y2 = (yscale * curve.points.get( iframe - (iLineSeg  ) ).y) + yctr;
               }
               g.draw( new Line2D.Double(x1,y1,x2,y2) );
	    } // end for points
         // Draw Pen - if we've run out of points to plot then put the pen along the x-axis
              double yPen;
	    if (iframe >= Npts) { 
              yPen = yctr;
            }
	    else {
              yPen = (yscale * curve.points.get( iframe ).y) + yctr;
            }
            g.setColor(Color.BLACK);
            g.draw( new Line2D.Double(width,yctr,xPen,yPen) );
	 } // end for curves
	} // else

        g.translate(-xoff,0);
        } // end Seis.draw()
       } // end nested class Seis



      /**
       * Creates a menu bar for use with this panel.  It contains
       * four menus: "File, "Control", "Color", and "BackgroundColor".
       */
      public JMenuBar createMenuBar() {

         /* Create the menu bar object */

         JMenuBar menuBar = new JMenuBar();

         /* Create the menu bar and add them to the menu bar. */

         JMenu fileMenu = new JMenu("File");
         JMenu controlMenu = new JMenu("Control");
         JMenu colorMenu = new JMenu("Color");
         JMenu bgColorMenu = new JMenu("BackgroundColor");
         menuBar.add(fileMenu);
         menuBar.add(controlMenu);
         menuBar.add(colorMenu);
         menuBar.add(bgColorMenu);
         
         /* Add commands to the "File" menu.  It contains two sets
          * of Open and Save commands, one for data saved in object
          * form and one for data saved in text form.  It also contains
          * a command for saving the user's picture as a PNG file and
          * a command for quitting the program.
          */
         
         //Removed the following menu items:
         //JMenuItem newCommand = new JMenuItem("New");
         //JMenuItem saveText = new JMenuItem("Save (text format)...");
         //JMenuItem saveObject = new JMenuItem("Save (binary format)...");
         //JMenuItem openBinary = new JMenuItem("Open (binary format)...");
         //JMenuItem saveImage = new JMenuItem("Save Image...");

         JMenuItem openText = new JMenuItem("Open (ASCII File)...");
         fileMenu.add(openText);
         openText.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
               doOpenAsText();
            }
         });
         JMenuItem openSac = new JMenuItem("Open (Sac File)...");
         fileMenu.add(openSac);
         openSac.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
               doOpenSacFile();
            }
         });

         fileMenu.addSeparator();
         JMenuItem quitCommand = new JMenuItem("Quit");
         fileMenu.add(quitCommand);
         quitCommand.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
               System.exit(0);
            }
         });
         

         /* Add commands to the "Control" menu.  It contains an Undo
          * command that will remove the most recently drawn curve
          * from the list of curves; a "Clear" command that removes
          * all the curves that have been drawn; and a "Use Symmetry"
          * checkbox that determines whether symmetry should be used.
          */

         JMenuItem undo = new JMenuItem("Undo");
         controlMenu.add(undo);
         undo.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
               if (curves.size() > 0) {
                  curves.remove( curves.size() - 1);
                  repaint();  // Redraw without the curve that has been removed.
               }
            }
         });
         JMenuItem clear = new JMenuItem("Clear");
         controlMenu.add(clear);
         clear.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
               //curves = new ArrayList<CurveData>();
               drawAxes = false;
               drawSeis = false;
               seis.iframe = 1; // Reset animation back to beginning in case we interrupted it
               repaint();  // Redraw with no curves shown.
            }
         });
         JCheckBoxMenuItem axe = new JCheckBoxMenuItem("Draw Axes");
         controlMenu.add(axe);
         axe.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
               drawAxes  = ((JCheckBoxMenuItem)evt.getSource()).isSelected();
               repaint();
            }
         });
         JCheckBoxMenuItem tics = new JCheckBoxMenuItem("Draw Tics");
         controlMenu.add(tics);
         tics.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
               axes.drawTics  = ((JCheckBoxMenuItem)evt.getSource()).isSelected();
               //drawAxes = true;
               repaint();
            }
         });
         JCheckBoxMenuItem grid = new JCheckBoxMenuItem("Draw Grid");
         controlMenu.add(grid);
         grid.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
               axes.drawGrid  = ((JCheckBoxMenuItem)evt.getSource()).isSelected();
               //axes.drawTics = true;
               repaint();
            }
         });


         //JCheckBoxMenuItem seisdraw = new JCheckBoxMenuItem("Draw Seis");
         JMenuItem seisdraw = new JMenuItem("Draw Seis");
         controlMenu.add(seisdraw);
         seisdraw.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
               //drawSeis  = ((JCheckBoxMenuItem)evt.getSource()).isSelected();
               drawSeis  = true;
               timerpause = false; // Remove pause if on
               repaint();
            }
         });


         /**
          * Add commands to the "Color" menu.  The menu contains commands for
          * setting the current drawing color.  When the user chooses one of these

         /**
          * Add commands to the "Color" menu.  The menu contains commands for
          * setting the current drawing color.  When the user chooses one of these
          * commands, it has not immediate effect on the drawing.  It justs sets
          * the color that will be used for future drawing.
          */

         colorMenu.add(makeColorMenuItem("Black", Color.BLACK));
         colorMenu.add(makeColorMenuItem("White", Color.WHITE));
         colorMenu.add(makeColorMenuItem("Red", Color.RED));
         colorMenu.add(makeColorMenuItem("Green", Color.GREEN));
         colorMenu.add(makeColorMenuItem("Blue", Color.BLUE));
         colorMenu.add(makeColorMenuItem("Cyan", Color.CYAN));
         colorMenu.add(makeColorMenuItem("Magenta", Color.MAGENTA));
         colorMenu.add(makeColorMenuItem("Yellow", Color.YELLOW));
         JMenuItem customColor = new JMenuItem("Custom...");
         colorMenu.add(customColor);
         customColor.addActionListener( new ActionListener() { 
            // The "Custom..." color command lets the user select the current
            // drawing color using a JColorChoice dialog.
            public void actionPerformed(ActionEvent evt) {
               Color c = JColorChooser.showDialog(SimplePaintPanel.this,
                     "Select Drawing Color", currentColor);
               if (c != null)
                  currentColor = c;
            }
         });

         /**
          * Add commands to the "BackgourndColor" menu.  The menu contains commands
          * for setting the background color of the panel.  When the user chooses
          * one of these commands, the panel is immediately redrawn with the new
          * background color.  Any curves that have been drawn are still there.
          */

         bgColorMenu.add(makeBgColorMenuItem("Black", Color.BLACK));
         bgColorMenu.add(makeBgColorMenuItem("White", Color.WHITE));
         bgColorMenu.add(makeBgColorMenuItem("Red", Color.RED));
         bgColorMenu.add(makeBgColorMenuItem("Green", Color.GREEN));
         bgColorMenu.add(makeBgColorMenuItem("Blue", Color.BLUE));
         bgColorMenu.add(makeBgColorMenuItem("Cyan", Color.CYAN));
         bgColorMenu.add(makeBgColorMenuItem("Magenta", Color.MAGENTA));
         bgColorMenu.add(makeBgColorMenuItem("Yellow", Color.YELLOW));
         JMenuItem customBgColor = new JMenuItem("Custom...");
         bgColorMenu.add(customBgColor);
         customBgColor.addActionListener( new ActionListener() { 
            public void actionPerformed(ActionEvent evt) {
               Color c = JColorChooser.showDialog(SimplePaintPanel.this,
                     "Select Background Color", getBackground());
               if (c != null)
                  setBackground(c);
            }
         });

         /* Return the menu bar that has been constructed. */

         return menuBar;

      } // end createMenuBar


      /**
       * This utility method is used to create a JMenuItem that sets the
       * current drawing color.
       * @param command  the text that will appear in the menu
       * @param color  the drawing color that is selected by this command.  (Note that
       *    this parameter is "final" for a technical reason: This is a requirement for
       *    a local variable that is used in an anonymous inner class.)
       * @return  the JMenuItem that has been created.
       */
      private JMenuItem makeBgColorMenuItem(String command, final Color color) {
         JMenuItem item = new JMenuItem(command);
         item.addActionListener( new ActionListener()  {
            public void actionPerformed(ActionEvent evt) {
               setBackground(color);
            }
         });
         return item;
      }


      /**
       * This utility method is used to create a JMenuItem that sets the
       * background color of the panel.
       * @param command  the text that will appear in the menu
       * @param color  the background color that is selected by this command.
       * @return  the JMenuItem that has been created.
       */
      private JMenuItem makeColorMenuItem(String command, final Color color) {
         JMenuItem item = new JMenuItem(command);
         item.addActionListener( new ActionListener()  {
            public void actionPerformed(ActionEvent evt) {
               currentColor = color;
       // MTH: this is a hack to change the color of seis().
               for (CurveData curve : curves) {
                 curve.color = currentColor;
               }
               repaint();
            }
         });
         return item;
      }
      
      private void doOpenSacFile() {
         if (fileDialog == null)
            fileDialog = new JFileChooser();
         fileDialog.setDialogTitle("Select File to be Opened");
         fileDialog.setSelectedFile(null);  // No file is initially selected.
         int option = fileDialog.showOpenDialog(this);
         if (option != JFileChooser.APPROVE_OPTION)
            return;  // User canceled or clicked the dialog's close box.
         File selectedFile = fileDialog.getSelectedFile();

	SacTimeSeries sac = new SacTimeSeries();
	try {
	  sac.read(selectedFile);
	}
        catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                  "Sorry, but an error occurred while trying to open the SAC file:" + selectedFile.toString() + "\n" + e);
            return;
        }
System.out.println("sac npts = " + sac.npts);
System.out.println("sac delta = " + sac.delta);
System.out.println("sac depmax = " + sac.depmax);
System.out.println("sac[0] = " + sac.y[0]);
sac.printHeader();

         try {
            ArrayList<CurveData> newCurves = new ArrayList<CurveData>();
            CurveData curve = new CurveData();
            curve.color = Color.BLACK;
            curve.color = Color.BLUE;
            curve.symmetric = false;
            curve.points = new ArrayList<Point2D.Double>();
            int x = 0;
            double yy;
            float mean = 0.F;
            double ymax = 0.;
	    file_is_read = false; // flag to see if data is read in

// Loop through, compute the mean and remove, compute the ymax and scale:
            for (int i=0; i < sac.npts; i++) {
	       mean += sac.y[i];
            }
            mean /= (float)sac.npts;
            for (int i=0; i < sac.npts; i++) {
	       sac.y[i] -= mean;
               if (sac.y[i] > ymax) ymax = sac.y[i];
            }
            for (int i=0; i < sac.npts; i++) {
	       yy = (double)(sac.y[i])/ymax;
               curve.points.add( new Point2D.Double(0,yy ) ); 
	    }
            seis.Npts = sac.npts;
            seis.dT   = sac.delta;
	    seis.iframe = 1; // Reset
	    stop.doClick();
	    repaint();

            play.setEnabled(true);
            newCurves.add(curve);
            curves = newCurves;
            //editFile = selectedFile;
            //setTitle("File Read: " + editFile.getName());
            file_is_read = true; 
/* delay only controls the refresh rate, not the time divisions. Set default = sac.delta */
            setTitle("SAC File Read: " + selectedFile.getName() + "delta="+sac.delta);
            delay = (int) (sac.delta * 1000.); // Set initial delay = sample rate in millisecs
	    timer.setDelay(delay);
            speedSlider.setValue(delay);
         } // end try read file
         catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                  "Sorry, but an error occurred while trying to read the data:\n" + e);
         }   

      }//end doOpenSacFile
      
      private void doOpenAsText() {
         if (fileDialog == null)
            fileDialog = new JFileChooser();
         fileDialog.setDialogTitle("Select File to be Opened");
         fileDialog.setSelectedFile(null);  // No file is initially selected.
         int option = fileDialog.showOpenDialog(this);
         if (option != JFileChooser.APPROVE_OPTION)
            return;  // User canceled or clicked the dialog's close box.
         File selectedFile = fileDialog.getSelectedFile();
         Scanner scanner;
         try {
            Reader stream = new BufferedReader(new FileReader(selectedFile));
            scanner = new Scanner( stream );
         }
         catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                  "Sorry, but an error occurred while trying to open the file:\n" + e);
            return;
         }
         try {
            ArrayList<CurveData> newCurves = new ArrayList<CurveData>();
            CurveData curve = new CurveData();
            curve.color = Color.BLACK;
            curve.symmetric = false;
            curve.points = new ArrayList<Point2D.Double>();
            int x = 0;
            double yy;
	    file_is_read = false; // flag to see if data is read in

            while (scanner.hasNextLine()) {
               String line = scanner.nextLine();
               try {
                 yy = Double.valueOf(line.trim()).doubleValue();
               }
               catch (Exception e) {
                  JOptionPane.showMessageDialog(this,
                        "Sorry, but an error occurred while trying to convert the line to double:\n" + e);
                  return;
               }
               curve.points.add( new Point2D.Double(x,yy) );
            } // end while
	    seis.Npts = curve.points.size();
	    seis.dT = 0.2;
	    seis.iframe = 1; // Reset
	    stop.doClick();
	    repaint();
            play.setEnabled(true);

            newCurves.add(curve);
            scanner.close();
            curves = newCurves;
            editFile = selectedFile;
            setTitle("File Read: " + editFile.getName());
	    file_is_read = true;

            setTitle("ASCII File Read: " + selectedFile.getName() + "delta="+seis.dT);
            delay = (int) (seis.dT * 1000.); // Set initial delay = sample rate in millisecs
            timer.setDelay(delay);
            speedSlider.setValue(delay);

         } // end try read file
         catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                  "Sorry, but an error occurred while trying to read the data:\n" + e);
         }   
      }// end doOpenAsText
      
  } // end SimplePaintPanel = JPanel class
} // end SimplePaintWithFiles = JFrame class
