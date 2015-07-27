package dna;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dna.textmining.ActorConceptMapper;

/**
 * This class preprocesses the DNA files in a format suitable [for {@link ActorConceptLinker] to construct feature
 * vectors to solve the fourth task of the project, 
 * or in other words to find the concepts that each actor is talking about.
 */
public class ActorConceptDataPreprocessor {
	
	public static void main(String[] args) {
		System.out.println("Started");
		
		String file1 = "/Users/rockyrock/Desktop/s0_copy.dna";
		List<File> files = new ArrayList<File>();
		files.add(new File(file1));
		
		ActorConceptDataPreprocessor acDataProcessor = new ActorConceptDataPreprocessor(new StanfordDNATokenizer());
		ActorConceptMapper mapper = acDataProcessor.generalizeData(files);
		
		for ( Integer docId : mapper.getFromDocIdToActorsConceptsLinks().keySet() ) {
			List<DNAToken> docTokens = mapper.getFromDocIdToDocTokens().get(docId);
			Map<Integer, Set<Integer>> acLinks = mapper.getFromDocIdToActorsConceptsLinks().get(docId);
			for ( int actorTokenIndex : acLinks.keySet() ) {
				System.out.println( "Actor: " + docTokens.get(actorTokenIndex) );
				System.out.println( "Linked to the following concept tokens:" );
				for( int conceptTokenIndex : acLinks.get(actorTokenIndex) ) {
					System.out.print("[" + docTokens.get(conceptTokenIndex) + "]");
				}
				System.out.println("\n");
			}
			
		}
		
	}
	
	/**
	 * The tokenizer to be used to tokenize the documents' text.
	 */
	private DNATokenizer tokenzier;
	
	/**
	 * 
	 * @param tokenzier The tokenizer to be used to tokenize the documents' text.
	 */
	public ActorConceptDataPreprocessor( DNATokenizer tokenzier ) {
		this.tokenzier = tokenzier;
	}
	
	
	/**
	 * This method converts a set of DNA files/documents into an abstract representation that 
	 * helps to find the concepts that each actor is talking about in the document.  
	 * 
	 * @param files
	 * @return {@link ActorConceptMapper}
	 */
	public ActorConceptMapper generalizeData( List<File> files ) {
		
		ActorConceptMapper acMapper = new ActorConceptMapper();
		Map<Integer, List<DNAToken>> fromDocIdToDocTokens = new HashMap<Integer, List<DNAToken>>();
		acMapper.setFromDocIdToDocTokens(fromDocIdToDocTokens);
		
		for ( File file : files ) {
			DataAccess dataAccess = new DataAccess("sqlite", file.getAbsolutePath() );
			List<Document> documentsList = dataAccess.getDocuments();
			
			for ( int c = 0; c < documentsList.size(); c++ ) {
				Map<Integer, Set<Integer>> actorConceptLinks = new
						HashMap<Integer, Set<Integer>>();
				acMapper.getFromDocIdToActorsConceptsLinks().put(c, actorConceptLinks);
				Document document = documentsList.get(c);
				String docString = document.getText();
				List<DNAToken> docTokens = tokenzier.tokenize(0, docString);
				
				for (DNAToken docToken : docTokens) {
					docToken.setInternalDocId(c);
				}
				
				DNATextMiner.giveLabels(docTokens, "Normal");
				fromDocIdToDocTokens.put( c, docTokens );
				//The purpose of the next hash table is to map from a token's start position
				//in the document's text to the token's index in the docTokens list.
				Map<Integer, Integer> fromTokenStartPostitionToTokenIndex = new
						HashMap<Integer, Integer>();
				for ( int j = 0; j < docTokens.size(); j++ ) {
					DNAToken token = docTokens.get(j);
					fromTokenStartPostitionToTokenIndex.put(token.getStart_position(), j);
				}
				
				List<SidebarStatement> approvedStatements = new ArrayList<SidebarStatement>();
				List<SidebarStatement> actorStatements = new ArrayList<SidebarStatement>();
				List<SidebarStatement> conceptStatements = new ArrayList<SidebarStatement>();
				
				List<SidebarStatement> statements = 
						dataAccess.getStatementsPerDocumentId(document.getId());
				
				//Categorize the statements according to their types (i.e put them in separate lists).
				for (SidebarStatement st : statements) {
					if (st.getType().equals("ApprovedStatement")) {
						approvedStatements.add(st);
					}
					else if (st.getType().equals("Person") || st.getType().equals("Organization")) {
						actorStatements.add(st);
					}
					else if (st.getType().equals("Concept")) {
						conceptStatements.add(st);
					}
				}
				
				removeInnerStatements(actorStatements);
				removeInnerStatements(conceptStatements);
				
				//Find the actors and concepts within each ApprovedStatement
				for ( SidebarStatement approvedStatement : approvedStatements ) {
					List<SidebarStatement> approvedStatementActors = new ArrayList<SidebarStatement>();
					List<SidebarStatement> approvedStatementConcepts = new ArrayList<SidebarStatement>();
					for ( SidebarStatement actorStatement : actorStatements ) {
						if ( actorStatement.getStart() >= approvedStatement.getStart() &&
								actorStatement.getStop() <= approvedStatement.getStop() ) {
							approvedStatementActors.add(actorStatement);
						}
					}
					
					for ( SidebarStatement conceptStatement : conceptStatements ) {
						if ( conceptStatement.getStart() >= approvedStatement.getStart() &&
								conceptStatement.getStop() <= approvedStatement.getStop() ) {
							approvedStatementConcepts.add(conceptStatement);
						}
					}
					
					//Tokenize the ApprovedStatement's text and label the tokens as either Actor, Concept,
					//or Normal. Of course, only do that to an ApprovedStatement that has at least one actor
					//and one concept.
					//
					//Yes I'm aware that the following approach is unnecessary complicated,
					//since we have the Actor's and Concept's statements positions and we can use 
					//String.substring() method, but I need this approach (for a possible usage in the future)
					//to find the unhighlighted parts in the ApprovedStatement itself. 
					if ( approvedStatementActors.size() > 0 && approvedStatementConcepts.size() > 0 ) {
						Map<Integer, Integer> actorsPositions = new HashMap<Integer, Integer>();
						Map<Integer, Integer> conceptsPositions = new HashMap<Integer, Integer>();
						Map<Integer, Integer> nonHighlightedTextPositions = new HashMap<Integer, Integer>();
						
						//Put the positions of the actors in a hashtable to be able to find their tokens
						for ( SidebarStatement statement : approvedStatementActors ) {
							actorsPositions.put(statement.getStart(), statement.getStop());
						}
						
						//Put the positions of the concepts in a hashtable to be able to find their tokens
						for ( SidebarStatement statement : approvedStatementConcepts ) {
							conceptsPositions.put(statement.getStart(), statement.getStop());
						}
						
						//Find which part of the ApprovedStatement's text is Actor, Concept or Normal.
						List<Integer> highlightedTextStartPositions = new ArrayList<Integer>();
						highlightedTextStartPositions.addAll( actorsPositions.keySet() );
						highlightedTextStartPositions.addAll( conceptsPositions.keySet() );
						Collections.sort( highlightedTextStartPositions );
						
						for ( int i = 0; i < highlightedTextStartPositions.size() - 1; i++ ) {
							int startPosition = highlightedTextStartPositions.get(i);
							int endPosition;
							if ( actorsPositions.containsKey(startPosition) ) {
								endPosition = actorsPositions.get(startPosition);
							}
							else {
								endPosition = conceptsPositions.get(startPosition);
							}
							
							int subsequentStartPosition = highlightedTextStartPositions.get(i+1);
							nonHighlightedTextPositions.put(endPosition, subsequentStartPosition);
						}
						
						//Check for the part before the first highlighted text segment
						if ( highlightedTextStartPositions.get(0) != approvedStatement.getStart() ) {
							int nonHighlightedTextStartPosition = approvedStatement.getStart();
							int nonHighlightedTextEndPosition = highlightedTextStartPositions.get(0);
							nonHighlightedTextPositions.put(nonHighlightedTextStartPosition, nonHighlightedTextEndPosition);
						}
						
						//Check for the part after the last highlighted text segment
						int lastHighlightedTextStartPosition = highlightedTextStartPositions.get(
																highlightedTextStartPositions.size()-1  );
						int lastHighlightedTextEndPosition;
						if ( actorsPositions.containsKey(lastHighlightedTextStartPosition) ) {
							lastHighlightedTextEndPosition = actorsPositions.get(lastHighlightedTextStartPosition);
						}
						else {
							lastHighlightedTextEndPosition = conceptsPositions.get(lastHighlightedTextStartPosition);
						}
						
						if ( lastHighlightedTextEndPosition != approvedStatement.getStop() ) {
							int lastNonHighlightedTextStartPosition = lastHighlightedTextEndPosition;
							int lastNonHighlightedTextEndPosition = approvedStatement.getStop();
							nonHighlightedTextPositions.put(lastNonHighlightedTextStartPosition,
									lastNonHighlightedTextEndPosition);
						}
						
						//Tokenization 
						List<DNAToken> statementTokens = new ArrayList<DNAToken>();
						
						for ( Integer start : actorsPositions.keySet() ) {
							int end = actorsPositions.get(start);
							
							List<DNAToken> temp_tokens = tokenzier.tokenize(start,
									docString.substring(start, end));
							DNATextMiner.giveLabels(temp_tokens, "Actor");
							for ( DNAToken actorToken : temp_tokens ) {
								int tokenIndex = fromTokenStartPostitionToTokenIndex.get( 
													actorToken.getStart_position() );
								actorConceptLinks.put( tokenIndex, new HashSet<Integer>() );
							}
							statementTokens.addAll(temp_tokens);
						}
						
						for ( Integer start : conceptsPositions.keySet() ) {
							int end = conceptsPositions.get(start);
							
							List<DNAToken> temp_tokens = tokenzier.tokenize(start,
									docString.substring(start, end));
							DNATextMiner.giveLabels(temp_tokens, "Concept");
							
							//links each actor token in this ApprovedStatement to every
							//concept token in this ApprovedStatement.
							for ( DNAToken conceptToken : temp_tokens ) {
								int conceptTokenIndex = fromTokenStartPostitionToTokenIndex.get( 
										conceptToken.getStart_position() );
								for ( Integer actorTokenIndex : actorConceptLinks.keySet() ) {
									Set<Integer> conceptTokensIndices = actorConceptLinks.get(actorTokenIndex);
									conceptTokensIndices.add(conceptTokenIndex);
								}
							}
							
							statementTokens.addAll(temp_tokens);
						}
						
						for ( Integer start : nonHighlightedTextPositions.keySet() ) {
							int end = nonHighlightedTextPositions.get(start);
							
							List<DNAToken> temp_tokens = tokenzier.tokenize(start,
									docString.substring(start, end));
							DNATextMiner.giveLabels(temp_tokens, "Normal");
							statementTokens.addAll(temp_tokens);
						}
						
						for ( DNAToken token : statementTokens ) {
							int tokenIndex = fromTokenStartPostitionToTokenIndex.get( token.getStart_position() );
							docTokens.get(tokenIndex).setLabel(token.getLabel());
						}
						
						acMapper.getFromDocIdToActorsConceptsLinks().put(c, actorConceptLinks);
					}
				}
				
			}
		}
		
		
		return acMapper;
	}

	/**
	 * Removes the statements that fall in a larger statement range. In other words,
	 * if a statement is highlighted and is part of a wider highlighted statement from the same category,
	 * then only the wider statement is used. For example, the statement "Mr.X" can be highlighted 
	 * as a Person and the statement "The minister of defense Mr.X" is also highlighted as a statement.
	 * This methods removes the extra "Mr.X" statement because its tokens are redundant.
	 * 
	 * @param statements a list of statements that should be filtered away from redundant statements.
	 */
	public static void removeInnerStatements( List<SidebarStatement> statements ) {

		List<Integer> toBeRemoved = new ArrayList<Integer>();
		
		for ( int i = 0; i < statements.size(); i++ ) {
			SidebarStatement s1 = statements.get(i);
			
			for ( int j = 0; j < statements.size(); j++ ) {
				SidebarStatement s2 = statements.get(j);
				
				if ( ( s1.getStart() >= s2.getStart() && s1.getStop() <= s2.getStop() )
						&& ( s1.getStart() != s2.getStart() || s1.getStop() != s2.getStop() ) ) {
					toBeRemoved.add( i );
				}
				
			}
		}

		Collections.sort(toBeRemoved, Collections.reverseOrder());
		
		for ( int index : toBeRemoved ) {
			statements.remove(index);
		}
		
	}
	
}

























