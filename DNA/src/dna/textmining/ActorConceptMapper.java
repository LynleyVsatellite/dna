package dna.textmining;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dna.DNAToken;

/**
 * This class is a mapping helper. It helps to fetch the documents' tokens and the links
 *  between the actors tokens and the concepts tokens in each document.
 */
public class ActorConceptMapper {

	/**
	 * Maps from document's ID to document's Tokens
	 */
	private Map<Integer, List<DNAToken>> fromDocIdToDocTokens;
	
	/**
	 * Maps from document's ID to document's actorsToConcepts links
	 */
	private Map<Integer, Map<Integer, Set<Integer>>> fromDocIdToActorsConceptsLinks;
	
	public ActorConceptMapper() {
		fromDocIdToDocTokens = new HashMap<Integer, List<DNAToken>>();
		fromDocIdToActorsConceptsLinks = new HashMap<Integer, Map<Integer, Set<Integer>>>();
	}

	public Map<Integer, List<DNAToken>> getFromDocIdToDocTokens() {
		return fromDocIdToDocTokens;
	}

	public void setFromDocIdToDocTokens(
			Map<Integer, List<DNAToken>> fromDocIdToDocTokens) {
		this.fromDocIdToDocTokens = fromDocIdToDocTokens;
	}

	public Map<Integer, Map<Integer, Set<Integer>>> getFromDocIdToActorsConceptsLinks() {
		return fromDocIdToActorsConceptsLinks;
	}

	public void setFromDocIdToActorsConceptsLinks(
			Map<Integer, Map<Integer, Set<Integer>>> fromDocIdToActorsConceptsLinks) {
		this.fromDocIdToActorsConceptsLinks = fromDocIdToActorsConceptsLinks;
	}
	
	
}













