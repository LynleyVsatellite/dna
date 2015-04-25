package dna.features;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dna.DNAToken;

public class Vocabulary {
	
	private Map<String, Integer> vocab;
	
	public Vocabulary( List<DNAToken> tokens, 
			Map<String, Set<Integer>> trainTestValDocsIds ) {
		vocab = buildVocabulary(tokens, trainTestValDocsIds);
	}
	
	
	public int getIndex( String txt ) {
		txt = preprocess(txt);
		if ( vocab.containsKey(txt) )
			return vocab.get(txt);
		else 
			return -1;
	}
	
	public Set<String> getTokens() {
		return vocab.keySet();
	}
	
	public int getSize() {
		return vocab.size();
	}
	
	public static String preprocess(String txt) {
		return txt.toLowerCase();
	}
	
	private Map<String, Integer> buildVocabulary( List<DNAToken> tokens, Map<String, Set<Integer>> trainTestValDocsIds ) {
		Map<String, Integer> voc = new LinkedHashMap<String, Integer>();
		Set<String> index = new LinkedHashSet<String>();
		
		Set<Integer> testDocsIDs = trainTestValDocsIds.get("test");
		Set<Integer> valDocsIDs = trainTestValDocsIds.get("validate");
		
		for (DNAToken tok : tokens) {
			if( !index.contains( preprocess( tok.getText() ) ) && 
					!testDocsIDs.contains( tok.getInternalDocId() ) && 
					!valDocsIDs.contains( tok.getInternalDocId() ) ) {
				index.add( preprocess( tok.getText() ) );
			}
		}
		
		int i = 0;
		for ( String token : index ) {
			if (!voc.containsKey(token)) {
				voc.put(token, i++);
			}
			else {
				throw new RuntimeException("A duplicate token in the tokens index!");
			}
		}
		
		return voc;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
