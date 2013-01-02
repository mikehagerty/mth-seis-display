/*
*	Java Source Code for fast 2D true color animation and games
*	yov408 Technologies Production - http://www.yov408.com
*	Memory Image Source Buffering
*	** Most of the code taken from JavaHelp.com **
*/

import java.awt.*;
import java.awt.event.*;
import java.awt.image.MemoryImageSource;

public class essai{

	public static void main(String[] argv){

		boolean casse_toi=false;


		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
	
	   	Frame frame = new Frame(gd.getDefaultConfiguration());
    	Window win = new Window(frame);
    
		win.addMouseListener(new MouseAdapter() {
        	public void mousePressed(MouseEvent evt) {
	            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment()
    	            .getDefaultScreenDevice();
        	    gd.setFullScreenWindow(null);
        	}
    	});

    
    	try {
	        
       	gd.setFullScreenWindow(win);
       	win.requestFocus();

 		DisplayMode[] affichages = gd.getDisplayModes();
		int i;
		for(i=0;i<affichages.length;i++){
			if((affichages[i].getWidth()==1025)&&(affichages[i].getHeight()==768)&&(affichages[i].getBitDepth()==32)){
				gd.setDisplayMode(affichages[i]);
			}
		}

       	int screenWidth = win.getWidth();
       	int screenHeight = win.getHeight();

		Graphics graphicwindow = win.getGraphics();
		graphicwindow.setColor(Color.black);
       	graphicwindow.fillRect(0,0,screenWidth,screenHeight);

   	    int size = screenWidth * screenHeight;
		int[] pixels = new int[size];

	    MemoryImageSource source = new MemoryImageSource(screenWidth, screenHeight, pixels, 0, screenWidth);
	    source.setAnimated(true);
	    source.setFullBufferUpdates(true);
		Image offscreen = Toolkit.getDefaultToolkit().createImage(source);
		int frames=0;


		while(!casse_toi){
			
			frames++;
		    for (i = 0; i < size; i++) {
				pixels[i] = 0x00000000;
	   		}

		    for (i = 0; i < size; i++) {
				pixels[i] = 0xffff0000;
	   		}
	   		source.newPixels();
			String s = "frame"+frames;
			graphicwindow.drawImage(offscreen,0,0,null); 
			graphicwindow.setColor(Color.black);
	 		graphicwindow.drawString(s,20,20);
		
		}


    	}
		catch (Throwable e)
		{
        
    	}

	}


}