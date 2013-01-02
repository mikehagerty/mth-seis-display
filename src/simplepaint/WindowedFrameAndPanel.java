/*
*	Java Source Code for fast 2D true color animation and games
*	yov408 Technologies Production - http://www.yov408.com
*	This code is free to use and modify.
*	Simple Double Buffering in JPanel and JFrame
*/


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;


class JPanelRenderer extends JPanel{

	BufferedImage image_off;
	int frames = 0;
	Graphics graph;
	int x=5;
	int y=5;


	public JPanelRenderer(GridLayout mylayout){

		super();
		image_off = new BufferedImage(800,600,BufferedImage.TYPE_INT_RGB);
		
	}

	protected void paintComponent(Graphics g){
		image_off.setRGB(x,y,Color.red.getRGB());
	
		frames++;
		String s = "Number of frames printed :"+frames;
		System.out.println(s);

		g.drawImage(image_off,0,0,this);

	}

	public void repainttt(){
		graph = getGraphics();
		paintComponent(graph);
	}

	public void updateXY(){
		x = (int)(Math.random()*100);
		y = (int)(Math.random()*100);
	}
}



public class essai extends JComponent implements Runnable{

	Thread my_app;
	JPanelRenderer pane;

	public static void main(String s[]) {
		JFrame frame = new JFrame( "yov@java" );
		essai baba = new essai();		
		baba.init_pane(frame);				
		frame.setSize(800,600); 
		frame.validate(); 
		frame.setVisible( true ); 
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		baba.start_thread();		
     }

	public void start_thread(){
		my_app = new Thread(this);
		my_app.start();		
	}

	public void init_pane(JFrame frame){
		pane = new JPanelRenderer(new GridLayout(1,1));
		pane.setOpaque(true);
		pane.setLayout(null);
		frame.getContentPane().add(pane); 
	}

	public void run(){

		while(true){

			pane.updateXY();
			pane.repainttt();
			try{

				Thread.sleep(10);

			}
			catch(Exception e){
			}
		}
	}
}
