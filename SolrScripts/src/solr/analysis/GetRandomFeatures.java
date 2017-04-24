package solr.analysis;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CursorMarkParams;

import solr.basics.Tuple;
import solr.utils.Const;
import solr.utils.Utils;

public class GetRandomFeatures {

	private static Random random;

	public static Tuple randomItem(List<Tuple> mylist) {

		random = new Random();

		Tuple randomInt = mylist.get(random.nextInt(mylist.size()));

		return randomInt;
	}

	@SuppressWarnings("unchecked")
	public static void writeFileRandomFeatures(String commentType, String framework, int numberExamples) {

		String filePath = "";

		if (commentType.equalsIgnoreCase(Const.GENERAL)) {

			filePath = Const.DIR_RESULTS + Const._GC + Const.SLASH;

		} else if (commentType.equalsIgnoreCase(Const.INLINE)) {

			filePath = Const.DIR_RESULTS + Const._IC + Const.SLASH;
		}

		List<Tuple> tuples = Utils.readTulpe(filePath + framework + Const._TUPLES_ID + Const._TXT);

		List<Tuple> randomList = new ArrayList<Tuple>();

		Map<String, String> sourcesJordanAndLakoff = Utils.readSourcesJordanAndLakoff();

		int counter = 0;

		for (int i = 0; i < tuples.size() && counter < numberExamples; i++) {

			Tuple randomTuple = randomItem(tuples);

			if (sourcesJordanAndLakoff.containsKey(randomTuple.getFeature())) {

				randomList.add(randomTuple);

				tuples.remove(randomTuple);

				counter = counter + 1;
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
				sbResults.append("<source>" + sourcesJordanAndLakoff.get(tuple.getFeature()) + "</source>" + Const.NEW_LINE);
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

		System.out.println("Done with writeFileRandomFeatures...");
	}

	public static void getHedgesExamplesWithoutOverlapping(String commentType, int numberExamples) {
		
		List<Tuple> hedges = getListTuples(Const.HEDGES);
		List<Tuple> hypo = getListTuples(Const.HYPOTHETICALS);
		List<Tuple> probables = getListTuples(Const.PROBABLES);
		List<Tuple> I_statements = getListTuples(Const.I_STATEMENTS);
		List<Tuple> nonverbals = getListTuples(Const.NONVERBALS);
		List<Tuple> meta = getListTuples(Const.META);

		hedges = removeOverlapping(hedges);
		hedges = removeOverlapping(hypo);
		hedges = removeOverlapping(probables);
		hedges = removeOverlapping(I_statements);
		hedges = removeOverlapping(nonverbals);
		hedges = removeOverlapping(meta);
		
		System.out.println("Done with getHedgesExamplesWithoutOverlapping...");
	}

	public static List<Tuple> removeOverlapping(List<Tuple> tuples) {
		
		List<Tuple> newList = new ArrayList<Tuple>();
		
		for (Tuple tuple : tuples) {
			
			List<Tuple> tmp = new ArrayList<Tuple>(tuples);
			
			tmp.remove(tuple);
			
			int counter = 0;
			
			for (Tuple tuple2 : tmp) {
				
				if (tuple.equals(tuple2)) {
					counter = counter + 1;
				}
			}
			
			if (counter == 0) {
				newList.add(tuple);
			}
		}
		
		return newList;
	}

	public static List<Tuple> getListTuples(String framework) {
		
		List<Tuple> listTuples = new ArrayList<Tuple>();
		
		try {

			SolrClient solr = new HttpSolrClient.Builder(Const.URL_SORL).build();

			String solrQuery = Const.MESSAGE + Const.TWO_DOTS + Const.DOUBLE_QUOTES;

			String frameworkPath = Const.DIR_FRAMEWORK + framework + Const._TXT;

			List<String> features = Files.readAllLines(Paths.get(frameworkPath));
			
			for (String feature : features) {
				
				String solrQueryString = solrQuery + feature + Const.DOUBLE_QUOTES;

				SolrQuery query = new SolrQuery();

				query.setQuery(solrQueryString + Const.EXCLUDE_BOTS);

				query.setSort(SortClause.asc(Const.ID));

				query.setFields(Const.ID);

				query.setRows(Const._5000);

				String cursorMark = CursorMarkParams.CURSOR_MARK_START;

				boolean done = false;
				
				while (!done) {
					
					query.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);

					QueryResponse response = solr.query(query);

					String nextCursorMark = response.getNextCursorMark();

					SolrDocumentList results = response.getResults();

					for (int j = 0; j < results.size(); ++j) {

						SolrDocument codeReview = results.get(j);

						String id = (String) codeReview.getFieldValue(Const.ID);

						listTuples.add(new Tuple(feature, id));
					}

					if (cursorMark.equals(nextCursorMark)) {
						done = true;
					}

					cursorMark = nextCursorMark;
				}
			}
			
		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
		}
		
		return listTuples;
	}

	public static void main(String[] args) {

		String commentType = Const.INLINE;

		// String framework = Const.HEDGES;

		int numberExamples = 25;

		// writeFileRandomFeatures(commentType, framework, numberExamples);
		
		getHedgesExamplesWithoutOverlapping(commentType, numberExamples);

	}
}
