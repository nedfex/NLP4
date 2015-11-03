package nlp.assignments;

import java.util.*;
import java.io.*;

import nlp.io.IOUtils;
import nlp.util.*;

/**
 * Harness for testing word-level alignments. The code is hard-wired for the
 * aligment source to be english and the alignment target to be french (recall
 * that's the direction for translating INTO english in the noisy channel
 * model).
 * 
 * Your projects will implement several methods of word-to-word alignment.
 */
public class WordAlignmentTester {

	static final String ENGLISH_EXTENSION = "e";
	static final String FRENCH_EXTENSION = "f";
	static int mode;
	static int max_iteration;

	/**
	 * A holder for a pair of sentences, each a list of strings. Sentences in
	 * the test sets have integer IDs, as well, which are used to retreive the
	 * gold standard alignments for those sentences.
	 */
	public static class SentencePair {
		int sentenceID;
		String sourceFile;
		List<String> englishWords;
		List<String> frenchWords;

		public int getSentenceID() {
			return sentenceID;
		}

		public String getSourceFile() {
			return sourceFile;
		}

		public List<String> getEnglishWords() {
			return englishWords;
		}

		public List<String> getFrenchWords() {
			return frenchWords;
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int englishPosition = 0; englishPosition < englishWords.size(); englishPosition++) {
				String englishWord = englishWords.get(englishPosition);
				sb.append(englishPosition);
				sb.append(":");
				sb.append(englishWord);
				sb.append(" ");
			}
			sb.append("\n");
			for (int frenchPosition = 0; frenchPosition < frenchWords.size(); frenchPosition++) {
				String frenchWord = frenchWords.get(frenchPosition);
				sb.append(frenchPosition);
				sb.append(":");
				sb.append(frenchWord);
				sb.append(" ");
			}
			sb.append("\n");
			return sb.toString();
		}

		public SentencePair(int sentenceID, String sourceFile,
				List<String> englishWords, List<String> frenchWords) {
			this.sentenceID = sentenceID;
			this.sourceFile = sourceFile;
			this.englishWords = englishWords;
			this.frenchWords = frenchWords;
		}
	}

	/**
	 * Alignments serve two purposes, both to indicate your system's guessed
	 * alignment, and to hold the gold standard alignments. Alignments map index
	 * pairs to one of three values, unaligned, possibly aligned, and surely
	 * aligned. Your alignment guesses should only contain sure and unaligned
	 * pairs, but the gold alignments contain possible pairs as well.
	 * 
	 * To build an alignemnt, start with an empty one and use
	 * addAlignment(i,j,true). To display one, use the render method.
	 */
	public static class Alignment {
		Set<Pair<Integer, Integer>> sureAlignments;
		Set<Pair<Integer, Integer>> possibleAlignments;

		public boolean containsSureAlignment(int englishPosition,
				int frenchPosition) {
			return sureAlignments.contains(new Pair<Integer, Integer>(
					englishPosition, frenchPosition));
		}

		public boolean containsPossibleAlignment(int englishPosition,
				int frenchPosition) {
			return possibleAlignments.contains(new Pair<Integer, Integer>(
					englishPosition, frenchPosition));
		}

		public void addAlignment(int englishPosition, int frenchPosition,
				boolean sure) {
			Pair<Integer, Integer> alignment = new Pair<Integer, Integer>(
					englishPosition, frenchPosition);
			if (sure)
				sureAlignments.add(alignment);
			possibleAlignments.add(alignment);
		}

		public Alignment() {
			sureAlignments = new HashSet<Pair<Integer, Integer>>();
			possibleAlignments = new HashSet<Pair<Integer, Integer>>();
		}

		public static String render(Alignment alignment,
				SentencePair sentencePair) {
			return render(alignment, alignment, sentencePair);
		}

		public static String render(Alignment reference, Alignment proposed,
				SentencePair sentencePair) {
			StringBuilder sb = new StringBuilder();
			for (int frenchPosition = 0; frenchPosition < sentencePair
					.getFrenchWords().size(); frenchPosition++) {
				for (int englishPosition = 0; englishPosition < sentencePair
						.getEnglishWords().size(); englishPosition++) {
					boolean sure = reference.containsSureAlignment(
							englishPosition, frenchPosition);
					boolean possible = reference.containsPossibleAlignment(
							englishPosition, frenchPosition);
					char proposedChar = ' ';
					if (proposed.containsSureAlignment(englishPosition,
							frenchPosition))
						proposedChar = '#';
					if (sure) {
						sb.append('[');
						sb.append(proposedChar);
						sb.append(']');
					} else {
						if (possible) {
							sb.append('(');
							sb.append(proposedChar);
							sb.append(')');
						} else {
							sb.append(' ');
							sb.append(proposedChar);
							sb.append(' ');
						}
					}
				}
				sb.append("| ");
				sb.append(sentencePair.getFrenchWords().get(frenchPosition));
				sb.append('\n');
			}
			for (int englishPosition = 0; englishPosition < sentencePair
					.getEnglishWords().size(); englishPosition++) {
				sb.append("---");
			}
			sb.append("'\n");
			boolean printed = true;
			int index = 0;
			while (printed) {
				printed = false;
				StringBuilder lineSB = new StringBuilder();
				for (int englishPosition = 0; englishPosition < sentencePair
						.getEnglishWords().size(); englishPosition++) {
					String englishWord = sentencePair.getEnglishWords().get(
							englishPosition);
					if (englishWord.length() > index) {
						printed = true;
						lineSB.append(' ');
						lineSB.append(englishWord.charAt(index));
						lineSB.append(' ');
					} else {
						lineSB.append("   ");
					}
				}
				index += 1;
				if (printed) {
					sb.append(lineSB);
					sb.append('\n');
				}
			}
			return sb.toString();
		}
	}

	/**
	 * WordAligners have one method: alignSentencePair, which takes a sentence
	 * pair and produces an alignment which specifies an english source for each
	 * french word which is not aligned to "null". Explicit alignment to
	 * position -1 is equivalent to alignment to "null".
	 */
	/*static interface WordAligner {
		Alignment alignSentencePair(SentencePair sentencePair);
		void train( List<SentencePair> trainingSentencePairs);
	}*/

	/**
	 * Simple alignment baseline which maps french positions to english
	 * positions. If the french sentence is longer, all final word map to null.
	 */
	/*static class BaselineWordAligner implements WordAligner {
		public Alignment alignSentencePair(SentencePair sentencePair) {
			Alignment alignment = new Alignment();
			int numFrenchWords = sentencePair.getFrenchWords().size();
			int numEnglishWords = sentencePair.getEnglishWords().size();
			for (int frenchPosition = 0; frenchPosition < numFrenchWords; frenchPosition++) {
				int englishPosition = frenchPosition;
				if (englishPosition >= numEnglishWords)
					englishPosition = -1;
				alignment.addAlignment(englishPosition, frenchPosition, true);
			}
			return alignment;
		}
	}*/


	//static class IBM_model2WordAligner implements WordAligner{
	static class WordAligner {

		CounterMap<String, String> t;
		CounterMap<String,Integer> a;
		

		//static int max_iteration;
		static final double lowerBoundProbabiliy = 0.0001;


		public Alignment alignSentencePair(SentencePair sentencePair) {
			Alignment alignment = new Alignment();

			int numFrenchWords = sentencePair.getFrenchWords().size();
			int numEnglishWords = sentencePair.getEnglishWords().size();

			for (int j = 1; j < sentencePair.getEnglishWords().size(); j++) {
				String e = sentencePair.getEnglishWords().get(j);

				double  maxProb = 0.0;
				int bestFrenchPosition=0;
				for (int i = 1; i < sentencePair.getFrenchWords().size(); i++) {
					String f = sentencePair.getFrenchWords().get(i);

					String key = makeKey(numEnglishWords-1,numFrenchWords-1,j);
					if(!a.getCounter(key).containsKey(i)){
 						System.out.println("Zero warning :"+key+" : "+ j);			
 					}

					double prob;
					if(mode <=1)
						prob = t.getCounter(f).getCount(e) ;
					else
						prob = t.getCounter(f).getCount(e) * a.getCounter(key).getCount(i);

					if(prob>maxProb){
						maxProb = prob;
						bestFrenchPosition = i;
					}
				}
				alignment.addAlignment(j, bestFrenchPosition, true);
			}
			/*
			for (int i = 1; i < sentencePair.getFrenchWords().size(); i++) {
					String f = sentencePair.getFrenchWords().get(i);
		
				double  maxProb = 0.0;
				int bestEnglishPosition=0;

				//System.out.println(t.getCounter(e).toString());
				for (int j = 0; j < sentencePair.getEnglishWords().size(); j++) {
					String e = sentencePair.getEnglishWords().get(j);
				

					//if t(f|e) does not exist, just return a min proability
 					Double prob = ( t.getCounter(f).getCount(e) == 0 ? lowerBoundProbabiliy : t.getCounter(f).getCount(e));
 					
 					String key = makeKey(numEnglishWords-1,numFrenchWords-1,j);
 					Double a_prob = 0.0;

 					//System.out.println(key+" : "+a.getCounter(key).toString());
 					if(!a.getCounter(key).containsKey(i)){
 						System.out.println("Zero warning :"+key+" : "+ j);
 						a_prob = initialAlignmentWeight(numFrenchWords);				
 					}
 					else
 						a_prob = a.getCounter(key).getCount(i);

 					prob *= a_prob;
					//Double prob = t.getCounter(e).getCount(f);

					if(maxProb < prob){
						maxProb = prob;
						bestEnglishPosition = j;
					}
				}
				//int englishPosition = frenchPosition;
				//if (bestFrenchPosition >= numEnglishWords)
				//	bestFrenchPosition = -1;
				alignment.addAlignment(bestEnglishPosition, i, true);
			}
			*/
			/*
			for (int frenchPosition = 1; frenchPosition < numFrenchWords; frenchPosition++) {
				String f = sentencePair.getFrenchWords().get(frenchPosition);
				double  maxProb = 0.0;
				int bestenglishPosition=0;

				for (int englishPosition = 1; englishPosition < sentencePair.getEnglishWords().size(); englishPosition++){
					String e = sentencePair.getEnglishWords().get(englishPosition);
					if(maxProb < t.getCounter(e).getCount(f)){
						maxProb = t.getCounter(e).getCount(f);
						bestenglishPosition = englishPosition; 
					}
				}

				//int englishPosition = frenchPosition;
				if (bestenglishPosition >= numEnglishWords)
					bestenglishPosition = -1;
				alignment.addAlignment(bestenglishPosition, frenchPosition, true);
			}*/
			return alignment;
		}
		//public void IBM_model2WordAligner(){
		public WordAligner(){
			String mode = new String("IBM");
			t = new CounterMap<String,String>();	
			a = new CounterMap<String,Integer>();		
			//max_iteration = 1;
			if(mode.equals("heuristic")){
				//max_iteration = 1;
			}
			else if(mode.equals("IBM")){
				System.out.println("IBM Model 1 initialized");
				//max_iteration = 1;
			}
			else if(mode.equals("IBM mode2")){
				//max_iteration = 1;
			}
			System.out.println("IBM Model 1 initialized");
		}
		//makeKey(numFrenchWords,numEnglishWords-1,frenchPosition);
		//a(2|5,6,5) – the 5th target word is aligned to the 2nd source word.
		//a(i| j, le, lf ) , le target sentence length , lf : source sentence length
		public String makeKey(int le,int lf,int j){
			//return new String((j-1)+"_"+le+"_"+lf);
			return new String((j)+"_"+le+"_"+lf);
		}
		public double initialAlignmentWeight(int lf ){

			return 1.0/((double)lf+1); 
		}
		public double initialAlignmentWeight(SentencePair sentencePair ){

			//return 1.0/((double)lf); 
			return 1;
		}
		public void train( List<SentencePair> trainingSentencePairs){		

			for(int iter = 0;iter < max_iteration ;iter ++){

				System.out.println("Iter "+iter);
				Counter<String> s_total = new Counter<String>();
				Counter<String> total = new Counter<String>();
				CounterMap<String, String> count = new CounterMap<String,String>();

				for (SentencePair sentencePair : trainingSentencePairs){
					
					int numFrenchWords = sentencePair.getFrenchWords().size();
					int numEnglishWords = sentencePair.getEnglishWords().size();
					//computing normalization
					for (int j = 1; j < sentencePair.getEnglishWords().size(); j++) {
						String e = sentencePair.getEnglishWords().get(j);
						s_total.setCount( e ,0.0);

						for (int i = 0; i < sentencePair.getFrenchWords().size(); i++) {
							String f = sentencePair.getFrenchWords().get(i);
							if( !t.getCounter(f).containsKey(e))
								t.setCount(f, e, lowerBoundProbabiliy); 

							s_total.incrementCount(e, t.getCounter(f).getCount(e));
							//System.out.println(f+" : "+t.getCounter(e).getCount(f)+" = "+s_total.getCount(f));
						}
					}
					//collect counts , first loop is target language, should start at 0
					for (int j = 1; j < sentencePair.getEnglishWords().size(); j++) {
						String e = sentencePair.getEnglishWords().get(j);
						for (int i = 0; i < sentencePair.getFrenchWords().size(); i++) {
							String f = sentencePair.getFrenchWords().get(i);

							Double c = t.getCounter(f).getCount(e) / s_total.getCount(e) ;
							//System.out.println(c);
							count.incrementCount(f,e,c);
							total.incrementCount(f,c);

						}
					}
				}//end for all sentence 

				//estimate probability
				CounterMap<String,String> t_t = new CounterMap<String,String>();
				for(String f : t.keySet()){
					for(String e : t.getCounter(f).keySet()){
						t.setCount(f,e,count.getCounter(f).getCount(e) / (total.getCount(f)));
						if(t.getCounter(f).getCount(e) > 0.0001)
							t_t.setCount(f,e,t.getCounter(f).getCount(e));
					}
					//System.out.println(e+" : "+t.getCounter(e).toString());
				}
				t = t_t;			
			}//end for iteration
			System.out.println("Iter over");
			System.out.println(t);
		}//end for public train

		public void train2( List<SentencePair> trainingSentencePairs){		

			for(int iter = 0;iter < max_iteration ;iter ++){

				System.out.println("Iter2 "+iter);
				Counter<String> s_total = new Counter<String>();
				Counter<String> total = new Counter<String>();
				CounterMap<String, String> count = new CounterMap<String,String>();

				//IBM Model 2
				CounterMap<String, Integer> count_a = new CounterMap<String,Integer>();
				Counter<String>total_a = new Counter<String>();

				for (SentencePair sentencePair : trainingSentencePairs){
					
					int numFrenchWords = sentencePair.getFrenchWords().size();
					int numEnglishWords = sentencePair.getEnglishWords().size();


					/*String chenkKey = makeKey(numEnglishWords-1,numFrenchWords-1,0);
					if(!a.containsKey(chenkKey))
						for (int i = 0; i < sentencePair.getFrenchWords().size(); i++) {
							a.setCount(chenkKey,i,initialAlignmentWeight(numFrenchWords-1));
							System.out.println(a.getCounter(chenkKey).getCount(i));
						}
					*/

					//computing normalization
					for (int j = 1; j < sentencePair.getEnglishWords().size(); j++) {
						String e = sentencePair.getEnglishWords().get(j);
						s_total.setCount( e ,0.0);

						for (int i = 0; i < sentencePair.getFrenchWords().size(); i++) {
							String f = sentencePair.getFrenchWords().get(i);
							if( !t.getCounter(f).containsKey(e))
								t.setCount(f, e, lowerBoundProbabiliy); 

							String key = makeKey(numEnglishWords-1,numFrenchWords-1,j);
							if( !a.getCounter(key).containsKey(i))
								a.setCount(key,i,initialAlignmentWeight(numFrenchWords-1));

							s_total.incrementCount(e, t.getCounter(f).getCount(e) *  a.getCounter(key).getCount(i));
							//System.out.println(f+" : "+t.getCounter(e).getCount(f)+" = "+s_total.getCount(f));
						}
					}
					//collect counts , first loop is target language, should start at 0
					for (int j = 1 ; j< sentencePair.getEnglishWords().size(); j++) {
						String e = sentencePair.getEnglishWords().get(j);
						for (int i = 0; i < sentencePair.getFrenchWords().size(); i++) {
							String f = sentencePair.getFrenchWords().get(i);

							String key = makeKey(numEnglishWords-1,numFrenchWords-1,j);
							Double c = t.getCounter(f).getCount(e) * a.getCounter(key).getCount(i) / s_total.getCount(e) ;
							//System.out.println(c);
							count.incrementCount(f,e,c);
							total.incrementCount(f,c);
							count_a.incrementCount(key,i,c);
							total_a.incrementCount(key,c); 

						}
					}
				}//end for all sentence 

				//CounterMap<String,String> t_t = new CounterMap<String,String>();
				//CounterMap<String,Integer> t_a = new CounterMap<String,Integer>();
				//estimate probability
				for(String f : t.keySet()){
					for(String e : t.getCounter(f).keySet()){
						t.setCount(f,e,count.getCounter(f).getCount(e) / (total.getCount(f)));
						//if(t.getCounter(f).getCount(e) > 0.0001)
						//	t_t.setCount(f,e,t.getCounter(f).getCount(e));
					}
					//System.out.println(e+" : "+t.getCounter(e).toString());
				}
				for(String key : a.keySet()){

					//if(total_a.getCount(key)==0.0)
					//	continue;
					for(int i : a.getCounter(key).keySet()){
						a.setCount(key,i,count_a.getCounter(key).getCount(i) / (total_a.getCount(key)));
						//if(a.getCounter(key).getCount(i) > 0.0001)
						//	t_a.setCount(key,i,a.getCounter(key).getCount(i));
					}
					//System.out.println(e+" : "+t.getCounter(e).toString());
				}
				//a = t_a;
				//t = t_t;
				//t = Counters.conditionalNormalize(t);
				//a = Counters.conditionalNormalize(a);

			}//end for iteration
			System.out.println("Iter2 over");
			//System.out.println(t);
			//System.out.println(a);
		}//end for public train2
	}//end for class WordAlinger



	public static void main(String[] args) {
		// Parse command line flags and arguments
		Map<String, String> argMap = CommandLineUtils
				.simpleCommandLineParser(args);

		// Set up default parameters and settings
		String basePath = ".";
		int maxTrainingSentences = 1000;
		boolean verbose = false;
		String dataset = "mini";
		String model = "baseline";
		max_iteration = 10;

		// Update defaults using command line specifications
		if (argMap.containsKey("-path")) {
			basePath = argMap.get("-path");
			System.out.println("Using base path: " + basePath);
		}
		if (argMap.containsKey("-sentences")) {
			maxTrainingSentences = Integer.parseInt(argMap.get("-sentences"));
			System.out.println("Using an additional " + maxTrainingSentences
					+ " training sentences.");
		}
		if (argMap.containsKey("-data")) {
			dataset = argMap.get("-data");
			System.out.println("Running with data: " + dataset);
		} else {
			System.out
					.println("No data set specified.  Use -data [miniTest, validate, test].");
		}
		if (argMap.containsKey("-model")) {
			model = argMap.get("-model");
			System.out.println("Running with model: " + model);
		} else {
			System.out.println("No model specified.  Use -model modelname.");
		}
		if (argMap.containsKey("-verbose")) {
			verbose = true;
		}
		if (argMap.containsKey("-iter")) {
			max_iteration = Integer.parseInt(argMap.get("-iter"));
		}

		// Read appropriate training and testing sets.
		List<SentencePair> trainingSentencePairs = new ArrayList<SentencePair>();
		if (!dataset.equals("miniTest") && maxTrainingSentences > 0){
			System.out.println("loading training samples");
			trainingSentencePairs = readSentencePairs(basePath + "/training",
					maxTrainingSentences);
		}
		List<SentencePair> testSentencePairs = new ArrayList<SentencePair>();
		Map<Integer, Alignment> testAlignments = new HashMap<Integer, Alignment>();
		if (dataset.equalsIgnoreCase("test")) {
			testSentencePairs = readSentencePairs(basePath + "/test",
					Integer.MAX_VALUE);
			testAlignments = readAlignments(basePath
					+ "/answers/test.wa.nonullalign");
		} else if (dataset.equalsIgnoreCase("validate")) {
			testSentencePairs = readSentencePairs(basePath + "/trial",
					Integer.MAX_VALUE);
			testAlignments = readAlignments(basePath + "/trial/trial.wa");
		} else if (dataset.equalsIgnoreCase("miniTest")) {
			testSentencePairs = readSentencePairs(basePath + "/mini",
					Integer.MAX_VALUE);
			testAlignments = readAlignments(basePath + "/mini/mini.wa");
		} else {
			throw new RuntimeException("Bad data set mode: " + dataset
					+ ", use test, validate, or miniTest.");
		}
		trainingSentencePairs.addAll(testSentencePairs);

		// Build model
		WordAligner wordAligner = null;
		if (model.equalsIgnoreCase("baseline")) {
			//wordAligner = new BaselineWordAligner();
			//wordAligner = new IBM_model2WordAligner();
		}
		else if(model.equals("IBM1")){
			System.out.println("IBM1");
			mode =1;
			//wordAligner = new IBM_model2WordAligner();
			wordAligner = new WordAligner();
			wordAligner.train(trainingSentencePairs);//IBM model 1
			//wordAligner.train2(trainingSentencePairs); // IBM model 2
			System.out.println("Training contains "+trainingSentencePairs.size());
		}else if(model.equals("IBM2")){
			System.out.println("IBM2");
			mode =2;
			//wordAligner = new IBM_model2WordAligner();
			wordAligner = new WordAligner();
			wordAligner.train(trainingSentencePairs);//IBM model 1
			wordAligner.train2(trainingSentencePairs); // IBM model 2
			System.out.println("Training contains "+trainingSentencePairs.size());
		}
		// TODO : build other alignment models

		// Test model
		test(wordAligner, testSentencePairs, testAlignments, verbose);
	}

	private static void test(WordAligner wordAligner,
			List<SentencePair> testSentencePairs,
			Map<Integer, Alignment> testAlignments, boolean verbose) {
		int proposedSureCount = 0;
		int proposedPossibleCount = 0;
		int sureCount = 0;
		int proposedCount = 0;
		for (SentencePair sentencePair : testSentencePairs) {
			Alignment proposedAlignment = wordAligner
					.alignSentencePair(sentencePair);
			Alignment referenceAlignment = testAlignments.get(sentencePair
					.getSentenceID());
			if (referenceAlignment == null)
				throw new RuntimeException(
						"No reference alignment found for sentenceID "
								+ sentencePair.getSentenceID());
			if (verbose)
				System.out.println("Alignment:\n"
						+ Alignment.render(referenceAlignment,
								proposedAlignment, sentencePair));
			for (int frenchPosition = 0; frenchPosition < sentencePair
					.getFrenchWords().size(); frenchPosition++) {
				for (int englishPosition = 0; englishPosition < sentencePair
						.getEnglishWords().size(); englishPosition++) {
					boolean proposed = proposedAlignment.containsSureAlignment(
							englishPosition, frenchPosition);
					boolean sure = referenceAlignment.containsSureAlignment(
							englishPosition, frenchPosition);
					boolean possible = referenceAlignment
							.containsPossibleAlignment(englishPosition,
									frenchPosition);
					if (proposed && sure)
						proposedSureCount += 1;
					if (proposed && possible)
						proposedPossibleCount += 1;
					if (proposed)
						proposedCount += 1;
					if (sure)
						sureCount += 1;
				}
			}
		}
		System.out.println("Precision: " + proposedPossibleCount
				/ (double) proposedCount);
		System.out.println("Recall: " + proposedSureCount / (double) sureCount);
		System.out.println("AER: "
				+ (1.0 - (proposedSureCount + proposedPossibleCount)
						/ (double) (sureCount + proposedCount)));
	}

	// BELOW HERE IS IO CODE

	private static Map<Integer, Alignment> readAlignments(String fileName) {
		Map<Integer, Alignment> alignments = new HashMap<Integer, Alignment>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			try {
				while (in.ready()) {
					String line = in.readLine();
					String[] words = line.split("\\s+");
					if (words.length != 4)
						throw new RuntimeException("Bad alignment file "
								+ fileName + ", bad line was " + line);
					Integer sentenceID = Integer.parseInt(words[0]);
					Integer englishPosition = Integer.parseInt(words[1]) - 1 +1 ; //+1 to avoid null
					Integer frenchPosition = Integer.parseInt(words[2]) - 1 +1 ; //+1 to avoid null
					String type = words[3];
					Alignment alignment = alignments.get(sentenceID);
					if (alignment == null) {
						alignment = new Alignment();
						alignments.put(sentenceID, alignment);
					}
					alignment.addAlignment(englishPosition, frenchPosition,
							type.equals("S"));
				}
			} finally {
				in.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return alignments;
	}

	private static List<SentencePair> readSentencePairs(String path,
			int maxSentencePairs) {
		List<SentencePair> sentencePairs = new ArrayList<SentencePair>();
		List<String> baseFileNames = getBaseFileNames(path);
		for (String baseFileName : baseFileNames) {
			System.out.println(baseFileName);
			if (sentencePairs.size() >= maxSentencePairs)
				continue;
			
			sentencePairs.addAll(readSentencePairs(baseFileName));
		}
		return sentencePairs;
	}

	private static List<SentencePair> readSentencePairs(String baseFileName) {
		List<SentencePair> sentencePairs = new ArrayList<SentencePair>();
		String englishFileName = baseFileName + "." + ENGLISH_EXTENSION;
		String frenchFileName = baseFileName + "." + FRENCH_EXTENSION;
		try {
			BufferedReader englishIn = new BufferedReader(new FileReader(
					englishFileName));
			BufferedReader frenchIn = new BufferedReader(new FileReader(
					frenchFileName));
			//BufferedReader frenchIn = new BufferedReader(new InputStreamReader(
                      //new FileInputStream(frenchFileName), "UTF8"));
			try {
				while (englishIn.ready() && frenchIn.ready()) {
					String englishLine = englishIn.readLine();
					String frenchLine = frenchIn.readLine();
					Pair<Integer, List<String>> englishSentenceAndID = readSentence(englishLine);
					Pair<Integer, List<String>> frenchSentenceAndID = readSentence(frenchLine);
					if (!englishSentenceAndID.getFirst().equals(
							frenchSentenceAndID.getFirst()))
						throw new RuntimeException(
								"Sentence ID confusion in file " + baseFileName
										+ ", lines were:\n\t" + englishLine
										+ "\n\t" + frenchLine);
					sentencePairs.add(new SentencePair(englishSentenceAndID.getFirst(), baseFileName, englishSentenceAndID.getSecond(), frenchSentenceAndID.getSecond()));
					//swap
					//sentencePairs.add(new SentencePair(englishSentenceAndID.getFirst(), baseFileName, frenchSentenceAndID.getSecond(), englishSentenceAndID.getSecond()));
				}
			} finally {
				englishIn.close();
				frenchIn.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return sentencePairs;
	}

	private static Pair<Integer, List<String>> readSentence(String line) {
		int id = -1;
		List<String> words = new ArrayList<String>();

		words.add(new String("<NULL>"));

		String[] tokens = line.split("\\s+");
		for (int i = 0; i < tokens.length; i++) {
			String token = tokens[i];
			if (token.equals("<s"))
				continue;
			if (token.equals("</s>"))
				continue;
			if (token.startsWith("snum=")) {
				String idString = token.substring(5, token.length() - 1);
				id = Integer.parseInt(idString);
				continue;
			}
			words.add(token.intern());
		}
		return new Pair<Integer, List<String>>(id, words);
	}

	private static List<String> getBaseFileNames(String path) {
		List<File> englishFiles = IOUtils.getFilesUnder(path, new FileFilter() {
			public boolean accept(File pathname) {
				if (pathname.isDirectory())
					return true;
				String name = pathname.getName();
				return name.endsWith(ENGLISH_EXTENSION);
			}
		});
		List<String> baseFileNames = new ArrayList<String>();
		for (File englishFile : englishFiles) {
			String baseFileName = chop(englishFile.getAbsolutePath(), "."
					+ ENGLISH_EXTENSION);
			if(baseFileName.contains(".DS_Store"))
				continue;
			baseFileNames.add(baseFileName);
		}
		return baseFileNames;
	}

	private static String chop(String name, String extension) {
		if (!name.endsWith(extension))
			return name;
		return name.substring(0, name.length() - extension.length());
	}

}
