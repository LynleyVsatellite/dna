package dna;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreprocessingFourthTask {

	public static void main(String[] args) {
		System.out.println("Started");
		
		String file1 = "/Users/rockyrock/Desktop/s0_copy.dna";
		
		DataAccess dataAccess = new DataAccess("sqlite", file1 );
		List<Document> documentsList = dataAccess.getDocuments();
		
		for ( Document document : documentsList ) {
			String docString = document.getText();
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
			
			//TODO ignore approved statements if there are no actor or concepts in them at all!
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
				//and one concept
				if ( approvedStatementActors.size() > 0 && approvedStatementConcepts.size() > 0 ) {
					StringBuffer buffer = new StringBuffer();
					Map<Integer, Integer> actorsPositions = new HashMap<Integer, Integer>();
					Map<Integer, Integer> conceptsPositions = new HashMap<Integer, Integer>();
					
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
					Map<Integer, Integer> nonHighlightedTextPositions = new HashMap<Integer, Integer>();
					
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
					
				}
			}
		}
		
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

		for ( int index : toBeRemoved ) {
			statements.remove(index);
		}
		
	}
	
}

























