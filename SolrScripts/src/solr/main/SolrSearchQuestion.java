package solr.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
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
import solr.basics.FeatureResult;
import solr.basics.Tuple;
import solr.utils.Const;
import solr.utils.Log;
import solr.utils.Utils;

public class SolrSearchQuestion {

	private static Log log;

	@SuppressWarnings("unchecked")
	public static void solrSearchQuestion(String commentType, int start, int rows) {
		
		int end = start + rows;
		
		String logFileName = "";
		
		if (commentType.equalsIgnoreCase(Const.GENERAL)) {
			
			logFileName = "solrSearchQuestionGC-[";
			
		} else if (commentType.equalsIgnoreCase(Const.INLINE)) {
			
			logFileName = "solrSearchQuestionIC-[";
		}
		
		log = new Log(logFileName +  start + "-" + end + "]");

		log.doFineLogging(Utils.getTimeStamp() + " >> Started SolrSearchQuestion: start = " + start + "...");
		System.out.println(Utils.getTimeStamp() + " >> Started SolrSearchQuestion: start = " + start + "...");
		
		String framework = "questions";

		FeatureResult result = new FeatureResult();

		List<Tuple> listTuples = new ArrayList<Tuple>();

		List<String> listIDs = new ArrayList<String>();

		StringBuffer sbFeaturesOutput = new StringBuffer();

		sbFeaturesOutput.append(Const.CSV_HEADLINE);

		sbFeaturesOutput.append(Const.NEW_LINE);

		try {

			log.doFineLogging("Starting the StanfordNLP API...");
			System.out.println("Starting the StanfordNLP API...");

			Properties props = new Properties();
			props.put(Const.ANNOTATORS, Const.STANFORD_NLP_ANNOTATORS);
			StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

			log.doFineLogging("Finished starting the StanfordNLP API!");
			System.out.println("Finished starting the StanfordNLP API!");

			SolrClient solr = new HttpSolrClient.Builder(Const.URL_SORL).build();

			SolrQuery query = new SolrQuery();

			String queryString = Const.STAR_TWODOTS_START + Const.EXCLUDE_BOTS;

			query.setQuery(queryString);

			query.setSort(SortClause.asc(Const.ID));

			query.setFields(Const.ID, Const.MESSAGE);

			query.setRows(rows);

			query.setStart(start);

			long numSBARQFeaturesFound = 0;

			long numSQFeaturesFound = 0;

			long numSBARQCommentsFound = 0;

			long numSQCommentsFound = 0;

			QueryResponse response = solr.query(query);

			SolrDocumentList results = response.getResults();
			
			int logCounter = 0;

			for (int i = 0; i < results.size(); i++) {

				SolrDocument codeReview = results.get(i);

				String id = (String) codeReview.getFieldValue(Const.ID);
				
				String comment = ((List<String>) codeReview.getFieldValue(Const.MESSAGE)).get(0);

				Annotation document = new Annotation(comment);

				pipeline.annotate(document);

				List<CoreMap> sentences = document.get(SentencesAnnotation.class);

				for (CoreMap sentence : sentences) {

					Tree tree = sentence.get(TreeAnnotation.class);
					Tree c = tree.getChild(0);

					if (c.label().toString().equalsIgnoreCase(Const.SBARQ)) {

						numSBARQFeaturesFound = numSBARQFeaturesFound + 1;

						if (!listIDs.contains(id)) {

							numSBARQCommentsFound = numSBARQCommentsFound + 1;

							listIDs.add(id);

							listTuples.add(new Tuple(Const.SBARQ, id));
						}

					} else if (c.label().toString().equalsIgnoreCase(Const.SQ)) {

						numSQFeaturesFound = numSQFeaturesFound + 1;

						if (!listIDs.contains(id)) {

							numSQCommentsFound = numSQCommentsFound + 1;

							listIDs.add(id);

							listTuples.add(new Tuple(Const.SQ, id));
						}
					}
				}

				if (logCounter % Const._100 == Const._0) {
					
					System.out.println(Utils.getTimeStamp() + " >> Finished more 100 comments...");
					log.doInfoLogging(Utils.getTimeStamp() + " >> Finished more 100 comments...");
				}
				
				logCounter = logCounter + 1;
			}

			sbFeaturesOutput.append(Const.SBARQ + Const.SEMICOLON + numSBARQCommentsFound + Const.SEMICOLON + numSBARQFeaturesFound);

			sbFeaturesOutput.append(Const.NEW_LINE);

			sbFeaturesOutput.append(Const.SQ + Const.SEMICOLON + numSQCommentsFound + Const.SEMICOLON + numSQFeaturesFound);

			sbFeaturesOutput.append(Const.NEW_LINE);

			long totalNumFeaturesFound = numSQFeaturesFound + numSBARQFeaturesFound;

			sbFeaturesOutput.append(Const.TOTAL + Const.SEMICOLON + listIDs.size() + Const.SEMICOLON + totalNumFeaturesFound);

			sbFeaturesOutput.append(Const.NEW_LINE);

			result.setFramework(framework);

			result.setListIDs(listIDs);

			result.setTotalNumCommentsFound(listIDs.size());

			result.setTotalNumFeaturesFound(totalNumFeaturesFound);

			result.setListTulpes(listTuples);

			Utils.writeCSVOutputFile(framework + "-" + start, commentType, sbFeaturesOutput);

			Utils.writeIDsOutputFile(framework + "-" + start, commentType, listIDs);

			Utils.writeTuplesOutputFile(framework + "-" + start, commentType, listTuples);

		} catch (SolrServerException | IOException e) {
			// e.printStackTrace();
			log.doSevereLogging("Error: solrSearchQuestion method!!!");
			log.doSevereLogging(e.getStackTrace().toString());
		}

		log.doFineLogging(Utils.getTimeStamp() + " >> Finished SolrSearchQuestion!");
		System.out.println(Utils.getTimeStamp() + " >> Finished SolrSearchQuestion!");
	}

	public static void main(String[] args) {

		String commentType = args[0];
		
		int rows = Const._20000;

		int start = Integer.valueOf(args[1]);
		
		// Work Done - GENERAL COMMENTS @linuxapps02:
		// 0 		- 10000		=> OK	=> PID 40564	=> solrSearchQuestion.2017-04-20-035915.log
		// 10000	- 20000		=> 		=> PID 43226	=>

		
		// Work Done - INLINE COMMENTS @TUe:
		// 0 		- 10000		=> OK	=> solrSearchQuestionIC-[0-10000].2017-04-21-114346.log 
		// 10000	- 20000		=> 	=>

		
		solrSearchQuestion(commentType, start, rows);
	}
}
