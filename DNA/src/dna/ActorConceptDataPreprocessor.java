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
		System.out.println("Started Preprocessor");
		
		String file1 = "/Users/rakandirbas/Desktop/dna_files/s0.dna";
		List<File> files = new ArrayList<File>();
		files.add(new File(file1));
		
		ActorConceptDataPreprocessor acDataProcessor = new ActorConceptDataPreprocessor(new StanfordDNATokenizer());
		ActorConceptMapper mapper = acDataProcessor.generalizeData(files);
		
		System.out.println("END");
		for ( Integer docId : mapper.getFromDocIdToActorsConceptsLinks().keySet() ) {
			System.out.println("DocID" + docId);
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
		int internalDocID = 0;
		for ( File file : files ) {
			DataAccess dataAccess = new DataAccess("sqlite", file.getAbsolutePath() );
			List<Document> documentsList = dataAccess.getDocuments();
			
			for ( Document document : documentsList ) {
				Map<Integer, Set<Integer>> actorConceptLinks = new
						HashMap<Integer, Set<Integer>>();
				acMapper.getFromDocIdToActorsConceptsLinks().put(internalDocID, actorConceptLinks);
				String docString = document.getText();
				List<DNAToken> docTokens = tokenzier.tokenize(0, docString);
				
				for (int i = 0; i < docTokens.size(); i++) {
					DNAToken docToken = docTokens.get(i);
					docToken.setInternalDocId(internalDocID);
					docToken.setIndex(i);
				}
				
				DNATextMiner.giveLabels(docTokens, "Normal");
				fromDocIdToDocTokens.put( internalDocID, docTokens );
				
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
				
				Map<SidebarStatement, Set<SidebarStatement>> fromActorStatementToConceptStatements = 
						new HashMap<SidebarStatement, Set<SidebarStatement>>();
				Map<Integer, Set<SidebarStatement>> fromActorTokenIndexToConceptStatements = 
						new HashMap<Integer, Set<SidebarStatement>>();
				
				List<SidebarStatement> allApprovedStatementsActors = new
						ArrayList<SidebarStatement>();
				List<SidebarStatement> allApprovedStatementsConcepts = 
						new ArrayList<SidebarStatement>();
				
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
					
					if ( approvedStatementActors.size() > 0 && approvedStatementConcepts.size() > 0 ) {
						for ( SidebarStatement approvedStatementActor : approvedStatementActors ) {
							fromActorStatementToConceptStatements.put(approvedStatementActor, 
									new HashSet<SidebarStatement>());
							
							for ( SidebarStatement approvedStatementConcept : approvedStatementConcepts ) {
								fromActorStatementToConceptStatements
									.get(approvedStatementActor).add(approvedStatementConcept);
							}
						}
						
						allApprovedStatementsActors.addAll(approvedStatementActors);
						allApprovedStatementsConcepts.addAll(approvedStatementConcepts);
					}
					
				}//for each approved statement
				
				List<Integer> actorTokensIndices = new ArrayList<Integer>();
				List<Integer> conceptTokensIndices = new ArrayList<Integer>();
				
				//Mark doc tokens as either Actor or Concept
				for (int i = 0; i < docTokens.size(); i++) {
					DNAToken docToken = docTokens.get(i);
					
					for ( SidebarStatement approvedStatementActor : allApprovedStatementsActors ) {
						if ( docToken.getStart_position() >= approvedStatementActor.getStart() &&
							 docToken.getEnd_position() <= approvedStatementActor.getStop() ) {
							docToken.setLabel("Actor");
							Set<SidebarStatement> actorConcepts = 
									fromActorStatementToConceptStatements.get(approvedStatementActor);
							fromActorTokenIndexToConceptStatements.put(docToken.getIndex(), actorConcepts);
							actorTokensIndices.add(docToken.getIndex());
						}
					}
					
					for ( SidebarStatement approvedStatementConcept : allApprovedStatementsConcepts ) {
						if ( docToken.getStart_position() >= approvedStatementConcept.getStart() &&
							 docToken.getEnd_position() <= approvedStatementConcept.getStop() ) {
							if(docToken.getLabel().equals("Actor")) {
								throw new RuntimeException("The doc token was set previosuly as an Actor token!");
							}
							else {
								docToken.setLabel("Concept");
								conceptTokensIndices.add(docToken.getIndex());
							}
						}
					}
				}//for each doc token
				
				//Construct the actor-concept links
				for ( int actorTokenIndex : actorTokensIndices ) {
					actorConceptLinks.put(actorTokenIndex, new HashSet<Integer>());
					Set<SidebarStatement> actorConcepts = 
							fromActorTokenIndexToConceptStatements.get(actorTokenIndex);
					
					for( int conceptTokenIndex : conceptTokensIndices ) {
						DNAToken conceptToken = docTokens.get(conceptTokenIndex);
						for ( SidebarStatement actorConceptStatement : actorConcepts ) {
							if ( conceptToken.getStart_position() >= actorConceptStatement.getStart() &&
							 conceptToken.getEnd_position() <= actorConceptStatement.getStop() ) {
								actorConceptLinks.get(actorTokenIndex).add(conceptTokenIndex);
							}
						}
					}
				}//for each actor token index
				
				System.out.println("procced doc: " + internalDocID);
				internalDocID++;
			}//for each document
		}//for each file
		
		
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

























