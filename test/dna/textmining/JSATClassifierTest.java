package dna.textmining;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Test;

import dna.textmining.DNAClassifierTest.Sample;

public class JSATClassifierTest {

	@Test
	public void allPredictionsShouldBeCorrect() {
		DNAClassifier clf = new JSATClassifier();
		Map<String, List<Sample>> datasets1 = DNAClassifierTest.getDataset("Iris-setosa");
		Map<String, List<Sample>> datasets2 = DNAClassifierTest.getDataset("Iris-versicolor");
		Map<String, List<Sample>> datasets3 = DNAClassifierTest.getDataset("Iris-virginica");

		for ( Sample s : datasets1.get("train") ) {
			clf.updateData(s.row, s.label);
		}
		
		clf.updateClassifier();
		
		for ( Sample s : datasets1.get("test") ) {
			assertEquals( clf.classifyInstance(s.row), s.label );
		}
		
		clf = new JSATClassifier();

		for ( Sample s : datasets2.get("train") ) {
			clf.updateData(s.row, s.label);
		}
		
		clf.updateClassifier();
		
		for ( Sample s : datasets2.get("test") ) {
			assertEquals( clf.classifyInstance(s.row), s.label );
		}
		
		clf = new JSATClassifier();

		for ( Sample s : datasets3.get("train") ) {
			clf.updateData(s.row, s.label);
		}
		
		clf.updateClassifier();
		
		for ( Sample s : datasets3.get("test") ) {
			assertEquals( clf.classifyInstance(s.row), s.label );
		}
		
	}

	@Test
	public void shouldNotTakeLong() {
		DNAClassifier clf = new JSATClassifier();

		int numberOfFeatures = 20000;
		int numb_samples = 1000;
		Random rnd = new Random(0);

		//Creating artificial positive samples
		for ( int i = 0; i < numb_samples/2; i++ ) {
			Map<String, Double> row = new HashMap<>();

			for ( Integer j = 0; j < numberOfFeatures; j++ ) {
				row.put(j.toString(), 0.0);
			}

			row.put( new Integer(rnd.nextInt(numberOfFeatures)).toString(), 1.0 );
			row.put( new Integer(rnd.nextInt(numberOfFeatures)).toString(), 1.0 );
			row.put( new Integer(rnd.nextInt(numberOfFeatures)).toString(), 1.0 );
			row.put( new Integer(rnd.nextInt(numberOfFeatures)).toString(), 1.0 );
			row.put( new Integer(rnd.nextInt(numberOfFeatures)).toString(), 1.0 );

			clf.updateData(row, DNAClassifier.POSITIVE_CLASS);
		}

		//Creating artificial negative samples
		for ( int i = 0; i < numb_samples/2; i++ ) {
			Map<String, Double> row = new HashMap<>();

			for ( Integer j = 0; j < numberOfFeatures; j++ ) {
				row.put(j.toString(), 0.0);
			}

			row.put( new Integer(rnd.nextInt(numberOfFeatures)).toString(), 1.0 );
			row.put( new Integer(rnd.nextInt(numberOfFeatures)).toString(), 1.0 );
			row.put( new Integer(rnd.nextInt(numberOfFeatures)).toString(), 1.0 );
			row.put( new Integer(rnd.nextInt(numberOfFeatures)).toString(), 1.0 );
			row.put( new Integer(rnd.nextInt(numberOfFeatures)).toString(), 1.0 );

			clf.updateData(row, DNAClassifier.NEGATIVE_CLASS);
		}
		clf.updateClassifier();
	}

}
