package solr.main;

import java.io.StringReader;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.shiftreduce.ShiftReduceParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.logging.Redwood;

public class SolrLogTest {

	private static Redwood.RedwoodChannels log = Redwood.channels(ShiftReduceParser.class);

	public static void main(String[] args) {

		String modelPath = "edu/stanford/nlp/models/srparser/englishSR.ser.gz";

		String taggerPath = "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger";

		String text = "I like you, don't I?. Do you like me? Why did you do that?! You are good! Am I wrong? Are you going to the party?";

		MaxentTagger tagger = new MaxentTagger(taggerPath);
		
		ShiftReduceParser model = ShiftReduceParser.loadModel(modelPath);

		DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(text));
		
		for (List<HasWord> sentence : tokenizer) {
			
			List<TaggedWord> tagged = tagger.tagSentence(sentence);
			
			Tree tree = model.apply(tagged);
			
			Tree c = tree.getChild(0);
			
			// TODO handle the FRAG sentence!!
			
			log.info(c.label());
		}
	}
}
