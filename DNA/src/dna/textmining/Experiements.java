package dna.textmining;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ml.classification.LinearBinarySVM;
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

public class Experiements {
	
	public static void main(String[] args) {
		int option = Integer.parseInt(args[0]);
		
		if ( option == 1 ) { //Person
			for ( int i = 0; i < 3; i++ )
				exp1( "Person", i );
		}
		else if( option == 2 ) { //Org
			for ( int i = 0; i < 3; i++ )
				exp1( "Organization", i );
		}
		else if ( option == 3 ) { //Concept
			for ( int i = 0; i < 3; i++ )
				exp1( "Concept", i );
		}
		else {
			throw new RuntimeException( "Unknown option!!" );
		}
		
	}

	public static void exp1(String classLabel, int windowSize) {
		System.out.println( "Started..." );
		System.out.printf("classLabel: %s, window size: %d\n", classLabel, windowSize);
		System.out.println( new Date() );
		String file0 = "train_data/s0.dna";
		String file1 = "train_data/steffi.dna";
		String file2 = "train_data/okt.dna";
		List<String> files = new ArrayList<String>();
//		files.add(file0);
		files.add(file1);
		files.add(file2);
		DNATextMiner textMiner = new DNATextMiner( new StanfordDNATokenizer() );
		
		List<DNAFeature> features = new ArrayList<DNAFeature>();
		features.add( new HasCapitalLetterDNAFeature() );
//		features.add( new CoreNLPDNAFeature(true, true) );
		features.add( new HasWeirdCharDNAFeature() );
		features.add( new AllCapitalizedDNAFeature() );
		features.add( new IsANumberDNAFeature() );
		features.add( new NumberOfCharsDNAFeature() );
		features.add(new WordDNAFeature());
		features.add(new NGramDNAFeature(3));
		
		String[] featuresNames = { "HasCapitalLetterDNAFeature",  "CoreNLPDNAFeature", "HasWeirdCharDNAFeature",
				"AllCapitalizedDNAFeature", "IsANumberDNAFeature", "NumberOfCharsDNAFeature", "WordDNAFeature", 
				"NGramDNAFeature"};
		
		for ( int i = -1; i < features.size(); i++ ) {
			if ( i == -1 )
				System.out.println( "Running a new test with all features" );
			else
				System.out.println( "Running a new test with out feature: " + featuresNames[i] );
			List<DNAFeature> tempFeatures = new ArrayList<DNAFeature>();
			for ( int j = 0; j < features.size(); j++ ) {
				if ( j != i ) {
					tempFeatures.add( features.get(j) );
				}
			}
			
			Dataset dataset = 
					textMiner.makeDataset(files, classLabel, tempFeatures, 
							0.6, 0.2, 0.2, 1, false);
			
			int regularizationType = 0;
			double lambda = 0;
			
//			ml.classification.Classifier lamlOrigClf = new LogisticRegression(regularizationType, lambda);
			
			double C = 1.0;
			double eps = 1e-4;
			ml.classification.Classifier lamlOrigClf = new LinearBinarySVM(C, eps);
			
			LAMLClassifier lamlClf = new LAMLClassifier(lamlOrigClf);
			TokenClassifier clf = new TokenClassifier(dataset, lamlClf, windowSize);
			
			clf.train();
			clf.validate();
			System.out.println( new Date() );
		}
		
		
	}
	
}








