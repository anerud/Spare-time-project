package antColonyOpt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import Util.Stat;
/**
 * @author Sebastian Ånerud
 * Data taken from http://www.me.chalmers.se/~mwahde/courses/soa/2013/TSPgraphics.zip
 */

public class MainFFR105Data extends JPanel{

	private static double[][] nodes;
	private static int[] path;
	private static double xyScale = 1;
	private static double xWindow = 800;
	private static double yWindow = 600;
	private static int circleSize = 16;
	private static int borderDistance = 15;
	private static int nIterations = 5000;
	private static JButton startButton;
	private static Stat<Double> runTimes;
	private static String dataSource = System.getProperty("user.dir") +
			"/data files/LoadCityLocations.txt";
	private static MainFFR105Data f;
			

	public MainFFR105Data(){
		super();
	}

	public static void main(String arg[]){

		runTimes = new Stat<Double>();

		/*
		 * Create new Ant System
		 * The best path after creation is a random 
		 * nearest neighbor path.
		 */
		AntSystem as = new AntSystem(dataSource, 60, 1, 2, 0.5);
		
		/*
		 * Get the nodes and the current path
		 * Also find the max coordinate value for scaling
		 */
		nodes = as.getLocations();
		path = new int[0];
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
		
    	f = new MainFFR105Data();
		f.display();
		
	}
	
	@Override
	public void paint(Graphics g){
		// Clear window
		g.clearRect(0, 0, (int)xWindow, (int)yWindow);
		
		// Paint the edges of currently best path
		g.setColor(Color.red);
		for(int i = 1; i<path.length;i++) {
			g.drawLine(((int)(nodes[path[i-1]][0]*xyScale)+borderDistance), 
					  ((int)(nodes[path[i-1]][1]*xyScale)+borderDistance), 
					  ((int)(nodes[path[i]][0]*xyScale)+borderDistance), 
					  ((int)(nodes[path[i]][1]*xyScale)+borderDistance));
		}
		
		// Paint the nodes
		g.setColor(Color.blue);
		for(int i = 0; i<nodes.length;i++) {
			g.fillOval(((int)(nodes[i][0]*xyScale)-circleSize/2+borderDistance), 
					((int)(nodes[i][1]*xyScale)-circleSize/2+borderDistance), 
					circleSize, circleSize);
		}
	}
	
	private class ButtonPanel extends JPanel implements ActionListener {

        public ButtonPanel() {
        	startButton = new JButton("Start new");
        	startButton.addActionListener(this);
            this.add(startButton);
        }

		@Override
		public void actionPerformed(ActionEvent e) {
			/*
			 * Create new Ant System
			 * The best path after creation is a random 
			 * nearest neighbor path.
			 */
			AntSystem as = new AntSystem(dataSource, 60, 1, 2, 0.5);
			double bestPathLength = as.getBestPathLength();
			System.out.println("NNP length: " + bestPathLength);
			
			// Iterate the ant system
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
					f.paint(f.getGraphics());
				}
			}
			
			/*
			 * If the path is not a valid path the algorithms has a bug.
			 * The algorithm should always produce a valid path.
			 */
			if(!Util.Util.isPermutation(as.getBestpath())) {
				System.out.println("Not valid path!!!");
			}
			
			System.out.println("Iteration running time mean = " + 
					Util.Util.roundNDecimals(runTimes.getMean(),4));
			System.out.println("Iteration running time standard deviation = " + 
					Util.Util.roundNDecimals(runTimes.getSampleVariance(),4));
			
		}
    }
	
	private void display() {
        JFrame f = new JFrame("Ant System");
        f.setPreferredSize(new Dimension((int)xWindow,(int)yWindow+100));
        this.setSize((int)xWindow,(int)yWindow);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(this);
        f.add(new ButtonPanel(), BorderLayout.SOUTH);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }
} 

