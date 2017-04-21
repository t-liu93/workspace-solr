package solr.analysis;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import solr.basics.Tuple;
import solr.utils.Const;
import solr.utils.Utils;

public class GetRandomFeatures {

	private static Random random;

	public static void getRandomFeaturesIDs(String commentType, String framework, String feature, int numberExamples) {

		String filePath = "";

		if (commentType.equalsIgnoreCase(Const.GENERAL)) {

			filePath = Const.DIR_RESULTS + Const._GC + Const.SLASH;

		} else if (commentType.equalsIgnoreCase(Const.INLINE)) {

			filePath = Const.DIR_RESULTS + Const._IC + Const.SLASH;
		}

		List<Tuple> tuples = Utils.readTulpe(filePath + framework + Const._TUPLES_ID + Const._TXT);

		Map<String, String> map = new HashMap<String, String>();

		for (Tuple tuple : tuples) {

			if (tuple.getFeature().equalsIgnoreCase(feature)) {

				map.put(tuple.getCommentID(), tuple.getFeature());

			}
		}

		StringBuffer sbOutput = new StringBuffer();

		for (int i = 0; i < numberExamples; i++) {

			Random random = new Random();

			List<String> keys = new ArrayList<String>(map.keySet());

			String randomKey = keys.get(random.nextInt(keys.size()));

			sbOutput.append("id:" + randomKey + " ");

			map.remove(randomKey);
		}

		System.out.println(sbOutput);

	}

	public static Tuple randomItem(List<Tuple> mylist) {

		random = new Random();

		Tuple randomInt = mylist.get(random.nextInt(mylist.size()));

		return randomInt;
	}

	@SuppressWarnings("unchecked")
	public static void getRandomFeatures(String commentType, String framework, int numberExamples) {

		String filePath = "";

		if (commentType.equalsIgnoreCase(Const.GENERAL)) {

			filePath = Const.DIR_RESULTS + Const._GC + Const.SLASH;

		} else if (commentType.equalsIgnoreCase(Const.INLINE)) {

			filePath = Const.DIR_RESULTS + Const._IC + Const.SLASH;
		}

		List<Tuple> tuples = Utils.readTulpe(filePath + framework + Const._TUPLES_ID + Const._TXT);

		List<Tuple> randomList = new ArrayList<Tuple>();
		
		Map<String, String> sourcesJordanAndLakoff = Utils.readSourcesJordanAndLakoff();

		for (int i = 0; i < numberExamples; i++) {

			Tuple randomTuple = randomItem(tuples);
			
			if (sourcesJordanAndLakoff.containsKey(randomTuple.getFeature())) {
				
				randomList.add(randomTuple);
				
				tuples.remove(randomTuple);
			}
		}
		
		StringBuffer sbResults = new StringBuffer();
		
		sbResults.append("<set>" + Const.NEW_LINE);

		try {
			
			SolrClient solr = new HttpSolrClient.Builder(Const.URL_SORL).build();

			SolrQuery query = new SolrQuery();

			String queryString = "";

			for (Tuple tuple : randomList) {

				queryString = Const.ID + Const.TWO_DOTS + tuple.getCommentID();

				query.setQuery(queryString);

				query.setFields(Const.MESSAGE);

				QueryResponse response;
				
				response = solr.query(query);

				SolrDocumentList results = response.getResults();
				
				SolrDocument codeReview = results.get(Const._0);

				String message = ((List<String>) codeReview.getFieldValue(Const.MESSAGE)).get(0);
				
				sbResults.append("<example>" + Const.NEW_LINE);
				sbResults.append("<id>" + tuple.getCommentID() + "</id>" + Const.NEW_LINE);
				sbResults.append("<feature>" + tuple.getFeature() + "</feature>" + Const.NEW_LINE);
				sbResults.append("<confusion></confusion>" + Const.NEW_LINE);
				sbResults.append("<message>" + message + "</message>" + Const.NEW_LINE);
				sbResults.append("</example>" + Const.NEW_LINE + Const.NEW_LINE);
			}
		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
		}
		
		sbResults.append("</set>");
		
		String filePathRandom = "";
		
		if (commentType.equalsIgnoreCase(Const.GENERAL)) {

			filePathRandom = Const.DIR_RESULTS + Const._GC + Const.SLASH + Const.DIR_TRAINING + Const.TRAINING_SET + Const._XML;

		} else if (commentType.equalsIgnoreCase(Const.INLINE)) {

			filePathRandom = Const.DIR_RESULTS + Const._IC + Const.SLASH + Const.DIR_TRAINING + Const.TRAINING_SET + Const._XML;
		}
		
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePathRandom), Const._UTF_8))) {

			writer.write(sbResults.toString());
		} catch (Exception e) {
			System.out.println(e);
		}
		
		System.out.println("Done with getRandomFeatures...");
	}

	public static void main(String[] args) {

		String commentType = Const.GENERAL;
		
		String framework = Const.HEDGES;

		int numberExamples = 25;

		getRandomFeatures(commentType, framework, numberExamples);

		// String feature = "would";

		// getRandomFeaturesIDs(commentType, framework, feature, numberExamples);
	}
}
