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

import solr.utils.Const;

public class GetRandomQuestionFeatures {

	private static Random random;

	public static String randomItem(List<String> mylist) {

		random = new Random();

		String randomInt = mylist.get(random.nextInt(mylist.size()));

		return randomInt;
	}

	public static void getRandomQuestionFeatures(String commentType, int numberExamples) {

		String filePath = Const.EMPTY_STRING;

		if (commentType.equalsIgnoreCase(Const.GENERAL)) {

			filePath = Const.DIR_RESULTS + Const._GC + Const.SLASH;

		} else if (commentType.equalsIgnoreCase(Const.INLINE)) {

			filePath = Const.DIR_RESULTS + Const._IC + Const.SLASH;
		}

		try {

			List<String> newList = new ArrayList<String>();

			List<String> randomList = new ArrayList<String>();

			List<String> questionIDs = Files.readAllLines(Paths.get(filePath + Const.QUESTIONS + Const._ID_TXT));

			if (commentType.equalsIgnoreCase(Const.GENERAL)) {

				removeTrainingSet(filePath, newList, questionIDs);

				removeVerifyingHedges(filePath, newList, questionIDs);
				
			} else if (commentType.equalsIgnoreCase(Const.INLINE)) {

				newList = questionIDs;
			}

			int counter = 0;

			for (int i = 0; i < newList.size() && counter < numberExamples; i++) {

				String randomItem = randomItem(newList);

				randomList.add(randomItem);

				newList.remove(randomItem);

				counter = counter + 1;
			}

			String file = filePath + Const.DIR_VERIFYING + Const.VERIFYING_ + Const.QUESTIONS + Const.SET;

			try (Writer writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(file + Const._TXT), Const._UTF_8))) {

				for (String string : randomList) {
					writer.write(string + Const.NEW_LINE);
				}
			} catch (Exception e) {
				System.out.println(e);
			}

			querySolr(randomList, file + Const._XLS);

		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Done with getRandomQuestionFeatures..");
	}

	@SuppressWarnings("unchecked")
	public static void querySolr(List<String> listIDs, String filePath) {

		try {

			FileOutputStream out = new FileOutputStream(filePath);

			Workbook wb = new HSSFWorkbook();

			Sheet s = wb.createSheet();

			wb.setSheetName(Const._0, Const.QUESTIONS);

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
	}

	public static void removeTrainingSet(String filePath, List<String> newList, List<String> questionIDs)
			throws IOException {

		List<String> trainingSetIDs = Files
				.readAllLines(Paths.get(filePath + Const.DIR_TRAINING + Const.TRAINING_SET_IDS + Const._TXT));

		for (String question : questionIDs) {

			boolean hasDuplicate = false;

			for (String hedge : trainingSetIDs) {

				if (question.equals(hedge)) {
					hasDuplicate = true;
					break;
				}
			}

			if (!hasDuplicate) {
				newList.add(question);
			}
		}
	}

	public static void removeVerifyingHedges(String filePath, List<String> newList, List<String> questionIDs)
			throws IOException {

		List<String> verifyingHedgesIDs = Files.readAllLines(
				Paths.get(filePath + Const.DIR_VERIFYING + Const.VERIFYING_ + Const.HEDGES + Const.SET + Const._TXT));

		for (String question : questionIDs) {

			boolean hasDuplicate = false;

			for (String hedge : verifyingHedgesIDs) {

				if (question.equals(hedge)) {
					hasDuplicate = true;
					break;
				}
			}

			if (!hasDuplicate) {
				newList.add(question);
			}
		}
	}

	public static void main(String[] args) {

		String commentType = Const.INLINE;

		int numberExamples = 400;

		getRandomQuestionFeatures(commentType, numberExamples);
	}
}
