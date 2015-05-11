package dna.textmining;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jsat.classifiers.linear.LogisticRegressionDCD;
import dna.DNAFeature;
import dna.DNATextMiner;
import dna.StanfordDNATokenizer;
import dna.features.AllCapitalizedDNAFeature;
import dna.features.HasCapitalLetterDNAFeature;
import dna.features.HasWeirdCharDNAFeature;
import dna.features.IsANumberDNAFeature;
import dna.features.NGramDNAFeature;
import dna.features.NumberOfCharsDNAFeature;
import dna.features.WordDNAFeature;

/**
 * An example of how train a classifier using dna files and then how to validate or test it.
 *
 */

public class TextMiningApp {

	public static void main(String[] args) throws Exception {
		System.out.println( "Started..." );
		System.out.println( new Date() );
		String file0 = "/Users/rockyrock/Desktop/s0.dna";
		String file1 = "/Users/rockyrock/Desktop/steffi.dna";
		String file2 = "/Users/rockyrock/Desktop/okt.dna";
		List<String> files = new ArrayList<String>();
		files.add(file0);
//		files.add(file1);
//		files.add(file2);
		String classLabel = "Person";
		DNATextMiner textMiner = new DNATextMiner( new StanfordDNATokenizer() );
		
		List<DNAFeature> features = new ArrayList<DNAFeature>();
		features.add( new HasCapitalLetterDNAFeature() );
//		features.add( new CoreNLPDNAFeature() );
		features.add( new HasWeirdCharDNAFeature() );
		features.add( new AllCapitalizedDNAFeature() );
		features.add( new IsANumberDNAFeature() );
		features.add( new NumberOfCharsDNAFeature() );
		features.add(new WordDNAFeature());
		features.add(new NGramDNAFeature(3));
		
		Dataset dataset = 
				textMiner.makeDataset(files, classLabel, features, 
						0.6, 0.2, 0.2, 1);
		
//		MultilayerPerceptron nn = new MultilayerPerceptron();
//		try {
//			nn.setOptions(Utils.splitOptions("-L 0.3 -M 0.2 -N 1 -S 0 -H 1"));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		Classifier wekaClf = new RandomForest();
//		wekaClf.setOptions( Utils.splitOptions( "-I 10" ) );
//		TokenClassifier clf = new TokenClassifier(dataset, wekaClf, 1);
		
		DNAClassifier dnaClf = new JSATClassifier(new LogisticRegressionDCD());
		TokenClassifier clf = new TokenClassifier(dataset, dnaClf, 1);
		
		
		
		System.out.println( "Sample Feature Space Size: " + clf.getWindowFeatureSpaceSize() );
		clf.train();
		System.out.println("Done training.");
		clf.validate();
		//Once done optimization for parameters, then test the performance of the classifier to see how it generalizes.
		//clf.test();
		System.out.println( new Date() );
	}
	
}


















