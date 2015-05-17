package dna.textmining;


public class JSATClassifierTest {

//	@Test
//	public void allPredictionsShouldBeCorrect() {
//		System.out.println( "Testing classifier acccuracy" );
//		
////		LinearSGD linearsgd = new LinearSGD(new HingeLoss(), 0, 0);
////        linearsgd.setUseBias(true);
////        GradientUpdater gu = new SimpleSGD();
////        linearsgd.setGradientUpdater(gu);
//        
//		Classifier jsatClf = new LogisticRegressionDCD();
//		
//		DNAClassifier clf = new JSATClassifier(jsatClf);
//		Map<String, List<Sample>> datasets1 = DNAClassifierTest.getDataset("Iris-setosa");
//		Map<String, List<Sample>> datasets2 = DNAClassifierTest.getDataset("Iris-versicolor");
//		Map<String, List<Sample>> datasets3 = DNAClassifierTest.getDataset("Iris-virginica");
//
//		for ( Sample s : datasets1.get("train") ) {
//			clf.updateData(s.vec, s.label);
//		}
//		
//		clf.updateClassifier();
//		
//		for ( Sample s : datasets1.get("test") ) {
////			assertEquals( clf.classifyInstance(s.vec), s.label );
//			System.out.println( clf.classifyInstance(s.vec) + " vs " + s.label );
//		}
//		
////		clf = new JSATClassifier(linearsgd);
////
////		for ( Sample s : datasets2.get("train") ) {
////			clf.updateData(s.vec, s.label);
////		}
////		
////		clf.updateClassifier();
////		
////		for ( Sample s : datasets2.get("test") ) {
////			assertEquals( clf.classifyInstance(s.vec), s.label );
////		}
////		
////		clf = new JSATClassifier(linearsgd);
////
////		for ( Sample s : datasets3.get("train") ) {
////			clf.updateData(s.vec, s.label);
////		}
////		
////		clf.updateClassifier();
////		
////		for ( Sample s : datasets3.get("test") ) {
////			assertEquals( clf.classifyInstance(s.vec), s.label );
////		}
//		
//	}
//
////	@Test
//	public void shouldNotTakeLong() {
//		System.out.println( "Testing classifier runtime performance" );
//		LinearSGD linearsgd = new LinearSGD(new HingeLoss(), 1e-4, 1e-5);
//        linearsgd.setUseBias(true);
//        GradientUpdater gu = new SimpleSGD();
//        linearsgd.setGradientUpdater(gu);
//		
//		DNAClassifier clf = new JSATClassifier(linearsgd);
//
//		int numberOfFeatures = 20000;
//		int numb_samples = 10000;
//		Random rnd = new Random(0);
//
//		//Creating artificial positive samples
//		for ( int i = 0; i < numb_samples/2; i++ ) {
//			double[] vec = new double[numberOfFeatures];
//
//			vec[rnd.nextInt(numberOfFeatures)] = 1.0;
//			vec[rnd.nextInt(numberOfFeatures)] = 1.0;
//			vec[rnd.nextInt(numberOfFeatures)] = 1.0;
//			vec[rnd.nextInt(numberOfFeatures)] = 1.0;
//			vec[rnd.nextInt(numberOfFeatures)] = 1.0;
//
//			clf.updateData(vec, DNAClassifier.POSITIVE_CLASS);
//		}
//
//		//Creating artificial negative samples
//		for ( int i = 0; i < numb_samples/2; i++ ) {
//			double[] vec = new double[numberOfFeatures];
//			
//			vec[rnd.nextInt(numberOfFeatures)] = 1.0;
//			vec[rnd.nextInt(numberOfFeatures)] = 1.0;
//			vec[rnd.nextInt(numberOfFeatures)] = 1.0;
//			vec[rnd.nextInt(numberOfFeatures)] = 1.0;
//			vec[rnd.nextInt(numberOfFeatures)] = 1.0;
//
//			clf.updateData(vec, DNAClassifier.NEGATIVE_CLASS);
//		}
//		clf.updateClassifier();
//	}

}
