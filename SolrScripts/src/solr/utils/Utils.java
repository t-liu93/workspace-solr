package solr.utils;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
}
