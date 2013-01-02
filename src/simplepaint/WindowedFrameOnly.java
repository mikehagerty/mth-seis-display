/*
*	Java Source Code for fast 2D true color animation and games
*	yov408 Technologies Production - http://www.yov408.com
*	This code is free to use and modify.
*	Simple Double Buffering in Window
*/

import java.awt.*;
import java.awt.image.BufferedImage;

public class essai{
	public static void main(String[] argv){
		
		boolean casse_toi=false;

    	Frame frame = new Frame("Simple Double Buffering in Window");
    	Window win = new Window(frame);

    	try {
	        
 		frame.setSize(640,480) ; 
		frame.validate(); 
		frame.setVisible(true); 
 
 		int i;
       	int screenWidth = 640;
       	int screenHeight = 480;

		Graphics graphicwindow = frame.getGraphics();
		graphicwindow.setColor(Color.white);
       	graphicwindow.fillRect(0,0,screenWidth,screenHeight);

		BufferedImage offscreen = new BufferedImage(screenWidth,screenHeight,BufferedImage.TYPE_INT_RGB);
		Graphics offscreen_high = offscreen.createGraphics();
		int frames=0;
		int j;

		while(!casse_toi){
			frames++;
			offscreen_high.setColor(Color.black);
	       	offscreen_high.fillRect(0,0,screenWidth,screenHeight);
			
			for(i=50;i<400;i++){
			for(j=50;j<400;j++){
				offscreen.setRGB(i,j,Color.green.getRGB());
			}
			}

			String s = "frame"+frames;
			offscreen_high.setColor(Color.red);
	 		offscreen_high.drawString(s,20,460);
			graphicwindow.drawImage(offscreen,0,0,null); 
		}

 
    	}

		catch (Throwable e)
		{
    	}
	}
}