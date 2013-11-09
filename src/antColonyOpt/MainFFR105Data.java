package antColonyOpt;

import Util.Stat;

/**
 * @author Sebastian Ånerud
 * Data taken from http://www.me.chalmers.se/~mwahde/courses/soa/2013/TSPgraphics.zip
 */

import java.awt.*;
import javax.swing.*;

public class MainFFR105Data extends JFrame{

	private static double[][] nodes;
	private static int[] path;
	private static double xyScale = 1;
	private static double xWindow = 800;
	private static double yWindow = 600;
	private static int circleSize = 16;
	private static int borderDistance = 15;
	private static int nIterations = 5000;

	public MainFFR105Data(){
		super();
	}

	@Override
	public void paint(Graphics g){
		g.clearRect(0, 0, (int)xWindow, (int)yWindow);
		g.setColor(Color.blue);
		for(int i = 0; i<nodes.length;i++) {
			g.fillOval(((int)(nodes[i][0]*xyScale)-circleSize/2+borderDistance), 
					((int)(nodes[i][1]*xyScale)-circleSize/2+borderDistance), 
					circleSize, circleSize);
		}
		
		g.setColor(Color.red);
		for(int i = 1; i<path.length;i++) {
			g.drawLine(((int)(nodes[path[i-1]][0]*xyScale)+borderDistance), 
					  ((int)(nodes[path[i-1]][1]*xyScale)+borderDistance), 
					  ((int)(nodes[path[i]][0]*xyScale)+borderDistance), 
					  ((int)(nodes[path[i]][1]*xyScale)+borderDistance));
		}
	}

	public static void main(String arg[]){

		Stat<Double> runTimes = new Stat<Double>();

		String dataSource = System.getProperty("user.dir") +
				"/data files/LoadCityLocations.txt";

		AntSystem as = new AntSystem(dataSource, 60, 1, 2, 0.5);
		double bestPathLength = as.getBestPathLength();
		System.out.println("NNP length: " + bestPathLength);
		
		/*
		 * Get the nodes and the current path
		 * Also find the max coordinate value for scaling
		 */
		nodes = as.getLocations();
		path = as.getBestpath();
		double xyMax = 0;
		for(int i = 0; i<nodes.length;i++) {
			if(nodes[i][0]>xyMax) {
				xyMax = nodes[i][0];
				xyScale = (xWindow-2*(circleSize+borderDistance))/xyMax;
			}
			if(nodes[i][1]>xyMax) {
				xyMax = nodes[i][1];
				xyScale = (yWindow-2*(circleSize+borderDistance))/xyMax;
			}
		}
		
		MainFFR105Data f = new MainFFR105Data();
		f.setSize((int)xWindow,(int)yWindow);
		f.setVisible(true);

		for(int i = 0; i<nIterations; i++) {
			double startTime = System.currentTimeMillis();
			as.iterateACO();
			double endTime = System.currentTimeMillis();
			runTimes.addObservation(endTime-startTime);
			if(bestPathLength > as.getBestPathLength()) {
				bestPathLength = as.getBestPathLength();
				path = as.getBestpath();
				System.out.println("Best length: " +bestPathLength);
				f.removeAll();
				f.repaint();
			}
		}
		if(Util.Util.isPermutation(as.getBestpath())) {
			System.out.println("Valid path");
		} else {
			System.out.println("Not valid path");
		}
		System.out.println("Mean(iterations) = " + runTimes.getMean());
		
	}
} 

