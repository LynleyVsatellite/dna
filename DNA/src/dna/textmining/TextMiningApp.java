package dna.textmining;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ml.classification.LogisticRegression;
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
//		files.add(file0);
		files.add(file1);
		files.add(file2);
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
		
		
//		LinearL1SCD( 10000, 1e-14, Loss.SQUARED )
		
//		jsat.classifiers.Classifier jsatClf = new LSSVM(new RBFKernel(0.5), SupportVectorLearner.CacheMode.FULL);
//		TokenClassifier clf = new TokenClassifier(dataset, new JSATClassifier( jsatClf ), 1);
		
//		RandomForest f = new RandomForest();
//		f.setOptions( Utils.splitOptions("-I 20 -S 0") );
//		TokenClassifier clf = new TokenClassifier(dataset, f, 1);
//		clf.toARFF();
//		DNAClassifier dnaClf = new JSATClassifier(new LogisticRegressionDCD());
//		TokenClassifier clf = new TokenClassifier(dataset, dnaClf, 1);
		
		int regularizationType = 0;
		double lambda = 0;
		
		ml.classification.Classifier lamlOrigClf = new LogisticRegression(regularizationType, lambda);
		LAMLClassifier lamlClf = new LAMLClassifier(lamlOrigClf);
		TokenClassifier clf = new TokenClassifier(dataset, lamlClf, 1);
		
		System.out.println( "Sample Feature Space Size: " + clf.getSampleFeatureSpaceSize() );
		clf.train();
//		System.out.println("Done training.");
		clf.validate();
		//Once done optimization for parameters, then test the performance of the classifier to see how it generalizes.
		clf.test();
		System.out.println( new Date() );
	}
	
}


















