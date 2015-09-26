package dna.textmining;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import dna.features.SparseVector;

/**
 * A class to export a sparse dataset into the ARFF file format.
 * It exports all feature vectors in the sparse ARFF format. 
 *
 */
public class ARFFExporter {
	
	private BufferedWriter bw;
	
	/**
	 * @param filename the name and location of the ARFF file to be saved. 
	 * @param numberOfFeatures the number of features in each feature vector. Note that the class label
	 * will be saved as an extra attribute in the ARFF file. So the total number of attributes in the ARFF file will
	 * be {@code numberOfFeatures+1 }. The class label attribute is saved as a numerical attribute.
	 */
	public ARFFExporter(String filename, int numberOfFeatures) {
		try {
			bw = new BufferedWriter( new FileWriter(filename) );
			
			bw.write( "@relation dataset\n" );
			for ( int i = 0; i < numberOfFeatures; i++ ) {
				bw.write( "@attribute " + i + " numeric\n" );
			}
			bw.write( "@attribute " + numberOfFeatures + " numeric\n" );//class attribute
			bw.write( "@data\n" );
			bw.flush();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public ARFFExporter(String filename, SparseDataset dataset) {
		this(filename, dataset.getX().get(0).size());
		for ( int i = 0; i < dataset.getX().size(); i++ ) {
			SparseVector vec = dataset.getX().get(i);
			double label = dataset.getY()[i];
			append(vec.toArray(), label);
		}
		close();
	}
	
	/**
	 * Appends a feature vector to the ARFF file. Call this method for every feature vector.
	 * @param array the feature vector
	 * @param label the class label (1.0 for positive and 0.0 for negative). 
	 */
	public void append(double[] array, double label) {
		try {
			bw.write( "{" );
			for ( int i = 0; i < array.length; i++ ) {
				if ( array[i] != 0 )
					bw.write( i + " " + array[i] + ", " );
			}
			
			bw.write( array.length + " " + label + "}\n" );
			
			bw.flush();
			 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
