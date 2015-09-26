package dna.textmining;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dna.features.SparseVector;


/**
 * This class is used to read an ARFF file and return the data as a {@link SparseDataset}.
 *
 */
public class ARFFImporter {

	private BufferedReader reader;

	/**
	 * @param filename the location of the ARFF file to be read.
	 */
	public ARFFImporter(String filename) {
		try {

			reader = new BufferedReader( new FileReader(filename) );

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reads the ARFF file and returns it as a {@link SparseDataset}.
	 * @return
	 */
	public SparseDataset asSparseDataset() {
		
		final List<SparseVector> X = new ArrayList<SparseVector>();
		final List<Double> y = new ArrayList<Double>();
		
		int numberOfFeatures = 0;
		try {
			String line = "";
			while( (line = reader.readLine()) != null ) {
				if ( line.trim().equals("@data") )
					break;
				if ( line.contains("@attribute") )
					numberOfFeatures++;
			}

			while( (line = reader.readLine()) != null ) {
				List<Integer> indices = new ArrayList<Integer>();
				List<Double> values = new ArrayList<Double>();
				line = line.replace("{", "");
				line = line.replace("}", "");
				line = line.trim();
				String[] parts = line.split(",");
				
				for ( String entry : parts ) {
					entry = entry.trim();
					String[] entryParts = entry.split(" ");
					indices.add( Integer.parseInt( entryParts[0].trim() ) );
					values.add( Double.parseDouble( entryParts[1].trim() ) );
				}
				
				indices.remove( indices.size() - 1 );
				y.add( values.get( values.size() - 1 ) );
				values.remove( values.size() - 1 );
				SparseVector v = new SparseVector( numberOfFeatures - 1, 
						ListUtils.asIntArray(indices), ListUtils.asDoubleArray(values) );
				X.add(v);
			}

			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		SparseDataset dataset = new SparseDataset() {
			
			@Override
			public double[] getY() {
				return ListUtils.asDoubleArray(y);
			}
			
			@Override
			public List<SparseVector> getX() {
				return X;
			}
		};
		
		return dataset;
	}
	
	public SimpleDataset asSimpleDataset() {
		SparseDataset sparseDataset = asSparseDataset();
		return new SimpleDataset(sparseDataset.getX(), sparseDataset.getY());
	}

}








