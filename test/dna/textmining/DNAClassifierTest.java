package dna.textmining;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Create datasets to check the output of classifiers 
 *
 */

public class DNAClassifierTest {

	public static Map<String, List<Sample>> getDataset( String POS_CLASS ) {
		if ( POS_CLASS.equals("") )
			throw new RuntimeException( "The class name can't be empty!" );

		final String POS_ClASS = POS_CLASS;
		Map<String, List<Sample>> datasets = new HashMap<>();
		List<Sample> trainDataset = new ArrayList<>();
		List<Sample> testDataset = new ArrayList<>();

		try{
			BufferedReader datasetReader = new BufferedReader( new FileReader("datasets/iris.csv") );
			BufferedReader testsetReader = new BufferedReader( new FileReader("datasets/iris-test.csv") );
			String line;
			int numbOfFeats = 4;
			LinkedHashSet<String> classes = new LinkedHashSet<String>();
			classes.add("Iris-setosa");
			classes.add("Iris-versicolor");
			classes.add("Iris-virginica");

			while( (line = datasetReader.readLine()) != null ) {
				String[] values = line.split(",");
				double[] vec = new double[numbOfFeats];
				vec[0] = Double.parseDouble(values[0]);
				vec[1] = Double.parseDouble(values[1]);
				vec[2] = Double.parseDouble(values[2]);
				vec[3] = Double.parseDouble(values[3]);
				
				String label = DNAClassifier.NEGATIVE_CLASS;
				if ( values[4].equals( POS_ClASS ) )
					label = DNAClassifier.POSITIVE_CLASS;
				Sample s = new Sample( vec, label );
				trainDataset.add( s );

			}

			while( (line = testsetReader.readLine()) != null ) {
				String[] values = line.split(",");
				double[] vec = new double[numbOfFeats];
				
				vec[0] = Double.parseDouble(values[0]);
				vec[1] = Double.parseDouble(values[1]);
				vec[2] = Double.parseDouble(values[2]);
				vec[3] = Double.parseDouble(values[3]);
				
				String label = DNAClassifier.NEGATIVE_CLASS;
				if ( values[4].equals( POS_ClASS ) )
					label = DNAClassifier.POSITIVE_CLASS;
				Sample s = new Sample( vec, label );
				testDataset.add( s );
			}

			datasets.put("train", trainDataset);
			datasets.put("test", testDataset);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return datasets;
	}

	static class Sample {
		double[] vec;
		String label;
		public Sample( double[] vec, String label ) {
			this.vec = vec;
			this.label = label;
		}
	}

}












