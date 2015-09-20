package dna.textmining;

import java.util.List;

public class StatsUtils {
	
	/**
	 * Prints classification statistics (Recal, Precision, F1-measure).
	 * @param y_test the ground-truth.
	 * @param preds the results from the classifier to be tested.
	 */
	public static void printStats(List<Double> y_test, List<Double> preds) {
		printStats( ListUtils.asDoubleArray(y_test), ListUtils.asDoubleArray(preds) );
	}
	
	/**
	 * Prints classification statistics (Recal, Precision, F1-measure).
	 * @param y_test the ground-truth.
	 * @param preds the results from the classifier to be tested.
	 */
	public static void printStats(double[] y_test, double[] preds) {

		if ( y_test.length != preds.length )
			throw new RuntimeException("The ground truth array and prediction array don't have the same length");

		int tp = 0;
		int fp = 0;
		int tn = 0;
		int fn = 0;

		for ( int i = 0; i < y_test.length; i++ ) {
			if ( y_test[i] == 1 && preds[i] == 1 )
				tp++;
			else if( y_test[i] == 1 && preds[i] == 0 )
				fn++;
			else if( y_test[i] == 0 && preds[i] == 1 )
				fp++;
			else
				tn++;
		}
		double recall = getRecall(tp, fn);
		double precision = getPrecision(tp, fp);
		double fmeasure = getFMeasure(recall, precision);
		System.out.println( "++++++ stats ++++++" );
		System.out.printf( "Recal: %f, Precision: %f, F1-measure: %f\n", recall, precision, fmeasure );
		System.out.printf( "TP: %d, TN: %d, FP: %d, FN: %d\n", tp, tn, fp, fn );
	}

	public static double getRecall( int tp, int fn ) {
		if ( 0 == (tp+fn) )
			return 0;
		else
			return (double) tp / (tp + fn); 
	}

	public static double getPrecision(int tp, int fp) {
		if ( 0 == (tp + fp) ) 
			return 0;
		else
			return (double) tp / (tp + fp); 
	}
	
	public static double getFMeasure( double recall, double precision ) {
		if ( (precision + recall) == 0)
			return 0;
		else
			return 2 * precision * recall / (precision + recall);
	}
	
	
}
