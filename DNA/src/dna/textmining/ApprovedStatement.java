package dna.textmining;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import dna.DNAToken;

/**
 * This class generalizes the tokens of an ApprovedStatement and contains two sets of tokens' indices. 
 * The first set contains the indices of the tokens of a concept while the other 
 * set contains the indices of the tokens of the actor who talks about that concept. 
 * Thus, this class represents a single ApprovedStatement.
 * 
 */
public class ApprovedStatement {

	/**
	 * All the tokens in the ApprovedStatement
	 */
	private List<DNAToken> statementTokens;
	/**
	 * The indices of the tokens in {@link #statementTokens} that were
	 * highlighted as either Person tokens or Organization tokens.
	 */
	private Set<Integer> actorTokensIndices;
	/**
	 * The indices of the tokens in {@link #statementTokens} that were
	 * highlighted as Concept.
	 */
	private Set<Integer> conceptTokensIndices;
	
	
	public ApprovedStatement( List<DNAToken> statementTokens ) {
		actorTokensIndices = new LinkedHashSet<Integer>();
		conceptTokensIndices = new LinkedHashSet<Integer>();
		this.statementTokens = statementTokens;
		
		for ( int i = 0; i < statementTokens.size(); i++  ) {
			DNAToken token = statementTokens.get(i);
			
			if ( token.getLabel().equals("Actor") )
				actorTokensIndices.add(i);
			else if ( token.getLabel().equals("Concept") )
				conceptTokensIndices.add(i);
				
		}
	}


	public List<DNAToken> getStatementTokens() {
		return statementTokens;
	}

	public Set<Integer> getActorTokensIndices() {
		return actorTokensIndices;
	}


	public void setActorTokensIndices(Set<Integer> actorTokensIndices) {
		this.actorTokensIndices = actorTokensIndices;
	}


	public Set<Integer> getConceptTokensIndices() {
		return conceptTokensIndices;
	}


	public void setConceptTokensIndices(Set<Integer> conceptTokensIndices) {
		this.conceptTokensIndices = conceptTokensIndices;
	}

	
}






