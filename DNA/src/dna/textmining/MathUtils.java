package dna.textmining;

public class MathUtils {
	
	public static double[][] transformMatrix( double[][] m ) {
		
		if ( m.length == 0 )
			throw new RuntimeException( "The matrix doesn't have any elements to be transformed!" );
		
		int numbOfRows = m.length;
		int numbOfCols = m[0].length;
		double[][] t = new double[numbOfCols][numbOfRows];
		
		for ( int row = 0; row < numbOfRows; row++ ) {
			for( int col = 0; col < numbOfCols; col++ ) {
				t[col][row] = m[row][col];
			}
		}
		
		return t;
	}
	
}
