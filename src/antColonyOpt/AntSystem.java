package antColonyOpt;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Random;

/**
 * @author Sebastian Ånerud
 */
public class AntSystem {
	
	private int nLocations = 0;
	private int nAnts;
	private int[] bestPath;
	private int[] currentPath;
	private double pathLength;
	private double bestPathLength;
	private double[][] locations;
	private double[][] pheromoneLevels;
	private double[][] deltaPheromone;
	private double[][] visibility;
	private LinkedList<Integer> notVisited;
	private double a;
	private double b;
	private double rho;
	private Random rand;
	
	public AntSystem(String dataSource, int nAnts, double a, double b, double p) {
		loadData(dataSource);
		this.nAnts = nAnts;
		this.a = a;
		this.b = b;
		this.rho = p;
		initializeAS();
	}
	
	/**
	 * Initializes:
	 * - Visibility matrix as distances between locations
	 * - Pheromone levels to number of locations divided by the length
	 *   of the nearest neighbor path.
	 */
	private void initializeAS(){
		
		//Initialize variables
		pheromoneLevels = new double[nLocations][nLocations];
		visibility = new double[nLocations][nLocations];
		bestPath = new int[nLocations];
		currentPath = new int[nLocations];
		bestPathLength = Double.MAX_VALUE;
		rand = new Random();
		notVisited = getLinkedListLocations();
		
		// Initialize visibility matrix as 1/distance between nodes
		for(int i = 0; i < nLocations; i++) {
			for(int j = i + 1; j < nLocations; j++) {
				double dx = locations[i][0] - locations[j][0];
				double dy = locations[i][1] - locations[j][1];
				visibility[i][j] = 1/Math.sqrt(dx*dx + dy*dy);
				visibility[j][i] = visibility[i][j];
			}
		}
		
		// Calculate nearest neighbor path.
		int start = rand.nextInt(nLocations);
		removeElementInNotVisited(start);
		int lastVisited = start;
		currentPath[0] = start;
		double nnPathLength = 0;
		for(int i = 1; i < nLocations; i++){
			ListIterator<Integer> it = notVisited.listIterator();
			int nn = 0; //Index of nearest neighbor
			double nnDistance = Double.MAX_VALUE;
			while(it.hasNext()) {
				int j = it.next();
				if(nnDistance > 1/visibility[j][lastVisited]){
					nn = j;
					nnDistance = 1/visibility[j][lastVisited];
				}
			}
			currentPath[i]=nn;
			removeElementInNotVisited(nn);
			nnPathLength += 1/visibility[nn][lastVisited];
			lastVisited = nn;
		}
		
		nnPathLength += 1/visibility[start][lastVisited];
		bestPathLength = nnPathLength;
		bestPath = currentPath;
		
		// Initialize pheromone levels to nLocations/nnPathLength
		double tau0 = nLocations/nnPathLength;
		for(int i = 0; i<nLocations;i++) {
			for(int j=0; j<nLocations;j++) {
				pheromoneLevels[i][j] = tau0;
			}
		}
	}
	
	/**
	 * Iterates the Ant System once.
	 * @return the currently best path
	 */
	public void iterateACO() {
		deltaPheromone = new double[nLocations][nLocations];
		for(int k = 0; k<nAnts; k++) {
			
			notVisited = getLinkedListLocations();
			
			//Randomize starting point in path
			int start = rand.nextInt(nLocations);
			currentPath[0] = start;
			pathLength = 0;
			removeElementInNotVisited(start);
			int lastVisited = start;
			for(int i = 1; i<nLocations;i++) {
				//Prepare the pheromone levels for step i in path.
				double[] pe = new double[nLocations];
				double marginSum = 0;
				ListIterator<Integer> it = notVisited.listIterator();
				while(it.hasNext()) {
					int j = it.next();
					pe[j] = Math.pow(pheromoneLevels[lastVisited][j], a)*
							 Math.pow(visibility[lastVisited][j], b);
					marginSum += pe[j];
				}
		
				//randomize next location in path
				double p = rand.nextDouble()*marginSum;
				it = notVisited.listIterator();
				int nextLocation = it.next();
				double peCumulative = pe[nextLocation];
				while(peCumulative < p) {
					nextLocation = it.next();
					peCumulative += pe[nextLocation];
				}
				
				//Update path and variables for next iteration
				currentPath[i] = nextLocation;
				pathLength += 1/visibility[lastVisited][nextLocation];
				it.remove();
				lastVisited = nextLocation;
			}
			
			// Add the length of trip back to first location
			pathLength += 1/visibility[lastVisited][start];
			
			// Update best path
			if(pathLength < bestPathLength) {
				bestPathLength = pathLength;
				bestPath = currentPath.clone();
			}
			
			// Update deltaPheromone
			for(int i = 1; i<nLocations; i++) {
				deltaPheromone[currentPath[i-1]][currentPath[i]] += 1/pathLength;
			}
			deltaPheromone[nLocations-1][currentPath[0]] += 1/pathLength;
			
		}
		
		//Update pheromone levels
		for(int i = 0; i<nLocations;i++) {
			for(int j = 0; j<nLocations; j++) {
				pheromoneLevels[i][j] = (1-rho)*pheromoneLevels[i][j] + deltaPheromone[i][j];
			}
		}
	}
	
	private void removeElementInNotVisited(int e) {
		Iterator<Integer> it = notVisited.iterator();
		while(it.hasNext()) {
			int i = it.next();
			if(e == i) {
				it.remove();
				return;
			}
		}
	}

	/**
	 * @return the locations of the Ant System
	 */
	public double[][] getLocations() {
		return locations;
	}
	
	/**
	 * Returns the best path (not a copy of it for the sake of performance).
	 * @return returns an int[]-reference to the best path.
	 */
	public int[] getBestpath(){
		return bestPath.clone();
	}
	
	/**
	 * @return the length of the best path.
	 */
	public double getBestPathLength(){
		return bestPathLength;
	}
	
	private LinkedList<Integer> getLinkedListLocations(){
		LinkedList<Integer> l = new LinkedList<Integer>();
		for(int i = 0; i< nLocations; i++) {
			l.add(i);
		}
		return l;
	}
	
	/**
	 * This method must be called the first thing done in constructor.
	 * @param dataSource each location bust be on a new row and x- and y-coordinates
	 * must be delimited by " ".
	 */
	private void loadData(String dataSource){
		BufferedReader data;
		try {
			data = new BufferedReader(new FileReader(dataSource));
			
			//Count number of rows
			nLocations = 1;
			String location = data.readLine();
	        while ((location = data.readLine()) != null && location.length() > 0) {
	        	nLocations++;
	        }
			locations = new double[nLocations][2];
			
			data = new BufferedReader(new FileReader(dataSource));
			int i = 0;
			while ((location = data.readLine()) != null && location.length() > 0) {
				String[] d = location.split(" ");
				locations[i][0] =  Double.parseDouble(d[0]);
				locations[i][1] =  Double.parseDouble(d[1]);
				i++;
	        }
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}
}
