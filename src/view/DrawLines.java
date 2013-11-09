package view;

import java.awt.*;
import javax.swing.*;

public class DrawLines extends JFrame{
	
	private static int x1 = 10;
	private static int y1 = 10;
	private static int x2 = 150;
	private static int y2 = 150;
	

  public DrawLines(){
    super();
  }

  @Override
  public void paint(Graphics g){
    g.drawLine(x1,y1,x2,y2);
  }

  public static void main(String arg[]){
	  DrawLines frame = new DrawLines();
    frame.setSize(800,600);

    frame.setVisible(true);
    x1 = 10;
    y1 = 150;
    x2 = 150;
    y2 = 10;
    frame.update(frame.getGraphics());
  }
} 