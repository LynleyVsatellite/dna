package dna.textmining;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import dna.features.SparseVector;


public class ListUtils {
	
	public static void printMatrix(double[][] M) {
		for ( int i = 0; i < M.length; i++ ) {
			for ( int j = 0; j < M[i].length; j++ ) {
				if ( j ==  M[i].length-1)
					System.out.print( M[i][j] );
				else
					System.out.print( M[i][j] + ", " );
			}
			System.out.println();
		}
	}
	
	public static void printArray( double[] array ) {
		for ( int i = 0; i < array.length; i++ ) {
			System.out.print( array[i] + ", " );
		}
		System.out.println();
	}

	public static List<Double> asList(double[] array) {
		List<Double> list = new ArrayList<Double> ();
		for ( double d : array ) {
			list.add(d);
		}
		
		return list;
	}
	
	public static List<Integer> asList(int[] array) {
		List<Integer> list = new ArrayList<Integer> ();
		for ( int d : array ) {
			list.add(d);
		}
		
		return list;
	}
	
	public static double[] asDoubleArray(List<Double> list) {
		double[] vec = new double[list.size()];
		
		for ( int i = 0; i < list.size(); i++ ) {
			vec[i] = list.get(i);
		}
			
		return vec;
	}
	
	public static int[] asIntArray(List<Integer> list) {
		int[] vec = new int[list.size()];
		
		for ( int i = 0; i < list.size(); i++ ) {
			vec[i] = list.get(i);
		}
			
		return vec;
	}
	
	public static int[] asIntArray(Set<Integer> set) {
		int[] vec = new int[set.size()];
		
		int i = 0;
		
		for ( int x : set ) {
			vec[i++] = x;
		}
		
		return vec;
	}
	
	public static int[] doubleArrayToIntArray( double[] a1 ) {
		int[] a2 = new int[a1.length];
		
		for ( int i = 0; i < a1.length; i++ ) {
			a2[i] = (int) a1[i];
		}
		
		return a2;
	}
	
	public static double[] intArrayToDoubleArray( int[] a1 ) {
		double[] a2 = new double[a1.length];
		
		for ( int i = 0; i < a1.length; i++ ) {
			a2[i] = a1[i];
		}
		
		return a2;
	}
	
	public static List<SparseVector> toSparseList( double[][] X ) {
		List<SparseVector> M = new ArrayList<SparseVector>();
		
		for ( int i = 0; i < X.length; i++ ) {
			SparseVector v = new SparseVector();
			for ( int j = 0; j < X[i].length; j++ ) {
				v.add( X[i][j] );
			}
			M.add(v);
		}
		
		return M;
	}
	
}
