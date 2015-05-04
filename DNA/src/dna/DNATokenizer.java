package dna;

import java.util.List;

public interface DNATokenizer {

	/**
	 * Tokenizes the text.
	 * @param offset the start position of the text in the document.
	 * @param text the text to tokenize
	 * @return list of tokens.
	 */
	public List<DNAToken> tokenize(int offset, String text);
	public List<String> tokenize(String text);
	
}
