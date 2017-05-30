package solr.analysis.random;

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
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import solr.basics.Tuple;
import solr.utils.Const;
import solr.utils.Utils;

public class GetRandomOtherKeywordFeatures {

	private static Random random;

	public static Tuple randomItem(List<Tuple> mylist) {

		random = new Random();

		Tuple randomInt = mylist.get(random.nextInt(mylist.size()));

		return randomInt;
	}

	public static void getRandomOtherKeywordFeatures(String commentType, int numberExamples) {

		String path = Const.DIR_RESULTS;

		if (commentType.equalsIgnoreCase(Const.GENERAL)) {

			path = path + Const._GC + Const.SLASH;

		} else if (commentType.equalsIgnoreCase(Const.INLINE)) {

			path = path + Const._IC + Const.SLASH;
		}

		List<Tuple> hypo = Utils.readTulpes(path + Const.HYPOTHETICALS + Const._TUPLESID_TXT);
		List<Tuple> probables = Utils.readTulpes(path + Const.PROBABLES + Const._TUPLESID_TXT);
		List<Tuple> I_statements = Utils.readTulpes(path + Const.I_STATEMENTS + Const._TUPLESID_TXT);
		List<Tuple> nonverbals = Utils.readTulpes(path + Const.NONVERBALS + Const._TUPLESID_TXT);
		List<Tuple> meta = Utils.readTulpes(path + Const.META + Const._TUPLESID_TXT);

		doUnionSets(probables, hypo);
		doUnionSets(probables, I_statements);
		doUnionSets(probables, nonverbals);
		doUnionSets(probables, meta);

		if (commentType.equalsIgnoreCase(Const.GENERAL)) {

			probables = removeTrainingSet(probables);
		}

		removeVerifyingSet(probables, Const.HEDGES, path);
		
		removeVerifyingSet(probables, Const.QUESTIONS, path);
		
		List<Tuple> randomList = new ArrayList<Tuple>();

		Map<String, String> sources = Utils.readOthersSourcesJordanLakoffHolmesEbert();

		int counter = 0;

		for (int i = 0; i < probables.size() && counter < numberExamples; i++) {

			Tuple randomTuple = randomItem(probables);

			if (sources.containsKey(randomTuple.getFeature())) {

				randomList.add(randomTuple);

				probables.remove(randomTuple);

				counter = counter + 1;
			}
		}
		
		StringBuffer sbResults = new StringBuffer();

		sbResults = buildStringBuffer(randomList);

		String filePathRandom = path + Const.DIR_VERIFYING + Const.VERIFYING_ + Const.OTHER + Const.SET + Const._TXT;

		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePathRandom), Const._UTF_8))) {

			writer.write(sbResults.toString());
		} catch (Exception e) {
			System.out.println(e);
		}

		String outputXLS = path + Const.DIR_VERIFYING + Const.VERIFYING_ + Const.OTHER + Const.SET + Const._XLS;
		
		buildXLSFile(filePathRandom, outputXLS);
		
		System.out.println("Done with getRandomOtherKeywordFeatures...");

	}
	
	public static StringBuffer buildStringBuffer(List<Tuple> randomList) {

		StringBuffer sbResults = new StringBuffer();

		for (Tuple tuple : randomList) {

			sbResults.append(tuple.getCommentID() + Const.NEW_LINE);
		}

		return sbResults;
	}

	public static void removeVerifyingSet(List<Tuple> tuples, String feature, String filePath) {

		try {

			List<String> verifyingHedgesIDs = Files.readAllLines(Paths
					.get(filePath + Const.DIR_VERIFYING + Const.VERIFYING_ + feature + Const.SET + Const._TXT));

			for (String hedgeID : verifyingHedgesIDs) {

				for (Tuple tuple : tuples) {

					if (tuple.getCommentID().equals(hedgeID)) {
						tuples.remove(tuple);
						break;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static List<Tuple> removeTrainingSet(List<Tuple> tuples) {

		List<Tuple> newList = new ArrayList<Tuple>();

		String filePath = Const.DIR_RESULTS + Const._GC + Const.SLASH + Const.DIR_TRAINING + Const.TRAINING_SET_IDS
				+ Const._TXT;

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

	public static void doUnionSets(List<Tuple> source, List<Tuple> target) {

		for (Tuple t : target) {

			boolean needToAdd = true;

			for (Tuple s : source) {

				if (s.equals(t)) {
					needToAdd = false;
					break;
				}
			}

			if (needToAdd) {
				source.add(t);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static void buildXLSFile(String filePath, String outputFile) {

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
	
	public static void main(String[] args) {

		String commentType = Const.INLINE;

		int numberExamples = 400;

		getRandomOtherKeywordFeatures(commentType, numberExamples);
	}
}
