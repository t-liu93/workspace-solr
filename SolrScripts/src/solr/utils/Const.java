/**
 * 
 */
package solr.utils;

/**
 * <h1>Constants</h1>
 * 
 * The Constants class contains all constants used in the program.
 *
 * @author Felipe Ebert
 * @version 0.1
 * @since 2016-12-23
 */
public class Const {

	public static String[] features = { "nonverbals", "meta", "I-statements", "hedges", "hypotheticals", "probables" };

	public static String EXCLUDE_BOTS = " NOT author._account_id:" + Const.ANDROID_BOT_TREEHUGGER
			+ " NOT author._account_id:" + Const.ANDROID_BOT_DECKARD + " NOT author._account_id:"
			+ Const.ANDROID_BOT_ANONYMOUS + " NOT author._account_id:" + Const.ANDROID_BOT_BIONIC
			+ " NOT author._account_id:" + Const.ANDROID_BOT_ANDROID_MERGER + " NOT author._account_id:"
			+ Const.ANDROID_BOT_ANDROID_DEVTOOLS + " NOT author._account_id:\\" + Const.ANDROID_BOT_GERRIT;

	public static String URL_SORL = "http://localhost:8983/solr/gettingstarted";

	public static String URL_GERRIT = "https://android-review.googlesource.com/#/c/";

	public static String _NUMBER = "_number";
	
	public static String HEDGES = "hedges";
	
	public static String HYPOTHETICALS = "hypotheticals";
	
	public static String I_STATEMENTS = "I-statements";
	
	public static String META = "meta";
	
	public static String NONVERBALS = "nonverbals";
	
	public static String PROBABLES = "probables";
	
	public static String QUESTIONS = "questions";

	public static String ID = "id";

	public static String CODE_REVIEW_ID = "code_review";

	public static String AUTHOR_ID = "author._account_id";

	public static String AUTHOR_NAME = "author.name";
	
	public static String AUTHOR_EMAIL = "author.email";

	public static String PATCH_SET = "patch_set";

	public static String REVISION_NUMBER = "_revision_number";

	public static String MESSAGE = "message";

	public static String ANNOTATORS = "annotators";
	
	public static String STANFORD_NLP_ANNOTATORS = "tokenize, ssplit, parse";

	public static String LINE = "line";

	public static String FILE = "file";
	
	public static String TOTAL = "Total";
	
	public static String CSV_HEADLINE = "Feature;Number of comments;Number of features";
	
	public static String CSV_HEADLINE_COMMENTS = "Feature;Number of comments";
	
	public static String CSV_HEADLINE_EMAILS = "ID,Name,Email";
	
	public static String EMAILS = "emails";
	
	public static String GENERAL = "general";
	
	public static String INLINE = "inline";
	
	public static String CONFUSION_IDS = "hedges-confusion-id";
	
	public static String ALL = "all";
	
	public static String SQ = "SQ";
	
	public static String SBARQ = "SBARQ";

	public static String SEMICOLON = ";";
	
	public static String COMMA = ",";

	public static String TRIPLE_HASTAG = "###";

	public static String SPACE = " ";
	
	public static String DASH = "-";

	public static String TWO_DOTS = ":";

	public static String STAR_TWODOTS_START = "*:*";

	public static final String DOT = ".";

	public static String SLASH = "/";

	public static String AT = "@";

	public static String DOUBLE_QUOTES = "\"";
	
	public static String EMPTY_STRING = "";

	public static String BRACKET_LEFT = "[";

	public static String BRACKET_RIGHT = "]";

	public static String DOT_STAR = ".*";

	public static String JAVA_REGEX = "\\.*";

	public static String DIR_FRAMEWORK = "./framework/";

	public static String DIR_RESULTS = "./results";
	
	public static String DIR_SEARCHQUESTION = "searchQuestion/";

	public static String _TXT = ".txt";
	
	public static String _XML = ".xml";
	
	public static String _XLS = ".xls";

	public static String _CSV = ".csv";
	
	public static String _ARFF = ".arff";

	public static String _HTML = ".html";
	
	public static String _JSON = ".json";

	public static String _OUT = "-out";

	public static String _EX = "-ex";

	public static String _IC = "-ic";

	public static String _GC = "-gc";
	
	public static String _ID_TXT = "-ID.txt";
	
	public static String __ID_TXT = "_ID.txt";
	
	public static String _TUPLESID_TXT = "-TuplesID.txt";
	
	public static String __TUPLESID_TXT = "_TuplesID.txt";
	
	public static String _OUT_CSV = "-out.csv";
	
	public static String __OUT_CSV = "_out.csv";
	
	public static String TRAINING_SET = "training-set";
	
	public static String VERIFYING_SET = "verifying-set";
	
	public static String VERIFYING_ = "verifying-";
	
	public static String OTHER = "other";
	
	public static String SET = "-set";

	public static String DIR_TRAINING = "set-training/";
	
	public static String DIR_VERIFYING = "set-verifying/";
	
	public static String TRAINING_SET_IDS = "training-set-ID";
	
	public static String _RESULTS = "-results";
	
	public static String EMAIL_LIST = "emails";
	
	public static String SOURCES = "sources";
	
	public static String LAKOFF = "Jordan";

	public static String JORDAN = "Lakoff";
	
	public static String HOLMES = "Holmes";
	
	public static String EBERT = "Ebert";
	
	public static String _STRATIFIED = "stratified";
	
	public static String CONFUSION = "confusion";
	
	public static String REASONING = "reasoning";
	
	public static String COMMENT = "comment";

	public static String _ID = "-ID";
	
	public static String _TUPLES_ID = "-TuplesID";

	public static String _UTF_8 = "UTF-8";

	public static String NEW_LINE = "\n";
	
	public static String DateFormat = "yyyy/MM/dd HH:mm:ss";

	public static int _0 = 0;
	
	public static int _1 = 1;
	
	public static int _2 = 2;
	
	public static int _3 = 3;

	public static int _100 = 100;
	
	public static int _500 = 500;

	public static int _3000 = 3000;
	
	public static int _5000 = 5000;

	public static int _10000 = 10000;
	
	public static int _20000 = 20000;

	public static int _50000 = 50000;

	public static final String LOG_DATE_FORMAT = "y-MM-dd-hhmmss";

	public static final String DIR_LOGS = "./logs/";

	public static final String LOG_EXTENSION = ".log";

	// android bot: Treehugger Robot => id = 1062513
	public static final int ANDROID_BOT_TREEHUGGER = 1062513;

	// android bot: Deckard Autoverifier => id = 1062513
	public static final int ANDROID_BOT_DECKARD = 1006433;

	// android bot: Anonymous Coward => id = 1017753
	public static final int ANDROID_BOT_ANONYMOUS = 1017753;

	// android bot: Bionic Buildbot => id = 1057783
	public static final int ANDROID_BOT_BIONIC = 1057783;

	// android bot: Android Merger => id = 1041063
	public static final int ANDROID_BOT_ANDROID_MERGER = 1041063;

	// android bot: Android Devtools Build => id = 1076253
	public static final int ANDROID_BOT_ANDROID_DEVTOOLS = 1076253;

	// android bot: Gerrit Code Review => id = -1
	public static final int ANDROID_BOT_GERRIT = -1;

}
