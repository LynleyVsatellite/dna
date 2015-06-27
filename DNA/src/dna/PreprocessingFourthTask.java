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
					//TODO filter away interconnected highlighted text!!!
					actorStatements.add(st);
				}
				else if (st.getType().equals("Concept")) {
					conceptStatements.add(st);
				}
			}
			
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
					
					for ( int i = 0; i < highlightedTextStartPositions.size(); i++ ) {
						int startPosition = highlightedTextStartPositions.get(i);
						
						if ( startPosition != 0 ) {
							//DO something
						}
						
						int endPosition;
						if ( actorsPositions.containsKey(startPosition) ) {
							endPosition = actorsPositions.get(startPosition);
						}
						else {
							endPosition = conceptsPositions.get(startPosition);
						}
						
					}
					
				}
			}
		}
		
	}
	
}

























