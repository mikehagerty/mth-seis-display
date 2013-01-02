 
/******************************************************************

        Java Applet buff.java
        March 1998 - Michael J. Hurben

This applet provides a _simulation_ of the famous Buffon's Needle
experiment.  Buffon's Needle is a Monte Carlo simulation for the
determination of the value of pi.

******************************************************************/

import java.applet.*;
import java.awt.*;

public class buff extends Applet implements Runnable
{
 Dimension offDimension, d;  // Variables used to create an
 Image offImage;             // offscreen image via the update() 
 Graphics offGraphics;       // method, to reduce flicker

 int needleLength = 20;
 int numRows = 10;

 int xBorder = 15;     // Basic layout variables
 int yBorder = 45; 
 int margin = 50; 
     
 int tableHeight = needleLength*numRows;    // 
 int tableWidth = 200;                      // Define size of table
 int tableXcorner = xBorder;                // where needles are thrown
 int tableYcorner = yBorder;                // 
     
 int barWidth = 20;             // Define size of bar graph
 int barHeight = tableHeight;
 int barXcorner = xBorder+tableXcorner+tableWidth+margin;
 int barYcorner = tableYcorner; 

 double barTop = 3.24;     // Set limits for limits of bar graph
 double barBot = 3.04;  
 double barMid = (barTop+barBot)/2;
 
 int barValue;     // Dynamic value of the bar graph
   
 int plotWidth = 200;          // Define size of plot
 int plotHeight = tableHeight; 
 int plotXcorner = barXcorner+margin+barWidth+xBorder;
 int plotYcorner = tableYcorner; 

 double plotMax = 4;      // Set limits on the plot
 double plotMin = 2;
 double plotMid = (plotMax+plotMin)/2;    

 int barMaxPlot = (int) (plotHeight*(plotMax-barTop)/
			 (plotMax-plotMin));
 int barMinPlot = (int) (plotHeight*(plotMax-barBot)/
			 (plotMax-plotMin));  
 int barThick=(barMaxPlot==barMinPlot? 1 :
		         barMinPlot-barMaxPlot);  
 
 int pointerWidth=30;          // Bar graph pointer
 int pointerTop, pointerBot;  
   
 int power = 5;  // Orders of magnitude for the x-axis of the plot
   
 int n = 0;     // The number of needles thrown 
 int hit = 0;   // The number of needles which hit lines
 int i = 1;     // 

 double needleX1, needleX2, needleY1, needleY2;
 double deltaX, deltaY;
 
 int ysign;  
   
 final double piValue=2.0*Math.atan(1.0);
  
 int x1, x2, y1, y2;      // Used to draw needles
 int gx1, gx2, gy1, gy2;  // Used to make the plot

 int speed = 20;   // Sets speed with try-catch block
     
 double h = 0;             // double version of the number of hits
 double estPi, prevEstPi;  // Estimates of pi
 double prob = 1;          // Ratio of hits to trys
 double prev = 1;          // Ratio on the previous trial

 boolean oldScreen = false;
 boolean begin = false;
   
 Thread t;
 Button b1, b2, b3, b4;

 public void init()                     
 {                                    
  setLayout(new FlowLayout(FlowLayout.LEFT));
  b1 = new Button("Start");
  b2 = new Button("Slower");
  b3 = new Button("Faster");
  b4 = new Button("Done");
	 
  add(b1);
  add(b2);
  add(b3);
  add(b4);
	 
  t = new Thread(this);
  t.start();
 }
 
 public boolean action(Event e, Object o)
 {
  if (o.equals("Start"))
  {
   n = 0;
   hit = 0;
   oldScreen = false;
   begin = true;  
  }
  else if (o.equals("Done"))
  { 
   t.stop();
  }
  else if (o.equals("Faster"))
  {
   speed = (speed>10? speed-10 : 10);         
  }                   
  else if (o.equals("Slower"))
  {
   speed = speed+10;
  } 
  return true;
 }

 public void run()      
 {                      
  while(true)       
  {
   if(begin)
   {    
   // Calculate the position of the needle ends...   
	     
    needleX1 = tableWidth*Math.random();
    needleY1 = tableHeight*Math.random();
    deltaX = needleLength*Math.sin(2*piValue*Math.random()-piValue);   
    deltaY = Math.sqrt(needleLength*needleLength-deltaX*deltaX);
    needleX2 = needleX1+deltaX;
    ysign = (Math.random()<0.5? -1 : 1);
    needleY2 = needleY1+ysign*deltaY;   

    // Check to see if it crosses a line...   
	  
    for (int yLine=0; yLine<=needleLength*numRows; yLine+=needleLength)
    {  
     if((needleY1<=yLine && needleY2>=yLine) ||
	     (needleY1>=yLine && needleY2<=yLine))
     {
      hit++;
      break;  
     }
    }
    n++;   
    prev = prob;
    h = hit;
    prob = h/n;
    prevEstPi = 2/prev;
    estPi = 2/prob;

    // Calculation for the plot

    gx1 = (int)(plotXcorner+Math.log(n)*plotWidth/(power*Math.log(10)));
    gx2 = (int)(plotXcorner+Math.log(n+1)*plotWidth/(power*Math.log(10)));
    gy1 = (int)(plotYcorner+(plotHeight/(plotMin-plotMax))*
	         (prevEstPi-plotMax));
    if (gy1<=plotYcorner) gy1 = plotYcorner+1;
    if (gy1==plotYcorner+plotHeight) gy1=gy1-1;
    gy2 = (int)(plotYcorner+(plotHeight/(plotMin-plotMax))*
	     (estPi-plotMax));
    if (gy2<=plotYcorner) gy2 = plotYcorner+1;      
    if (gy2==plotYcorner+plotHeight) gy2=gy2-1;
     
    // Calculation for drawing the needles

    x1 = (int) (xBorder+needleX1);
    x2 = (int) (xBorder+needleX2);
    y1 = (int) (yBorder+needleY1);
    y2 = (int) (yBorder+needleY2);
    repaint();
   }
   try
   {
    Thread.currentThread().sleep(speed);
   }
   catch(InterruptedException e) 
   {
   }	       	         
  }
 }
                                          
 public void paint(Graphics g)         
 {                                  
  d = size();
  update(g);
 }

 public void update(Graphics g)
 {
  if((offGraphics==null)                // Setup an off-screen image
      ||(d.width!=offDimension.width)      // via the update() method.
       || (d.height!=offDimension.height)) //
  {
   offDimension = d;
   offImage = createImage(d.width, d.height);
   offGraphics = offImage.getGraphics();
  }

  if(!oldScreen)
  {                                             
   offGraphics.setColor(getBackground());        
   offGraphics.fillRect(0,0, d.width, d.height); 
   offGraphics.setColor(Color.gray); 
   offGraphics.fillRect(plotXcorner, plotYcorner+barMaxPlot,
	       plotWidth, barMinPlot-barMaxPlot);
   offGraphics.drawString(" "+barMid+"-", barXcorner-45,
			 barYcorner+barHeight/2);
   offGraphics.drawString(" "+barTop+"-", barXcorner-45, 
			 barYcorner+5);
   offGraphics.drawString(" "+barBot+"-", barXcorner-45, 
			 barYcorner+3+barHeight);
   offGraphics.drawString("-"+plotMax, plotXcorner+plotWidth+2,
			 plotYcorner+5);
   offGraphics.drawString("-"+plotMin, plotXcorner+plotWidth+2,
			 plotYcorner+plotHeight+5);
   offGraphics.drawString("-"+plotMid, plotXcorner+plotWidth+2,
			 plotYcorner+plotHeight/2+5);	
   offGraphics.drawString("Estimate vs. log(number of tries)",
			 plotXcorner+5, plotYcorner+plotHeight+20);
  
   // Draw lines which relate the bar graph and the plot... 
        
   offGraphics.drawLine(barXcorner+barWidth+5, barYcorner, 
		       plotXcorner-5, barMaxPlot+plotYcorner);
   offGraphics.drawLine(barXcorner+barWidth+5, barYcorner+barHeight, 
		       plotXcorner-5, barMinPlot+plotYcorner);  
  
   // Draw tabletop and the plot axes

   offGraphics.setColor(Color.blue);
   offGraphics.drawRect(tableXcorner,tableYcorner, tableWidth, tableHeight);
   offGraphics.drawRect(plotXcorner, plotYcorner,plotWidth, plotHeight);
	
   for(i=1; i<tableHeight/needleLength; i++)
   {
    offGraphics.drawLine(tableXcorner, tableYcorner+needleLength*i,
	    tableXcorner+tableWidth, tableYcorner+needleLength*i);
   }   
   oldScreen = true;                              
  }                                             
                                                     
  else                                         
  {                                             
   offGraphics.setColor(getBackground());
   offGraphics.fillRect(0,0, d.width, yBorder-10);
  }

  // Write n and estPi to screen

  offGraphics.setColor(Color.black);
  offGraphics.drawString("Current estimate of pi: "+estPi, 
		  plotXcorner+10, 20);
  offGraphics.drawString("Number of tries: "+n, 220, 20);
 
  offGraphics.setColor(Color.black);

  // Make sure that none of the needles appear outside 
	// the table boundary:	
	
  x1 = (x1tableXcorner+tableWidth?
      tableXcorner+tableWidth:x1);       
  x2 = (x2tableXcorner+tableWidth?
      tableXcorner+tableWidth:x2);       
  y1 = (y1tableYcorner+tableHeight?
       tableYcorner+tableHeight:y1);
  y2 = (y2tableYcorner+tableHeight?
      tableYcorner+tableHeight:y2);

  offGraphics.drawLine(x1,y1,x2,y2);

  // Draw the bar graph

  offGraphics.setColor(Color.gray);
  offGraphics.fillRect(barXcorner, barYcorner, 20, barHeight);

  offGraphics.setColor(Color.red);
       
  barValue = (int)(barHeight*(estPi-barTop)/(barBot-barTop));       
  if (barValue<0-pointerWidth/2) barValue=0-pointerWidth/2;
  if (barValue>barHeight+pointerWidth/2) barValue=barHeight+
                    pointerWidth/2;

  pointerTop = (barValue-pointerWidth/2<0 ? 
		0 : barValue-pointerWidth/2); 
  pointerBot = (barValue+pointerWidth/2>barHeight? 
		barHeight : barValue+pointerWidth/2);
    
  offGraphics.fillRect(barXcorner, barYcorner+pointerTop, 
		       barWidth, pointerBot-pointerTop);
  
  // Center line in the pointer
    
  offGraphics.setColor(Color.gray);
  if (barValue<0) barValue=0;
  if (barValue>barHeight) barValue=barHeight;  
  offGraphics.drawLine(barXcorner, barYcorner+barValue, 
		barXcorner+barWidth-1, barYcorner+barValue);
    
  // Finally, draw the plot	
	
  offGraphics.setColor(Color.red);   
    
  offGraphics.drawLine(gx1,gy1,gx2,gy2);
  g.drawImage(offImage, 0, 0, this);
 }
}
