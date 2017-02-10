package solr.analysis;

import java.io.StringReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PropertiesUtils;
import solr.utils.Utils;

@SuppressWarnings("deprecation")
public class StanfordNLPTest {

	public static void parseNNDependencies() {

		String modelPath = DependencyParser.DEFAULT_MODEL;

		String taggerPath = "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger";

		MaxentTagger tagger = new MaxentTagger(taggerPath);

		DependencyParser parser = DependencyParser.loadFromModelFile(modelPath);

		// String text = "It's is a test... Why?! Wait, I will!!! This is a
		// T.L.A. test. Now with a Dr. in it.";
		String text = "does he play tennis?";

		List<String> sentences = Utils.splitParagraphIntoSentences(text);

		for (String sentence : sentences) {

			DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(sentence));

			for (List<HasWord> phrase : tokenizer) {

				List<TaggedWord> tagged = tagger.tagSentence(phrase);

				GrammaticalStructure gs = parser.predict(tagged);

				Collection<TypedDependency> tdl = gs.typedDependenciesCollapsed();

				for (Iterator<TypedDependency> iter = tdl.iterator(); iter.hasNext();) {
					TypedDependency var = iter.next();

					IndexedWord dep = var.dep();
					IndexedWord gov = var.gov();

					// All useful information for a node in the tree
					String reln = var.reln().getShortName();

					int depIdx = var.dep().index();
					int govIdx = var.gov().index();

					System.out.println(depIdx);
				}

				System.out.println(gs);
			}
		}
	}

	public static void parseCoreNLPNNDependencies() {

		String text = "It's is a test... Why?! Wait, I will!!! This is a T.L.A. test. Now with a Dr. in it.";

		// List<String> sentences = Utils.splitParagraphIntoSentences(text);

		// for (String sentence : sentences) {

		Annotation ann = new Annotation(text);

		Properties props = PropertiesUtils.asProperties("annotators", "tokenize,ssplit,pos,depparse", "depparse.model",
				DependencyParser.DEFAULT_MODEL);

		AnnotationPipeline pipeline = new StanfordCoreNLP(props);

		pipeline.annotate(ann);

		for (CoreMap sent : ann.get(CoreAnnotations.SentencesAnnotation.class)) {

			SemanticGraph sg = sent.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);

			System.out.println(IOUtils.eolChar + sg.toString(SemanticGraph.OutputFormat.LIST));
		}
		// }
	}

	public static void parseCodeNLPDemo() {

		String text = "It's is a test... Why?! Wait, I will!!! This is a T.L.A. test. Now with a Dr. in it.";

		Document doc = new Document(text);

		for (Sentence sent : doc.sentences()) {

			String t = sent.word(1);

			System.out.println("The second word of the sentence '" + sent + "' is " + t);

			String s = sent.lemma(2);

			System.out.println("The third lemma of the sentence '" + sent + "' is " + s);

			Tree p = sent.parse();

			System.out.println("The parse of the sentence '" + sent + "' is " + p);
		}
	}

	public static void parseSQClause() {

		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		String text = "Good I’m feeling rather tired. Bad It would be better to make a decision now, rather than leave it until later. I'd rather go.";

		Annotation document = new Annotation(text);

		pipeline.annotate(document);

		List<CoreMap> sentences = document.get(SentencesAnnotation.class);

		for (CoreMap sentence : sentences) {

			System.out.println("sentence: " + sentence);

			Tree tree = sentence.get(TreeAnnotation.class);
			System.out.println("parse tree: " + tree);

			Tree c = tree.getChild(0);
			System.out.println("root label: " + c.label());

		}
	}

	public static void parseTest() {

		// creates a StanfordCoreNLP object, with POS tagging, lemmatization,
		// NER, parsing, and coreference resolution
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		// read some text from the file..
		// String text = "It's is a test... Why?! Wait, I will!!! This is a
		// T.L.A. test. Now with a Dr. in it.";
		String text = "is it true? He does not like you! Why did you do that? Does he know it? Am I wrong?";

		// create an empty Annotation just with the given text
		Annotation document = new Annotation(text);

		// run all Annotators on this text
		pipeline.annotate(document);

		// these are all the sentences in this document
		// a CoreMap is essentially a Map that uses class objects as keys and
		// has values with custom types
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);

		for (CoreMap sentence : sentences) {
			// traversing the words in the current sentence
			// a CoreLabel is a CoreMap with additional token-specific methods
			// for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
			// // this is the text of the token
			// String word = token.get(TextAnnotation.class);
			// // this is the POS tag of the token
			// String pos = token.get(PartOfSpeechAnnotation.class);
			// // this is the NER label of the token
			// String ne = token.get(NamedEntityTagAnnotation.class);
			//
			// System.out.println("word: " + word + " pos: " + pos + " ne:" +
			// ne);
			// }

			System.out.println(sentence);

			// this is the parse tree of the current sentence
			Tree tree = sentence.get(TreeAnnotation.class);
			System.out.println("parse tree:\n" + tree);

			Tree c = tree.getChild(0);
			System.out.println("label: " + c.label());

			// this is the Stanford dependency graph of the current sentence
			SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
			System.out.println("dependency graph:\n" + dependencies);

		}

		// This is the coreference link graph
		// Each chain stores a set of mentions that link to each other,
		// along with a method for getting the most representative mention
		// Both sentence and token offsets start at 1!
		Map<Integer, CorefChain> graph = document.get(CorefChainAnnotation.class);

		System.out.println(graph.size());

	}

	public static void main(String[] args) {

		// parseNNDependencies();

		// parseCoreNLPNNDependencies();

		// parseCodeNLPDemo();

		// parseTest();

		parseSQClause();
	}
}
