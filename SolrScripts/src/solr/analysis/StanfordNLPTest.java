package solr.analysis;

import java.io.StringReader;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PropertiesUtils;
import solr.utils.Utils;

public class StanfordNLPTest {

	public static void parseNNDependencies() {
		String modelPath = DependencyParser.DEFAULT_MODEL;

		String taggerPath = "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger";

		MaxentTagger tagger = new MaxentTagger(taggerPath);

		DependencyParser parser = DependencyParser.loadFromModelFile(modelPath);

		String text = "It's is a test... Why?! Wait, I will!!! This is a T.L.A. test. Now with a Dr. in it.";

		List<String> sentences = Utils.splitParagraphIntoSentences(text);

		for (String sentence : sentences) {

			DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(sentence));

			for (List<HasWord> phrase : tokenizer) {

				List<TaggedWord> tagged = tagger.tagSentence(phrase);

				GrammaticalStructure gs = parser.predict(tagged);

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

	public static void main(String[] args) {

		// parseNNDependencies();

		// parseCoreNLPNNDependencies();
		
		parseCodeNLPDemo();
	}
}
