package dna.textmining;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dna.DNAFeature;
import dna.features.AllCapitalizedDNAFeature;
import dna.features.CoreNLPDNAFeature;
import dna.features.HasCapitalLetterDNAFeature;
import dna.features.HasWeirdCharDNAFeature;
import dna.features.IsANumberDNAFeature;
import dna.features.NGramDNAFeature;
import dna.features.NumberOfCharsDNAFeature;
import dna.features.WordDNAFeature;
import ml.classification.LogisticRegression;

public class ACExperiements {
	
	public static void main(String[] args) {
		System.out.println( "Started..." );
		System.out.println( new Date() );
		String file0 = "train_data/s0.dna";
		String file1 = "train_data/steffi.dna";
		String file2 = "train_data/okt.dna";
		List<String> files = new ArrayList<String>();
//		files.add(file0);
		files.add(file1);
		files.add(file2);
		
		List<DNAFeature> features = new ArrayList<DNAFeature>();
		features.add( new HasCapitalLetterDNAFeature() );
		features.add( new CoreNLPDNAFeature(true, true) );
		features.add( new HasWeirdCharDNAFeature() );
		features.add( new AllCapitalizedDNAFeature() );
		features.add( new IsANumberDNAFeature() );
		features.add( new NumberOfCharsDNAFeature() );
		features.add(new WordDNAFeature());
		features.add(new NGramDNAFeature(3));
		
		int regularizationType = 0;
		double lambda = 0;
		ml.classification.Classifier lamlOrigClf = new LogisticRegression(regularizationType, lambda);
		
		LAMLClassifier lamlClf = new LAMLClassifier(lamlOrigClf);
		
		ActorConceptLinker acLinker = new ActorConceptLinker(files, features, lamlClf, 1, 
				0.6, 0.2, 0.2, 0, true, true, true);
		acLinker.train();
		acLinker.test();
		System.out.println( new Date() );
		System.out.println( "+++ Done +++" );
		
	}

}
