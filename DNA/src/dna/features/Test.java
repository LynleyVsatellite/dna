package dna.features;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import dna.DNATextMiner;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.util.CoreMap;



public class Test {

	public static void main(String[] args) {

		System.out.println( "Started ..." );
		
		String text = "Ich gehe von Berlin mit John";
		String ner_tagger = "/Users/rockyrock/git/dna/DNA/src/dna/features/models/hgc_175m_600.crf.ser.gz";
		String pos_tagger = "/Users/rockyrock/git/dna/DNA/src/dna/features/models/german-hgc.tagger";

		List<String> toks = DNATextMiner.readFlushedList();
		System.out.println( toks.size() );
		
		StringBuffer docStringTokens = new StringBuffer();
		for ( int i = 0; i < toks.size(); i++ ) {
			String x = toks.get(i);
			if(x.equals("\n") || x.equals("") || x.equals(" ") || x.equals("\t") )
				System.out.println("WHITE SPACE!");
			if ( i == toks.size()-1 )
				docStringTokens.append( x );
			else 
				docStringTokens.append( x + "\n" );
		}
		
		Properties props = new Properties();
//		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
		props.setProperty("annotators", "tokenize, ssplit");
		props.setProperty( "tokenize.whitespace", "true" );
		props.setProperty( "ssplit.isOneSentence", "true" );
		props.setProperty( "pos.model", pos_tagger );
		props.setProperty( "ner.model", ner_tagger );
		props.setProperty( "ner.useSUTime", "false" );

		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		Annotation document = new Annotation(docStringTokens.toString());
		// run all Annotators on this text
		pipeline.annotate(document);

		List<CoreMap> sentences = document.get(SentencesAnnotation.class);

		for(CoreMap sentence: sentences) {
			List<CoreLabel> taggedTokens = sentence.get(TokensAnnotation.class);
			System.out.println(taggedTokens.size());
			
			for (int i = 0; i < toks.size(); i++) {
				CoreLabel token = taggedTokens.get(i);
				String tTok = token.get(TextAnnotation.class);
				String mTok = toks.get(i);
				if (!tTok.equals(mTok))
					System.out.println( i + "=>" + tTok + "<-->" + mTok );
			}
			
			
//			for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
//				// this is the text of the token
//				String word = token.get(TextAnnotation.class);
//				System.out.println(word);
//				// this is the POS tag of the token
//				String pos = token.get(PartOfSpeechAnnotation.class);
//				System.out.println(pos);
//				// this is the NER label of the token
//				String ne = token.get(NamedEntityTagAnnotation.class);      
//				System.out.println(ne);
//				System.out.println("----");
//			}

		}
		
	}
	
	public static List<String> tokenize(String text) {
		List<String> tokens = new ArrayList<String>();
		
		StringReader reader = new StringReader(text);
		PTBTokenizer ptbt = new PTBTokenizer(reader,
				new CoreLabelTokenFactory(), "");
		for (CoreLabel label; ptbt.hasNext(); ) {
			label = (CoreLabel) ptbt.next();
			tokens.add(label.toString());
			
		}
		
		return tokens;
	}

}















