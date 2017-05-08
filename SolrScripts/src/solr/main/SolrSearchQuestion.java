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
		
		String framework = Const.QUESTIONS;

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
		//
		// 0 		- 10000		=> OK	=> PID 40564	=> solrSearchQuestionGC-[0-10000].2017-04-20-035915.log
		// 10000	- 20000		=> OK	=> PID 43226	=> solrSearchQuestionGC-[10000-20000].2017-04-21-070201.log
		// 20000	- 30000		=> OK	=> PID 43477	=> solrSearchQuestionGC-[20000-30000].2017-04-21-080525.log
		// 30000	- 50000		=> OK	=> PID 43719	=> solrSearchQuestionGC-[30000-50000].2017-04-21-085926.log
		// 50000	- 70000		=> OK	=> PID 44096 	=> solrSearchQuestionGC-[50000-70000].2017-04-21-110828.log
		// 70000	- 100000	=> OK	=> PID 47243 	=> solrSearchQuestionGC-[70000-100000].2017-04-22-091938.log
		// 100000	- 150000	=> OK	=> PID 1426 	=> solrSearchQuestionGC-[100000-150000].2017-04-23-054314.log
		// 150000	- 180000	=> OK	=> PID 19679  	=> solrSearchQuestionGC-[150000-180000].2017-04-26-091009.log
		// 180000	- 190000	=> OK 	=> @fe  		=> solrSearchQuestionGC-[180000-190000].2017-04-25-102258.log
		// 190000	- 200000	=> OK 	=> @fe  		=> solrSearchQuestionGC-[190000-200000].2017-04-25-110946.log
		// 200000	- 210000	=> OK 	=> @TUe  		=> solrSearchQuestionGC-[200000-210000].2017-04-25-052509.log 
		// 210000	- 220000	=> OK 	=> @TUe  		=> solrSearchQuestionGC-[210000-220000].2017-04-26-094128.log
		// 220000	- 228000	=> OK 	=> @TUe  		=> solrSearchQuestionGC-[220000-228000].2017-04-26-010449.log
		// 228000	- 230000	=> OK 	=> @TUe  		=> solrSearchQuestionGC-[228000-230000].2017-04-26-021455.log
		// 230000	- 280000	=> OK 	=> @TUe  		=> solrSearchQuestionGC-[230000-280000].2017-04-26-054427.log
		// 280000	- 330000	=> OK 	=> @TUe  		=> solrSearchQuestionGC-[280000-330000].2017-04-27-054146.log
		// 330000	- 350000	=> OK 	=> @TUe  		=> solrSearchQuestionGC-[330000-350000].2017-04-29-015009.log
		// 350000	- 380000	=> OK	=> PID 33036	=> solrSearchQuestionGC-[350000-380000].2017-04-29-085605.log
		// 380000	- 400000	=> OK 	=> PID 27954	=> solrSearchQuestionGC-[380000-400000].2017-04-28-075700.log
		// 400000	- 430000 	=> OK 	=> PID 36699 	=> solrSearchQuestionGC-[400000-430000].2017-04-30-105537.log
		// 430000	- 450000 	=> OK 	=> @TUe		 	=> solrSearchQuestionGC-[430000-450000].2017-04-30-035711.log
		// 450000	- 470000 	=> OK 	=> @TUe		 	=> solrSearchQuestionGC-[450000-470000].2017-04-30-083649.log
		// 470000	- 520000 	=> OK 	=> PID 42053 	=> solrSearchQuestionGC-[470000-520000].2017-05-01-053202.log
		// 520000	- 550000 	=> OK 	=> @TUe			=> solrSearchQuestionGC-[520000-550000].2017-05-01-103643.log
		// 550000	- 580000 	=> OK	=> @TUe			=> solrSearchQuestionGC-[550000-580000].2017-05-01-035029.log
		// 580000	- 590000 	=> OK	=> @TUe			=> solrSearchQuestionGC-[580000-590000].2017-05-04-125534.log
		
		// 590000	- 600000 	=> 		=> @TUe			=> 
		
		// 600000	- 610000 	=> OK	=> PID 9693		=> solrSearchQuestionGC-[600000-610000].2017-05-04-035742.log
		// 610000	- 615000 	=> OK	=> @TUe			=> solrSearchQuestionGC-[610000-615000].2017-05-06-103802.log
		
		// 615000 	- 620000	=> OK	=> @TUe			=> solrSearchQuestionGC-[615000-620000].2017-05-08-093459.log
		// 620000	- 630000 	=> 		=> @TUe			=>
		
		// 630000	- 640000 	=> OK	=> @TUe			=> solrSearchQuestionGC-[630000-640000].2017-05-02-124710.log
		// 640000	- 650000 	=> OK	=> @TUe			=> solrSearchQuestionGC-[640000-650000].2017-05-02-011916.log
		// 650000	- 660000 	=> OK	=> @TUe			=> solrSearchQuestionGC-[650000-660000].2017-05-02-021227.log
		// 660000	- 670000 	=> OK	=> @TUe			=> solrSearchQuestionGC-[660000-670000].2017-05-02-023754.log

		
		// Work Done - INLINE COMMENTS @TUe:
		// 
		// 0 		- 10000		=> OK	=> solrSearchQuestionIC-[0-10000].2017-04-21-114346.log 
		// 10000	- 30000		=> OK 	=> solrSearchQuestionIC-[10000-30000].2017-04-21-125549.log
		// 30000	- 50000		=> OK 	=> solrSearchQuestionIC-[30000-50000].2017-04-21-021848.log
		// 50000	- 70000		=> OK 	=> solrSearchQuestionIC-[50000-70000].2017-04-21-040931.log
		// 70000	- 100000	=> OK 	=> solrSearchQuestionIC-[70000-100000].2017-04-21-055510.log
		// 100000	- 200000	=> OK 	=> solrSearchQuestionIC-[100000-200000].2017-04-24-094616.log
		// 200000	- 232471	=> OK 	=> solrSearchQuestionIC-[200000-232471].2017-04-25-094147.log

		
		solrSearchQuestion(commentType, start, rows);
	}
}
