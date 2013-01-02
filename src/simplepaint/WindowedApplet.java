/*
*	Java Source Code for fast 2D true color animation and games
*	yov408 Technologies Production - http://www.yov408.com
*	This code is free to use and modify.
*	Applet double buffering in window
*/


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.BufferedImage;



public class essai extends JApplet implements Runnable, MouseMotionListener{

	BufferedImage offscreen; 
	Graphics screen_;

	int frames;
     	int curX=5, curY=5; 
	Thread my_app;

	boolean buffer_ready=false;
	
	

	public void init() {

		addMouseMotionListener(this);
	}

	public void start(){
		frames=0;
		my_app = new Thread(this);
		my_app.start();
    	}
    
	public void paint(Graphics g) {

			System.out.println("Réallocation du backbuffer");
 			setBackground(Color.black); 
       			offscreen = (BufferedImage)createImage(this.getSize().width,this.getSize().height); 
	        	screen_ = getGraphics();
			buffer_ready = true;
			System.out.println("Appel du 'paint' de l'applet et graphics nulles");

    	}
   

	public void update(Graphics g) 
    	{ 
		System.out.println("Appel de 'update' de l'applet");
         	paint(g); 
    	} 


 
	public void stop(){
		System.out.println("Appel du 'stop' de l'applet");
		my_app = null;
	}




	public void mouseMoved(MouseEvent evt)  
	{ 
        	curX = evt.getX(); 
        	curY = evt.getY(); 
        	Repaindre(screen_); 
     	} 
  

	public void mouseDragged(MouseEvent evt)  
     	{ 
     	} 

	public static void main(String s[]) {
		JFrame frame = new JFrame( "yov@java" );
		System.out.println("Création de l'applet");
		essai theApplet = new essai();
		System.out.println("Appel de 'init' de l'applet");
		theApplet.init(); 
		System.out.println("Appel de 'start' de l'applet");
		theApplet.start(); 

		System.out.println("ajout de l'applet a la fenetre");
		frame.getContentPane().add(theApplet); 
		frame.setSize(500,500); 
		System.out.println("validate");
		frame.validate(); 
		System.out.println("setvisible = true");
		frame.setVisible( true ); 
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
     	}
	

	public void run(){
		while(my_app != null){
			if(buffer_ready==true){
				//curX = (int)(Math.random() * this.getSize().width);
				//curY = (int)(Math.random() * this.getSize().height);
				Repaindre(screen_);
				try
      				{
        				//Thread.sleep(10);
				}
				catch(Exception e){
					stop();
				}
			}		
		}
	}




	public void Repaindre(Graphics g){

		frames++;
		String s = "Frames rendered: "+frames;
		System.out.println(s);
		
		offscreen.setRGB(curX,curY,Color.red.getRGB());
		
		g.drawImage(offscreen,0,0,this); 
	}


}
