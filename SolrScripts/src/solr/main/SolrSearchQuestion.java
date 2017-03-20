package solr.main;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import solr.utils.Const;

public class SolrSearchQuestion {

	@SuppressWarnings("unchecked")
	public static void printSomeExamples() {

		try {
			
			Properties props = new Properties();
			props.put("annotators", "tokenize, ssplit, parse");
			StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

			SolrClient solr = new HttpSolrClient.Builder(Const.URL_SORL).build();

			String solrQuery = Const.STAR_TWODOTS_START;

			String excludeBots = " NOT author._account_id:" + Const.ANDROID_BOT_TREEHUGGER + " NOT author._account_id:"
					+ Const.ANDROID_BOT_DECKARD + " NOT author._account_id:" + Const.ANDROID_BOT_ANONYMOUS
					+ " NOT author._account_id:" + Const.ANDROID_BOT_BIONIC + " NOT author._account_id:"
					+ Const.ANDROID_BOT_ANDROID_MERGER + " NOT author._account_id:" + Const.ANDROID_BOT_ANDROID_DEVTOOLS
					+ " NOT author._account_id:\\" + Const.ANDROID_BOT_GERRIT;

			SolrQuery query = new SolrQuery();
			
			query.setQuery(solrQuery + excludeBots);

			query.setFields(Const.MESSAGE);

			query.setRows(500);

			QueryResponse response = solr.query(query);

			SolrDocumentList results = response.getResults();

			for (int j = 0; j < results.size(); ++j) {

				SolrDocument codeReview = results.get(j);

				List<String> message = ((List<String>) codeReview.getFieldValue(Const.MESSAGE));
				
				Annotation document = new Annotation(message.get(0));
				
				pipeline.annotate(document);
			
				List<CoreMap> sentences = document.get(SentencesAnnotation.class);
			
				for (CoreMap sentence : sentences) {
			
					Tree tree = sentence.get(TreeAnnotation.class);
					Tree c = tree.getChild(0);
					
					// SBARQ and SQ
					
					if (c.label().toString().equalsIgnoreCase("SBARQ") 
							|| c.label().toString().equalsIgnoreCase("SQ") ) {
						
						System.out.println("sentence: " + sentence);
						System.out.println("parse tree: " + tree);
						System.out.println("root label: " + c.label());
						System.out.println("=========================");
					}
				}
			}
		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
		}

		System.out.println("Done...");
	}

	public static void main(String[] args) {

		printSomeExamples();
	}
}
