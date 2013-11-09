package Util;

/**
 * 
 * @author Sebastian Ånerud
 *
 */
public class Util {

	/**
	 * Checks if an array is a permutation of the numbers 
	 * 0 to (array.length - 1).
	 * @param perm array with a possible permutation.
	 * @return true if it is a permutation false otherwise.
	 */
	public static boolean isPermutation(int[] perm) {
		boolean[] checked = new boolean[perm.length];
		for(int i=0; i<perm.length;i++) {
			if(checked[perm[i]]) {
				return false;
			}
			checked[perm[i]] = true;
		}
		return true;
	}
	
	/**
	 * rounds a number to N decimals
	 * @param d the number to round
	 * @param N the number of decimals
	 * @return
	 */
	public static double roundNDecimals(double d, int N) {
		double factor = Math.pow(10, N);
		double result = d * factor;
		result = Math.round(result);
		result = result / factor;
		return result;
	}
	
	public static double[] getNormallyDistributedVector(){
		return getNormallyDistributedVector(1);
	}
	
	public static double[] getNormallyDistributedVector(double std){
		double U = Math.random();
		double V = Math.random();
		double x = Math.sqrt(-2*Math.log(U))*Math.cos(2*Math.PI*V);
		double y = Math.sqrt(-2*Math.log(U))*Math.sin(2*Math.PI*V);
		double[] vector = {std*x,std*y};
		return vector;
	}
}
