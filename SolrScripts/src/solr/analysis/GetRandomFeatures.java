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

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.SortClause;
import org.apache.solr.client.solrj.SolrServerException;
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

	public static void writeFileRandomFeatures(String commentType, String framework, int numberExamples) {

		String filePath = Const.EMPTY_STRING;

		if (commentType.equalsIgnoreCase(Const.GENERAL)) {

			filePath = Const.DIR_RESULTS + Const._GC + Const.SLASH;

		} else if (commentType.equalsIgnoreCase(Const.INLINE)) {

			filePath = Const.DIR_RESULTS + Const._IC + Const.SLASH;
		}

		List<Tuple> tuples = Utils.readTulpes(filePath + framework + Const._TUPLES_ID + Const._TXT);

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

		StringBuffer sbResults = buildXMLStringBuffer(randomList, sourcesJordanAndLakoff);

		String filePathRandom = Const.EMPTY_STRING;

		if (commentType.equalsIgnoreCase(Const.GENERAL)) {

			filePathRandom = Const.DIR_RESULTS + Const._GC + Const.SLASH + Const.DIR_TRAINING + Const.TRAINING_SET
					+ Const._XML;

		} else if (commentType.equalsIgnoreCase(Const.INLINE)) {

			filePathRandom = Const.DIR_RESULTS + Const._IC + Const.SLASH + Const.DIR_TRAINING + Const.TRAINING_SET
					+ Const._XML;
		}

		try (Writer writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(filePathRandom), Const._UTF_8))) {

			writer.write(sbResults.toString());
		} catch (Exception e) {
			System.out.println(e);
		}

		System.out.println("Done with writeFileRandomFeatures...");
	}

	public static void getHedgesExamplesWithoutOverlapping(String commentType, int numberExamples,
			String outputFormat) {

		List<Tuple> hedges = getListTuples(Const.HEDGES);
		List<Tuple> hypo = getListTuples(Const.HYPOTHETICALS);
		List<Tuple> probables = getListTuples(Const.PROBABLES);
		List<Tuple> I_statements = getListTuples(Const.I_STATEMENTS);
		List<Tuple> nonverbals = getListTuples(Const.NONVERBALS);
		List<Tuple> meta = getListTuples(Const.META);

		hedges = removeOverlappingFromSameList(hedges);
		hedges = removeOverlappingFromOtherList(hedges, hypo);
		hedges = removeOverlappingFromOtherList(hedges, probables);
		hedges = removeOverlappingFromOtherList(hedges, I_statements);
		hedges = removeOverlappingFromOtherList(hedges, nonverbals);
		hedges = removeOverlappingFromOtherList(hedges, meta);

		hedges = removeTrainingSet(hedges, commentType);

		System.out.println("Number of hedges comments: " + hedges.size());

		List<Tuple> randomList = new ArrayList<Tuple>();

		Map<String, String> sourcesJordanAndLakoff = Utils.readSourcesJordanAndLakoff();

		int counter = 0;

		for (int i = 0; i < hedges.size() && counter < numberExamples; i++) {

			Tuple randomTuple = randomItem(hedges);

			if (sourcesJordanAndLakoff.containsKey(randomTuple.getFeature())) {

				randomList.add(randomTuple);

				hedges.remove(randomTuple);

				counter = counter + 1;
			}
		}

		StringBuffer sbResults = new StringBuffer();

		String fileFormat = Const.EMPTY_STRING;

		if (outputFormat.equalsIgnoreCase(Const._XML)) {

			sbResults = buildXMLStringBuffer(randomList, sourcesJordanAndLakoff);

			fileFormat = Const._XML;

		} else if (outputFormat.equalsIgnoreCase(Const._TXT)) {

			sbResults = buildTXTtringBuffer(randomList);

			fileFormat = Const._TXT;
		}

		String filePathRandom = Const.EMPTY_STRING;

		if (commentType.equalsIgnoreCase(Const.GENERAL)) {

			filePathRandom = Const.DIR_RESULTS + Const._GC + Const.SLASH + Const.DIR_400 + Const.VERIFYING_SET
					+ fileFormat;

		} else if (commentType.equalsIgnoreCase(Const.INLINE)) {

			filePathRandom = Const.DIR_RESULTS + Const._IC + Const.SLASH + Const.DIR_400 + Const.VERIFYING_SET
					+ fileFormat;
		}

		try (Writer writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(filePathRandom), Const._UTF_8))) {

			writer.write(sbResults.toString());
		} catch (Exception e) {
			System.out.println(e);
		}

		System.out.println("Done with getHedgesExamplesWithoutOverlapping...");
	}

	public static StringBuffer buildTXTtringBuffer(List<Tuple> randomList) {

		StringBuffer sbResults = new StringBuffer();

		for (Tuple tuple : randomList) {

			sbResults.append(tuple.getCommentID() + Const.NEW_LINE);
		}

		return sbResults;
	}

	@SuppressWarnings("unchecked")
	public static void buildXLSExampleFile(String filePath, String outputFile) {

		try {

			FileOutputStream out = new FileOutputStream(outputFile);

			Workbook wb = new HSSFWorkbook();

			Sheet s = wb.createSheet();

			wb.setSheetName(Const._0, Const.HEDGES);

			Row r = null;

			Cell c = null;
			
			int lineCounter = 1;

			r = s.createRow(Const._0);

			c = r.createCell(Const._0);
			c.setCellValue(Const.ID);

			c = r.createCell(Const._1);
			c.setCellValue(Const.CONFUSION);

			c = r.createCell(Const._2);
			c.setCellValue(Const.REASONING);

			c = r.createCell(Const._3);
			c.setCellValue(Const.COMMENT);

			List<String> listIDs = Files.readAllLines(Paths.get(filePath));

			String queryString = Const.EMPTY_STRING;

			List<String> listSolrQueries = new ArrayList<String>();

			for (int i = 0; i < listIDs.size(); i++) {

				queryString = queryString + "id:" + listIDs.get(i) + Const.SPACE;

				if ((i + 1) % Const._100 == Const._0) {

					listSolrQueries.add(new String(queryString));

					queryString = Const.EMPTY_STRING;
				}
			}

			SolrClient solr = new HttpSolrClient.Builder(Const.URL_SORL).build();

			SolrQuery query = new SolrQuery();

			for (String solrQuery : listSolrQueries) {
				
				query.setRows(Const._100);
				
				query.setQuery(solrQuery);

				query.setFields(Const.ID, Const.MESSAGE);

				QueryResponse response = solr.query(query);

				SolrDocumentList results = response.getResults();
				
				for (int i = 0; i < results.size(); i++) {

					SolrDocument codeReview = results.get(i);

					String id = (String) codeReview.getFieldValue(Const.ID);

					String message = ((List<String>) codeReview.getFieldValue(Const.MESSAGE)).get(Const._0);

					r = s.createRow(lineCounter);

					c = r.createCell(Const._0);
					c.setCellValue(id);

					c = r.createCell(Const._3);
					c.setCellValue(message);
					
					lineCounter = lineCounter + 1;
				}
			}

			wb.write(out);

			out.close();

			wb.close();

		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
		}

		System.out.println("Done with buildXLSExampleFile...");
	}

	@SuppressWarnings("unchecked")
	public static StringBuffer buildXMLStringBuffer(List<Tuple> randomList,
			Map<String, String> sourcesJordanAndLakoff) {

		StringBuffer sbResults = new StringBuffer();

		sbResults.append("<set>" + Const.NEW_LINE);

		try {

			SolrClient solr = new HttpSolrClient.Builder(Const.URL_SORL).build();

			SolrQuery query = new SolrQuery();

			String queryString = Const.EMPTY_STRING;

			for (Tuple tuple : randomList) {

				queryString = Const.ID + Const.TWO_DOTS + tuple.getCommentID();

				query.setQuery(queryString);

				query.setFields(Const.MESSAGE);

				QueryResponse response = solr.query(query);

				SolrDocumentList results = response.getResults();

				SolrDocument codeReview = results.get(Const._0);

				String message = ((List<String>) codeReview.getFieldValue(Const.MESSAGE)).get(0);

				sbResults.append("<example>" + Const.NEW_LINE);
				sbResults.append("<id>" + tuple.getCommentID() + "</id>" + Const.NEW_LINE);
				sbResults.append("<feature>" + tuple.getFeature() + "</feature>" + Const.NEW_LINE);
				sbResults.append(
						"<source>" + sourcesJordanAndLakoff.get(tuple.getFeature()) + "</source>" + Const.NEW_LINE);
				sbResults.append("<confusion></confusion>" + Const.NEW_LINE);
				sbResults.append("<message>" + message + "</message>" + Const.NEW_LINE);
				sbResults.append("</example>" + Const.NEW_LINE + Const.NEW_LINE);
			}
		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
		}

		sbResults.append("</set>");

		return sbResults;
	}

	public static List<Tuple> removeTrainingSet(List<Tuple> tuples, String commentType) {

		List<Tuple> newList = new ArrayList<Tuple>();

		String filePath = Const.EMPTY_STRING;

		if (commentType.equalsIgnoreCase(Const.GENERAL)) {

			filePath = Const.DIR_RESULTS + Const._GC + Const.SLASH + Const.DIR_TRAINING + Const.TRAINING_SET_IDS
					+ Const._TXT;

		} else if (commentType.equalsIgnoreCase(Const.INLINE)) {

			filePath = Const.DIR_RESULTS + Const._IC + Const.SLASH + Const.DIR_TRAINING + Const.TRAINING_SET_IDS
					+ Const._TXT;
		}

		try {

			List<String> trainingSetIDs = Files.readAllLines(Paths.get(filePath));

			for (Tuple tuple : tuples) {

				boolean isFromTrainingSet = false;

				for (String id : trainingSetIDs) {

					if (tuple.getCommentID().equals(id)) {
						isFromTrainingSet = true;
						break;
					}
				}

				if (!isFromTrainingSet) {
					newList.add(tuple);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return newList;
	}

	public static List<Tuple> removeOverlappingFromSameList(List<Tuple> tuples) {

		List<Tuple> newList = new ArrayList<Tuple>();

		for (Tuple tuple : tuples) {

			List<Tuple> tmp = new ArrayList<Tuple>(tuples);

			tmp.remove(tuple);

			boolean hasDuplicate = false;

			for (Tuple tuple2 : tmp) {

				if (tuple.equals(tuple2)) {
					hasDuplicate = true;
					break;
				}
			}

			if (!hasDuplicate) {
				newList.add(tuple);
			}
		}

		return newList;
	}

	public static List<Tuple> removeOverlappingFromOtherList(List<Tuple> source, List<Tuple> target) {

		List<Tuple> newList = new ArrayList<Tuple>();

		for (Tuple s : source) {

			boolean hasDuplicate = false;

			for (Tuple t : target) {

				if (s.equals(t)) {
					hasDuplicate = true;
					break;
				}
			}

			if (!hasDuplicate) {
				newList.add(s);
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

		// String commentType = Const.GENERAL;

		// String framework = Const.HEDGES;

		// int numberExamples = 400;

		// String outputFormat = Const._TXT;

		// writeFileRandomFeatures(commentType, framework, numberExamples);

		// getHedgesExamplesWithoutOverlapping(commentType, numberExamples,
		// outputFormat);

		buildXLSExampleFile("./results-gc/set-400/verifying-set.txt", "./results-gc/set-400/verifying-set.xls");

	}
}
