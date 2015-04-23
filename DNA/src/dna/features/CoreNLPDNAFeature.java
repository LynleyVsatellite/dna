package dna.features;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import dna.DNAFeature;
import dna.DNAToken;
import dna.Utils;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class CoreNLPDNAFeature extends DNAFeature {
	
	private static final String ner_tagger = 
			"/Users/rockyrock/git/dna/DNA/src/dna/features/models/hgc_175m_600.crf.ser.gz";
	private static final String pos_tagger = 
			"/Users/rockyrock/git/dna/DNA/src/dna/features/models/german-hgc.tagger";
	private static final String[] posTagsList = 
		{ "ADJA", "ADJD", "ADV", "APPR", "APPRART", "APPO", "APZR", "ART", "CARD",
		"FM", "ITJ", "KOUI", "KOUS", "KON", "KOKOM", "NN", "NE", "PDS", "PDAT", "PIS", "PIAT",
		"PIDAT", "PPER", "PPOSS", "PPOSAT", "PRELS", "PRELAT", "PRF", "PWS", "PWAT", "PWAV",
		"PAV", "PTKZU", "PTKNEG", "PTKVZ", "PTKANT", "PTKA", "TRUNC", "VVFIN", "VVIMP", "VVINF",
		"VVIZU", "VVPP", "VAFIN", "VAIMP", "VAINF", "VAPP", "VMFIN", "VMINF", "VMPP",
		"XY", "$,", "$.", "$("}; 
	private static final int NER_NUMB_FEATURES = 4;
	private StanfordCoreNLP pipeline;
	
	public CoreNLPDNAFeature() {
		Properties props = new Properties();
	    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
	    props.setProperty( "tokenize.whitespace", "true" );
	    props.setProperty( "ssplit.isOneSentence", "true" );
	    props.setProperty( "pos.model", pos_tagger );
	    props.setProperty( "ner.model", ner_tagger );
	    props.setProperty( "ner.useSUTime", "false" );
		pipeline = new StanfordCoreNLP(props);
	}

	@Override
	public List<DNAToken> buildFeature(List<DNAToken> tokens) {
		
		int currentDocID = 0;
		StringBuffer docStringTokens = new StringBuffer();
		List<DNAToken> docDNATokens = new ArrayList<DNAToken>();
		for ( int c = 0; c < tokens.size(); c++ ) {
			DNAToken token = tokens.get(c);
			//buffer the current document tokens so Stanford CoreNLP can be used
			if ( token.getInternalDocId() == currentDocID && c != tokens.size()-1 ) {
				docStringTokens.append( token.getText() + " " );
				docDNATokens.add(token);
			}
			else {
//				System.out.println( "Flushing... " + currentDocID + ", new id: " + token.getInternalDocId() );
				if ( c == tokens.size()-1 ) {
					docStringTokens.append( token.getText() + " " );
					docDNATokens.add(token);
				}
				//Tag the processed tokens with NER and POS tags and then empty the buffers and start with a new document
				Annotation document = new Annotation(docStringTokens.toString());
			    pipeline.annotate(document);
			    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
			    if (sentences.size() != 1) 
			    	throw new RuntimeException("The number of sentences is more than 1!");
			    else {
			    	for(CoreMap sentence: sentences) {
				    	List<CoreLabel> taggedTokens = sentence.get(TokensAnnotation.class);
				    	if (taggedTokens.size() != docDNATokens.size()) 
				    		throw new RuntimeException("The number of tagged tokens is different from the number of document tokens!");
				    	else {
				    		for ( int i = 0; i < taggedTokens.size(); i++ ) {
				    			DNAToken docDNAToken = docDNATokens.get(i);
				    			CoreLabel taggedToken = taggedTokens.get(i);
				    			String taggedTokenText = taggedToken.get(TextAnnotation.class);
				    			String posTag = taggedToken.get(PartOfSpeechAnnotation.class);
				    			String nerTag = taggedToken.get(NamedEntityTagAnnotation.class);  
				    			
				    			if ( !docDNAToken.getText().equals( taggedTokenText ) ) {
				    				throw new RuntimeException("The document token is different from the Stanford token!");
				    			}
				    			else {
				    				//Construct the features for the DNA document token.
				    				double[] pos_features = new double[posTagsList.length];
				    				for ( int j = 0; j < posTagsList.length; j++ ) {
				    					if ( posTag.equals( posTagsList[j] ) ) {
				    						pos_features[j] = 1;
				    					}
				    						
				    				}
				    				
				    				double[] ner_features = new double[ NER_NUMB_FEATURES ];
				    				if ( nerTag.equals( "I-PER" ) || nerTag.equals( "O-PER" ) )
				    					ner_features[0] = 1;
									else if ( nerTag.equals( "I-LOC" ) || nerTag.equals( "O-LOC" ) )
										ner_features[1] = 1;
									else if ( nerTag.equals( "I-ORG" ) || nerTag.equals( "O-ORG" ) )
										ner_features[2] = 1;
									else if ( nerTag.equals( "I-MISC" ) || nerTag.equals( "O-MISC" ) )
										ner_features[3] = 1;
				    				
				    				docDNAToken.getFeatures().addAll( Utils.asList(pos_features) );
				    				docDNAToken.getFeatures().addAll( Utils.asList(ner_features) );
				    			}
				    			
				    		}
				    	}
				    	
				    }
			    }
//			    System.out.println("Starting processing a new document");
				//Start with a new document
				docStringTokens = new StringBuffer();
				docDNATokens = new ArrayList<DNAToken>();
				docStringTokens.append( token.getText() + " " );
				docDNATokens.add(token);
				currentDocID = token.getInternalDocId();
			}
		}
		
		return tokens;
	}

	@Override
	public int numberOfFeatures() {
		return posTagsList.length + NER_NUMB_FEATURES;
	}


}








