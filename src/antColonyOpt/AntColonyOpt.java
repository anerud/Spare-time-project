package antColonyOpt;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 
 * @author Sebastian Ånerud
 * Data taken from http://www.me.chalmers.se/~mwahde/courses/soa/2013/TSPgraphics.zip
 */
public class AntColonyOpt {
	
	private double[][] locations;
	
	public AntColonyOpt(String dataSource) {
		loadData(dataSource);
	}
	
	public Integer[] iterateACO() {
		return null;
	}
	
	public double[][] getLocations() {
		return locations;
	}
	
	/**
	 * @param dataSource each location bust be on a new row and x- and y-coordinates
	 * must be delimited by " ".
	 */
	private void loadData(String dataSource){
		BufferedReader data;
		try {
			data = new BufferedReader(new FileReader(dataSource));
			
			//Count number of rows
			int n = 1;
			String location = data.readLine();
	        while ((location = data.readLine()) != null && location.length() > 0) {
	            n++;
	        }
			locations = new double[n][2];
			
			data = new BufferedReader(new FileReader(dataSource));
			int i = 0;
			while ((location = data.readLine()) != null && location.length() > 0) {
				String[] d = location.split(" ");
//				System.out.println(d[0] + " " + d[1]);
				locations[i][0] =  Double.parseDouble(d[0]);
				locations[i][1] =  Double.parseDouble(d[1]);
				i++;
	        }
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}
}
