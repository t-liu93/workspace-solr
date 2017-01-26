package solr.utils;

public class Utils {

	public static boolean isBot(Long authorID) {

		boolean isBot = false;

		if (authorID == Const.ANDROID_BOT_TREEHUGGER 
				|| authorID == Const.ANDROID_BOT_DECKARD
				|| authorID == Const.ANDROID_BOT_ANONYMOUS 
				|| authorID == Const.ANDROID_BOT_BIONIC
				|| authorID == Const.ANDROID_BOT_ANDROID_MERGER
				|| authorID == Const.ANDROID_BOT_ANDROID_DEVTOOLS 
				|| authorID == Const.ANDROID_BOT_GERRIT) {
			isBot = true;
		}

		return isBot;
	}
}
