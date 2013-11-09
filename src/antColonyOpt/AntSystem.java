package antColonyOpt;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

/**
 * @author Sebastian Ånerud
 */
public class AntSystem {
	
	private int nLocations = 0;
	private int nAnts;
	private int[] bestPath;
	private int[] path;
	private double pathLength;
	private double bestPathLength;
	private double[][] locations;
	private double[][] pheromoneLevels;
	private double[][] deltaPheromone;
	private double[][] visibility;
	private boolean[] visited;
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
	 * Iterates the Ant System once.
	 * @return the currently best path
	 */
	public void iterateACO() {
		deltaPheromone = new double[nLocations][nLocations];
		for(int k = 0; k<nAnts; k++) {
			/*
			 * Generate path
			 */
			int start = rand.nextInt(nLocations);
			path[0] = start;
			pathLength = 0;
			visited[start]=true;
			int lastVisited = start;
			for(int i = 1; i<nLocations;i++) {
				/*
				 * Prepare pheromone levels
				 */
				double[] pe = new double[nLocations];
				double marginSum = 0;
				for(int j=0; j<nLocations;j++) {
					if(!visited[j]) {
						pe[j] = Math.pow(pheromoneLevels[lastVisited][j], a)*
								 Math.pow(visibility[lastVisited][j], b);
						marginSum += pe[j];
					} 
				}
		
				/*
				 * randomize next location in path
				 */
				double p = rand.nextDouble()*marginSum;
				int nextLocation = 0;
				while(visited[nextLocation]) {
					nextLocation++;
				}
				double peCumulative = pe[nextLocation];
				while(peCumulative < p) {
					nextLocation++;
					if(!visited[nextLocation]) {
						peCumulative += pe[nextLocation];
					}
				}
				/*
				 * Update path and variables for next iteration
				 */
				path[i] = nextLocation;
				pathLength += 1/visibility[lastVisited][nextLocation];
				visited[nextLocation] = true;
				lastVisited = nextLocation;
				
			}
			
			/*
			 * Add the length of trip back to first location
			 */
			pathLength += 1/visibility[lastVisited][start];
			
			/*
			 * Update best path
			 */
			if(pathLength < bestPathLength) {
				bestPathLength = pathLength;
				bestPath = path.clone();
			}
			
			/*
			 * Update deltaPheromone
			 */
			for(int i = 1; i<nLocations; i++) {
				deltaPheromone[path[i-1]][path[i]] += 1/pathLength;
			}
			deltaPheromone[nLocations-1][path[0]] += 1/pathLength;
			
			clearVisitedList();
		}
		
		for(int i = 0; i<nLocations;i++) {
			for(int j = 0; j<nLocations; j++) {
				pheromoneLevels[i][j] = (1-rho)*pheromoneLevels[i][j] + deltaPheromone[i][j];
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
	
	/**
	 * Resets the array "visited" to false.
	 */
	private void clearVisitedList(){
		for(int i=0; i<nLocations;i++) {
			visited[i]=false;
		}
	}
	
	/**
	 * Initializes:
	 * - Visibility matrix as distances between locations
	 * - Pheromone levels to number of locations divided by the length
	 *   of the nearest neighbor path.
	 */
	private void initializeAS(){
		/*
		 * Initialize variables
		 */
		pheromoneLevels = new double[nLocations][nLocations];
		visibility = new double[nLocations][nLocations];
		bestPath = new int[nLocations];
		path = new int[nLocations];
		bestPathLength = Double.MAX_VALUE;
		visited = new boolean[nLocations];
		rand = new Random();
		
		/*
		 * Initialize visibility matrix
		 */
		for(int i = 0; i < nLocations; i++) {
			for(int j = i + 1; j < nLocations; j++) {
				double dx = locations[i][0] - locations[j][0];
				double dy = locations[i][1] - locations[j][1];
				visibility[i][j] = 1/Math.sqrt(dx*dx + dy*dy);
				visibility[j][i] = visibility[i][j];
			}
		}
		
		/*
		 * Calculate nearest neighbor path.
		 */
		int start = rand.nextInt(nLocations);
		int lastVisited = start;
		visited[start] = true;
		path[0] = start;
		double nnPathLength = 0;
		for(int i = 1; i < nLocations; i++){
			int nn = 0; //Index of nearest neighbor
			double nnDistance = Double.MAX_VALUE;
			for(int j = 0; j < nLocations; j++) {
				if (!visited[j]) {
					if(nnDistance > 1/visibility[j][lastVisited]){
						nn = j;
						nnDistance = 1/visibility[j][lastVisited];
					}
				}
			}
			path[i]=nn;
			nnPathLength += 1/visibility[nn][lastVisited];
			visited[nn] = true;
			lastVisited = nn;
		}
		clearVisitedList();
		nnPathLength += 1/visibility[start][lastVisited];
		bestPathLength = nnPathLength;
		bestPath = path;
		
		/*
		 * Initialize pheromone levels to nLocations/nnPathLength
		 */
		double tau0 = nLocations/nnPathLength;
		for(int i = 0; i<nLocations;i++) {
			for(int j=0; j<nLocations;j++) {
				pheromoneLevels[i][j] = tau0;
			}
		}
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
