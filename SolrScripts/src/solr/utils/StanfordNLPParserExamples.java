package solr.utils;

import java.io.StringReader;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.shiftreduce.ShiftReduceParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class StanfordNLPParserExamples {

	/** A logger for this class */
	// private static Redwood.RedwoodChannels log = Redwood.channels(ShiftReduceParser.class);

	/**
	 * Demonstrates how to first use the tagger, then use the ShiftReduceParser.
	 * Note that ShiftReduceParser will not work on untagged text.
	 *
	 * @author John Bauer
	 */
	public static void shiftReduceParserDemo(String text) {

		String modelPath = "edu/stanford/nlp/models/srparser/englishSR.ser.gz";

		String taggerPath = "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger";

		MaxentTagger tagger = new MaxentTagger(taggerPath);

		ShiftReduceParser model = ShiftReduceParser.loadModel(modelPath);

		DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(text));

		for (List<HasWord> sentence : tokenizer) {

			List<TaggedWord> tagged = tagger.tagSentence(sentence);

			Tree tree = model.apply(tagged);

			// log.info(tree);

			System.out.println(tree.toString());
		}
	}

	/**
	 * This is the "regular" Stanford Parser, however the Shif-Reduce is faster than this one. 
	 * 
	 */
	public static void parserDemo(String text) {

		Properties props = new Properties();
		
		props.put("annotators", "tokenize, ssplit, parse");
		
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		Annotation document = new Annotation(text);

		pipeline.annotate(document);

		List<CoreMap> sentences = document.get(SentencesAnnotation.class);

		for (CoreMap sentence : sentences) {

			Tree tree = sentence.get(TreeAnnotation.class);
			
			System.out.println(tree.toString());

		}
	}

	public static void main(String[] args) {
		
		String text = "You like it, don't you? Do you like me? Why did you do that?! You are good! Am I wrong? Are you going to the party?";
		
		shiftReduceParserDemo(text);
		
		parserDemo(text);

	}
}
