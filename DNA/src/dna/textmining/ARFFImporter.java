package dna.textmining;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dna.features.SparseVector;

public class ARFFImporter {

	private BufferedReader reader;
	private List<SparseVector> vectors;
	
	public ARFFImporter(String filename) {
		try {

			reader = new BufferedReader( new FileReader(filename) );

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//TODO this needs to be re-done.
	private List[] read() {
		List<SparseVector> X = new ArrayList<SparseVector>();
		List<Double> y = new ArrayList<Double>();
		List[] result = new List[2];
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
				SparseVector v = new SparseVector();
				Map<Integer, Double> entriesHolder = new HashMap<Integer, Double>();
				line = line.replace("{", "");
				line = line.replace("}", "");
				line = line.trim();
				String[] parts = line.split(",");
				
				for ( String entry : parts ) {
					entry = entry.trim();
					String[] entryParts = entry.split(" ");
					entriesHolder.put( Integer.parseInt(entryParts[0].trim()),
							Double.parseDouble(entryParts[1].trim()) );
				}
				
				for ( int i = 0; i < numberOfFeatures-1; i++ ) {
					if ( entriesHolder.containsKey(i) )
						v.add( entriesHolder.get(i) );
					else 
						v.add(0);
				}
				y.add( entriesHolder.get(numberOfFeatures-1) );
				X.add(v);
			}

			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		result[0] = X;
		result[1] = y;
		return result;
	}
	
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

}








