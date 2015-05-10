package dna.textmining;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

			LinkedHashSet<String> features = new LinkedHashSet<String>();
			LinkedHashSet<String> classes = new LinkedHashSet<String>();

			features.add("sepallength");
			features.add("sepalwidth");
			features.add("petallength");
			features.add("petalwidth");

			classes.add("Iris-setosa");
			classes.add("Iris-versicolor");
			classes.add("Iris-virginica");

			while( (line = datasetReader.readLine()) != null ) {
				Map<String, Double> row = new LinkedHashMap<String, Double>();
				String[] values = line.split(",");

				row.put("sepallength", Double.parseDouble(values[0]));
				row.put("sepalwidth", Double.parseDouble(values[1]));
				row.put("petallength", Double.parseDouble(values[2]));
				row.put("petalwidth", Double.parseDouble(values[3]));
				String label = DNAClassifier.NEGATIVE_CLASS;
				if ( values[4].equals( POS_ClASS ) )
					label = DNAClassifier.POSITIVE_CLASS;
				Sample s = new Sample( row, label );
				trainDataset.add( s );

			}

			while( (line = testsetReader.readLine()) != null ) {
				Map<String, Double> row = new LinkedHashMap<String, Double>();
				String[] values = line.split(",");

				row.put("sepallength", Double.parseDouble(values[0]));
				row.put("sepalwidth", Double.parseDouble(values[1]));
				row.put("petallength", Double.parseDouble(values[2]));
				row.put("petalwidth", Double.parseDouble(values[3]));
				String label = DNAClassifier.NEGATIVE_CLASS;
				if ( values[4].equals( POS_ClASS ) )
					label = DNAClassifier.POSITIVE_CLASS;
				Sample s = new Sample( row, label );
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
		Map<String, Double> row;
		String label;
		public Sample( Map<String, Double> row, String label ) {
			this.row = row;
			this.label = label;
		}
	}

}












