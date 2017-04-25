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
		
		// invocation method:
		//										  comType start rows
		// nohup java -jar solrSearchQuestion.jar general 20000 20000 &

		String commentType = args[0];

		int start = Integer.valueOf(args[1]);
		
		int rows = Integer.valueOf(args[2]);

		// Work Done - GENERAL COMMENTS @linuxapps02:
		// Rows => 10000
		//
		// 0 		- 10000		=> OK	=> PID 40564	=> solrSearchQuestionGC-[0-10000].2017-04-20-035915.log
		// 10000	- 20000		=> OK	=> PID 43226	=> solrSearchQuestionGC-[10000-20000].2017-04-21-070201.log
		// 20000	- 30000		=> OK	=> PID 43477	=> solrSearchQuestionGC-[20000-30000].2017-04-21-080525.log
		// 30000	- 50000		=> OK	=> PID 43719	=> solrSearchQuestionGC-[30000-50000].2017-04-21-085926.log
		// 50000	- 70000		=> OK	=> PID 44096 	=> solrSearchQuestionGC-[50000-70000].2017-04-21-110828.log
		// 70000	- 100000	=> OK	=> PID 47243 	=> solrSearchQuestionGC-[70000-100000].2017-04-22-091938.log
		// 100000	- 150000	=> OK	=> PID 1426 	=> solrSearchQuestionGC-[100000-150000].2017-04-23-054314.log
		// 150000	- 180000	=> 	=> PID 12307  => 
		// 180000	- 190000	=> OK 	=> @fe  		=> solrSearchQuestionGC-[180000-190000].2017-04-25-102258.log
		// 190000	- 200000	=>  	=> @fe  		=> 

		
		// Work Done - INLINE COMMENTS @TUe:
		// Rows => 32471
		// 
		// 0 		- 10000		=> OK	=> solrSearchQuestionIC-[0-10000].2017-04-21-114346.log 
		// 10000	- 30000		=> OK 	=> solrSearchQuestionIC-[10000-30000].2017-04-21-125549.log
		// 30000	- 50000		=> OK 	=> solrSearchQuestionIC-[30000-50000].2017-04-21-021848.log
		// 50000	- 70000		=> OK 	=> solrSearchQuestionIC-[50000-70000].2017-04-21-040931.log
		// 70000	- 100000	=> OK 	=> solrSearchQuestionIC-[70000-100000].2017-04-21-055510.log
		// 100000	- 200000	=> OK 	=> solrSearchQuestionIC-[100000-200000].2017-04-24-094616.log
		// 200000	- 232471	=>  	=> 

		
		solrSearchQuestion(commentType, start, rows);
	}
}
