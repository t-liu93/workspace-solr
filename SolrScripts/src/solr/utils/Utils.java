package solr.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

public class Utils {

	public static boolean isBot(Long authorID) {

		boolean isBot = false;

		if (authorID == Const.ANDROID_BOT_TREEHUGGER || authorID == Const.ANDROID_BOT_DECKARD
				|| authorID == Const.ANDROID_BOT_ANONYMOUS || authorID == Const.ANDROID_BOT_BIONIC
				|| authorID == Const.ANDROID_BOT_ANDROID_MERGER || authorID == Const.ANDROID_BOT_ANDROID_DEVTOOLS
				|| authorID == Const.ANDROID_BOT_GERRIT) {
			isBot = true;
		}

		return isBot;
	}

	public static int countWordFrequency(String feature, String comment) {
		
		int matches = 0;

		Matcher matcher = Pattern.compile("\\b" + feature + "\\b", Pattern.CASE_INSENSITIVE).matcher(comment);

		while (matcher.find()) {
			matches++;
		}
		
		return matches;		
	}
	
	public static List<String> getCodeReviewIDs() {

		List<String> codeReviewIDs = new ArrayList<String>();

		try {
			codeReviewIDs = Files.readAllLines(Paths.get("./results/code-review-id-list.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return codeReviewIDs;
	}

	public static List<String> splitParagraphIntoSentences(String text) {

		List<String> sentences = new ArrayList<String>();

		BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);

		iterator.setText(text);

		int start = iterator.first();

		for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
			sentences.add(text.substring(start, end));
		}

		return sentences;
	}
	
	public static boolean checkCodeReview(SolrClient solr, int codeReviewID) {

		boolean exists = false;

		try {

			String queryString = "code_review:" + codeReviewID;

			SolrQuery query = new SolrQuery();

			query.setQuery(queryString);

			query.setFields("code_review");

			QueryResponse response = solr.query(query);

			SolrDocumentList results = response.getResults();

			long numCodeReviewsFound = results.getNumFound();

			if (numCodeReviewsFound > 0) {
				exists = true;
			}

		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
		}

		return exists;
	}
}
