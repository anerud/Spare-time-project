package antColonyOpt;

public class MainFFR105Data {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String dataSource = "C:\\Users\\Sebbe\\Documents\\GitHub\\Spare-time-project\\" +
				"data files\\LoadCityLocations.txt";
		
		AntSystem as = new AntSystem(dataSource, 25, 1, 1);
		as.iterateACO();
	}

}
