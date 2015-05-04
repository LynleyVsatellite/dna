package dna.textmining;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import weka.classifiers.functions.SimpleLogistic;
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

public class TextMiningApp {

	public static void main(String[] args) {
		System.out.println( "Started..." );
		System.out.println( new Date() );
		String file1 = "/Users/rockyrock/Desktop/s0.dna";
//		String file1 = "/Users/rockyrock/Desktop/steffi.dna";
		String file2 = "/Users/rockyrock/Desktop/okt.dna";
		List<String> files = new ArrayList<String>();
		files.add(file1);
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
		
		TokenClassifier clf = new TokenClassifier(dataset, new SimpleLogistic(), 1);
		clf.train();
		System.out.println("Done training.");
		clf.validate();
		System.out.println( new Date() );
	}
	
}
