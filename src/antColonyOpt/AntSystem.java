package antColonyOpt;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

/**
 * 
 * @author Sebastian Ånerud
 * Data taken from http://www.me.chalmers.se/~mwahde/courses/soa/2013/TSPgraphics.zip
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
	private double[][] visibility;
	private boolean[] visited;
	private double a;
	private double b;
	private Random rand;
	
	public AntSystem(String dataSource, int nAnts, double a, double b) {
		loadData(dataSource);
		this.nAnts = nAnts;
		this.a = a;
		this.b = b;
		initializeAS();
	}
	
	/**
	 * Iterates the Ant System once.
	 * @return the currently best path
	 */
	public int[] iterateACO() {
		for(int k = 0; k<nAnts; k++) {
			System.out.println("Ant: " + k);
			int start = rand.nextInt(nLocations);
			path[0] = start;
			pathLength = 0;
			visited[start]=true;
			int lastVisited = start;
			double[] pe = new double[nLocations];
			double marginSum = 0;
			for(int i=0; i<nLocations;i++) {
				pe[i] = Math.pow(pheromoneLevels[lastVisited][i], a)*
						 Math.pow(visibility[lastVisited][i], b);
				marginSum += pe[i];
			}
			marginSum -= pe[start]; //Correction because start was added in for-loop above.
			
			/*
			 * Generate path
			 */
			for(int i = 1; i<nLocations;i++) {
//				System.out.println("Iteration: " + i);
//				double checksum = 0;
//				for(int ii = 0; ii<nLocations;ii++) {
//					if(!visited[ii]) {
//						checksum += pe[ii];
//					}
//				}
//				System.out.println("Cumulative sum: " + (checksum/marginSum));
				
				/*
				 * randomize next location in path
				 */
				double p = rand.nextDouble()*marginSum;
				int nextLocation = 0;
				double peCumulative = 0;
				while(peCumulative < p) {
					if(!visited[nextLocation]) {
						peCumulative += pe[nextLocation];
					}
					nextLocation++;
				}
				nextLocation--; //Correction
//				System.out.println(nextLocation + " | " + p + " | " + peCumulative);
				
				/*
				 * Update path and variables for next iteration
				 */
				path[i] = nextLocation;
				pathLength += visibility[lastVisited][nextLocation];
				visited[nextLocation] = true;
				marginSum -= pe[nextLocation];
				lastVisited = nextLocation;
				
//				System.out.println("------------------------------------------");
			}
			
//			System.out.println("Generated path: ");
//			for(int i=0;i<nLocations;i++){
//				System.out.print(path[i] + ", ");
//			}
//			System.out.println("");
//			System.out.println(isPermutation(path));
//			System.out.println("");
			
			System.out.println(pathLength);
			
			clearVisitedList();
		}
		
		return bestPath;
	}
	
	/**
	 * @return the locations of the Ant System
	 */
	public double[][] getLocations() {
		return locations;
	}
	
	/**
	 * Resets the array visited to false.
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
				visibility[i][j] = Math.sqrt(dx*dx + dy*dy);
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
					if(nnDistance > visibility[j][lastVisited]){
						nn = j;
						nnDistance = visibility[j][lastVisited];
					}
				}
			}
			path[i]=nn;
			nnPathLength += visibility[nn][lastVisited];
			visited[nn] = true;
			lastVisited = nn;
		}
		clearVisitedList();
		nnPathLength += visibility[start][lastVisited];
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
	
	/**
	 * Checks if an array is a permutation of the numbers 
	 * 0 to (array.length - 1).
	 * @param perm array with a possible permutation.
	 * @return true if it is a permutation false otherwise.
	 */
	private boolean isPermutation(int[] perm) {
		boolean[] checked = new boolean[perm.length];
		for(int i=0; i<perm.length;i++) {
			if(checked[perm[i]]) {
				return false;
			}
			checked[perm[i]] = true;
		}
		return true;
	}
}
