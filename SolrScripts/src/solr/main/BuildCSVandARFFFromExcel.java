package solr.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import solr.utils.Const;

public class BuildCSVandARFFFromExcel {

	public static String replaceBreakLine(String str) {
		String replaced = str;
		if (replaced.contains("\n") || str.contains("\r")) {
			replaced = str.replaceAll("\\r\\n|\\r|\\n", " ");
		}
		return replaced;
	}

	public static String replaceDoubleQuotes(String str) {
		String replaced = str;
		if (replaced.contains("\"")) {
			replaced = str.replaceAll("\"", "\\\\\"");
		}
		return replaced;
	}

	public static String replaceNumbers(String str) {
		//Pattern pattern = Pattern.compile("[*0-9]+");
		Pattern pattern = Pattern.compile("-?\\d+");
		Matcher matcher = pattern.matcher(str);
		while (matcher.find()) {
			String match = matcher.group();
			str = str.replace(match, "NUMBER");
		}
		return str;

//		String replaced = str;
//		String[] array = str.split(" ");
//		for (String word : array) {
//			//TODO HERE!!!
//		}
//		for (int i = 0; i < str.length(); i++) {
//			char c = str.charAt(i);
//
//			if (Character.isDigit(c)) {
//				replaced = replaced.replace(c, 'N');
//			}
//		}
//		return replaced;
	}

	public static String replacteUsers(String str) {
		if (Pattern.compile(Pattern.quote("danalbert's"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)danalbert's", "@USER");
		}
		if (Pattern.compile(Pattern.quote("cferris'"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)cferris'", "@USER");
		}
		if (Pattern.compile(Pattern.quote("host's"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)host's", "@HOSTS");
		}
		if (Pattern.compile(Pattern.quote("Mark's"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)Mark's", "@USER");
		}
		if (Pattern.compile(Pattern.quote("api's"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)api's", "@APIS");
		}
		if (Pattern.compile(Pattern.quote("change's"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)change's", "@CHANGES");
		}
		if (Pattern.compile(Pattern.quote("scripts'"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)scripts'", "@SCRIPTS");
		}
		if (Pattern.compile(Pattern.quote("Google's"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)Google's", "@GOOGLES");
		}
		if (Pattern.compile(Pattern.quote("restorecon's"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)restorecon's", "@RESTORECONS");
		}
		if (Pattern.compile(Pattern.quote("Daniel's"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)Daniel's", "@USER");
		}
		if (Pattern.compile(Pattern.quote("jarjar'd"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)jarjar'd", "@JARJARD");
		}
		if (Pattern.compile(Pattern.quote("Android's"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)Android's", "@ANDROIDS");
		}
		if (Pattern.compile(Pattern.quote("Dan's"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)Dan's", "@USER");
		}
		if (Pattern.compile(Pattern.quote("libbase's"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)libbase's", "@LIBBASES");
		}
		if (Pattern.compile(Pattern.quote("people's"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)people's", "@PEOPLES");
		}
		if (Pattern.compile(Pattern.quote("neil's"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)neil's", "@USER");
		}
		return str;
	}

	public static String replacteMetatokens(String str) {
		if (Pattern.compile(Pattern.quote("won't"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)won't", "WONT");
		}
		if (Pattern.compile(Pattern.quote("it's"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)it's", "ITS");
		}
		if (Pattern.compile(Pattern.quote("I'm"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)I'm", "IAM");
		}
		if (Pattern.compile(Pattern.quote("I've"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)I've", "IHAVE");
		}
		if (Pattern.compile(Pattern.quote("that's"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)that's", "THATS");
		}
		if (Pattern.compile(Pattern.quote("don't"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)don't", "DONT");
		}
		if (Pattern.compile(Pattern.quote("can't"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)can't", "CANT");
		}
		if (Pattern.compile(Pattern.quote("didn't"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)didn't", "DIDNT");
		}
		if (Pattern.compile(Pattern.quote("hadn't"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)hadn't", "HADNT");
		}
		if (Pattern.compile(Pattern.quote("I'll"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)I'll", "IWILL");
		}
		if (Pattern.compile(Pattern.quote("doesn't"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)doesn't", "DOESNT");
		}
		if (Pattern.compile(Pattern.quote("dosen't"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)dosen't", "DOESNT");
		}
		if (Pattern.compile(Pattern.quote("It'd"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)It'd", "ITWOULD");
		}
		if (Pattern.compile(Pattern.quote("here's"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)here's", "HEREIS");
		}
		if (Pattern.compile(Pattern.quote("what's"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)what's", "WHATS");
		}
		if (Pattern.compile(Pattern.quote("let's"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)let's", "LETS");
		}
		if (Pattern.compile(Pattern.quote("shouldn't"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)shouldn't", "SHOULDNT");
		}
		if (Pattern.compile(Pattern.quote("wasn't"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)wasn't", "WASNT");
		}
		if (Pattern.compile(Pattern.quote("we'd"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)we'd", "WEWOULD");
		}
		if (Pattern.compile(Pattern.quote("I'd"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)I'd", "IWOULD");
		}
		if (Pattern.compile(Pattern.quote("we've"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)we've", "WEHAVE");
		}
		if (Pattern.compile(Pattern.quote("you're"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)you're", "YOUARE");
		}
		if (Pattern.compile(Pattern.quote("we're"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)we're", "WEARE");
		}
		if (Pattern.compile(Pattern.quote("they're"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)they're", "THEYARE");
		}
		if (Pattern.compile(Pattern.quote("aren't"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)aren't", "ARENT");
		}
		if (Pattern.compile(Pattern.quote("that'd"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)that'd", "THATWOULD");
		}
		if (Pattern.compile(Pattern.quote("isn't"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)isn't", "ISNT");
		}
		if (Pattern.compile(Pattern.quote("haven't"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)haven't", "HAVENT");
		}
		if (Pattern.compile(Pattern.quote("you'd"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)you'd", "YOUWOULD");
		}
		if (Pattern.compile(Pattern.quote("hasn't"), Pattern.CASE_INSENSITIVE).matcher(str).find()) {
			str = str.replaceAll("(?i)hasn't", "HASNT");
		}
		return str;
	}

	public static String replaceURLs(String text) {
		String str = text;
		String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
		Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
		Matcher urlMatcher = pattern.matcher(text);

		while (urlMatcher.find()) {
			String url = text.substring(urlMatcher.start(0), urlMatcher.end(0));
			str = str.replace(url, "@URL");
		}

		return str;
	}

	public static void buildCSVandARFFFromExcel(String excelFileLocation, String excelFileName) {

		Workbook workbook = null;

		try {

			StringBuffer sbCsv = new StringBuffer();

			sbCsv.append("comment,confusion\n");

			StringBuffer sbArff = new StringBuffer();

			sbArff.append("@relation hedges\n\n" + "@attribute comment string\n"
					+ "@attribute confusionclass {confusion, no_confusion}\n\n" + "@data\n");

			workbook = Workbook.getWorkbook(new File(excelFileLocation + excelFileName));

			Sheet sheet = workbook.getSheet(0);

			for (int i = 0; i < 396; i++) {

				Cell cell1 = sheet.getCell(0, i);

				String comment = cell1.getContents();

				// remove breaklines: "\\r\\n|\\r|\\n" ==> " "
				comment = replaceBreakLine(comment);

				// replace metatokens
				comment = replacteMetatokens(comment);

				// replace users
				comment = replacteUsers(comment);

				// replace URLs
				comment = replaceURLs(comment);

				// replace numbers
				comment = replaceNumbers(comment);

				// escape double quotes: " ==> \"
				comment = replaceDoubleQuotes(comment);

				Cell cell2 = sheet.getCell(1, i);

				String label = cell2.getContents();

				sbCsv.append("\"" + comment + "\"" + "," + label + "\n");
				sbArff.append("\"" + comment + "\"" + "," + label + "\n");
			}

			String outputCsv = excelFileName.substring(0, 17);

			try (Writer writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(excelFileLocation + outputCsv + Const._CSV), Const._UTF_8))) {
				writer.write(sbCsv.toString());
			} catch (Exception e) {
				System.out.println(e);
			}

			String outputArff = excelFileName.substring(0, 17);

			try (Writer writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(excelFileLocation + outputArff + Const._ARFF), Const._UTF_8))) {
				writer.write(sbArff.toString());
			} catch (Exception e) {
				System.out.println(e);
			}

		} catch (IOException | BiffException e) {
			e.printStackTrace();
		} finally {
			if (workbook != null) {
				workbook.close();
			}
		}

		System.out.println("Done with buildCSVandARFFFromExcel...");
	}

	public static void main(String[] args) {

		String excelFilePath = "C:/Users/febert/Dropbox/fifo/Doutorado/Papers Published/ICSME 2017 - Code Reviews/"
				+ "code-review/data - manual labeling/general-comments/hedges/";

		String excelFileName = "classified-hedges.xls";

		buildCSVandARFFFromExcel(excelFilePath, excelFileName);
	}
}
