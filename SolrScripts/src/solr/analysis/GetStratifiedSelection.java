package solr.analysis;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import solr.utils.Const;
import solr.utils.Tuple;

public class GetStratifiedSelection {

	public static void getStratifiedSelection(String commentType) {

		String filePath = "";

		if (commentType.equalsIgnoreCase(Const.GENERAL)) {

			filePath = Const.DIR_RESULTS + Const._GC + Const.SLASH;

		} else if (commentType.equalsIgnoreCase(Const.INLINE)) {

			filePath = Const.DIR_RESULTS + Const._IC + Const.SLASH;
		}

		List<Tuple> hedges = readTulpe(filePath + Const.HEDGES + Const._TUPLES_ID + Const._TXT);

		List<Tuple> hypo = readTulpe(filePath + Const.HYPOTHETICALS + Const._TUPLES_ID + Const._TXT);

		List<Tuple> I_statements = readTulpe(filePath + Const.I_STATEMENTS + Const._TUPLES_ID + Const._TXT);

		List<Tuple> probables = readTulpe(filePath + Const.PROBABLES + Const._TUPLES_ID + Const._TXT);

		List<Tuple> nonverbals = readTulpe(filePath + Const.NONVERBALS + Const._TUPLES_ID + Const._TXT);

		List<Tuple> meta = readTulpe(filePath + Const.META + Const._TUPLES_ID + Const._TXT);

		List<Tuple> hedgesStratified = checkStratifiedIDFromAll(hedges, hypo, I_statements, probables, nonverbals,
				meta);

		List<Tuple> hypoStratified = checkStratifiedIDFromAll(hypo, hedges, I_statements, probables, nonverbals, meta);

		List<Tuple> I_statementsStratified = checkStratifiedIDFromAll(I_statements, hedges, hypo, probables, nonverbals,
				meta);

		List<Tuple> probablesStratified = checkStratifiedIDFromAll(probables, hedges, hypo, I_statements, nonverbals,
				meta);

		List<Tuple> nonverbalsStratified = checkStratifiedIDFromAll(nonverbals, hedges, hypo, I_statements, probables,
				meta);

		List<Tuple> metaStratified = checkStratifiedIDFromAll(meta, hedges, hypo, I_statements, probables, nonverbals);

		writeStratifiedTuplesOutputFile(Const.HEDGES, commentType, hedgesStratified);

		writeStratifiedTuplesOutputFile(Const.HYPOTHETICALS, commentType, hypoStratified);

		writeStratifiedTuplesOutputFile(Const.I_STATEMENTS, commentType, I_statementsStratified);

		writeStratifiedTuplesOutputFile(Const.PROBABLES, commentType, probablesStratified);

		writeStratifiedTuplesOutputFile(Const.NONVERBALS, commentType, nonverbalsStratified);

		writeStratifiedTuplesOutputFile(Const.META, commentType, metaStratified);

		int totalHedges = writeStratifiedTuplesCSVOutputFile(Const.HEDGES, commentType, hedgesStratified);

		int totalHypo = writeStratifiedTuplesCSVOutputFile(Const.HYPOTHETICALS, commentType, hypoStratified);

		int totalI_Statements = writeStratifiedTuplesCSVOutputFile(Const.I_STATEMENTS, commentType,
				I_statementsStratified);

		int totalProbables = writeStratifiedTuplesCSVOutputFile(Const.PROBABLES, commentType, probablesStratified);

		int totalNonverbals = writeStratifiedTuplesCSVOutputFile(Const.NONVERBALS, commentType, nonverbalsStratified);

		int totalMeta = writeStratifiedTuplesCSVOutputFile(Const.META, commentType, metaStratified);

		int total = totalHedges + totalHypo + totalI_Statements + totalProbables + totalNonverbals + totalNonverbals
				+ totalMeta;

		StringBuffer sbOverall = new StringBuffer();
		sbOverall.append(Const.CSV_HEADLINE_COMMENTS);
		sbOverall.append(Const.NEW_LINE);
		sbOverall.append(Const.HEDGES + Const.SEMICOLON + totalHedges);
		sbOverall.append(Const.NEW_LINE);
		sbOverall.append(Const.HYPOTHETICALS + Const.SEMICOLON + totalHypo);
		sbOverall.append(Const.NEW_LINE);
		sbOverall.append(Const.PROBABLES + Const.SEMICOLON + totalProbables);
		sbOverall.append(Const.NEW_LINE);
		sbOverall.append(Const.I_STATEMENTS + Const.SEMICOLON + totalI_Statements);
		sbOverall.append(Const.NEW_LINE);
		sbOverall.append(Const.NONVERBALS + Const.SEMICOLON + totalNonverbals);
		sbOverall.append(Const.NEW_LINE);
		sbOverall.append(Const.META + Const.SEMICOLON + totalMeta);
		sbOverall.append(Const.NEW_LINE);
		sbOverall.append(Const.TOTAL + Const.SEMICOLON + total);

		String resultFile = "";

		if (commentType.equalsIgnoreCase(Const.GENERAL)) {

			resultFile = Const.DIR_RESULTS + Const._GC + Const.SLASH + Const._STRATIFIED + Const.SLASH + Const.GENERAL
					+ Const._RESULTS + Const._CSV;

		} else if (commentType.equalsIgnoreCase(Const.INLINE)) {

			resultFile = Const.DIR_RESULTS + Const._IC + Const.SLASH + Const._STRATIFIED + Const.SLASH + Const.INLINE
					+ Const._RESULTS + Const._CSV;
		}

		try (Writer writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(resultFile), Const._UTF_8))) {
			writer.write(sbOverall.toString());
		} catch (Exception e) {
			System.out.println(e);
		}

		System.out.println("Done with getStratifiedSelection...");
	}

	public static int writeStratifiedTuplesCSVOutputFile(String framework, String commentType, List<Tuple> listTuples) {

		String filePath = "";

		if (commentType.equalsIgnoreCase(Const.GENERAL)) {

			filePath = Const.DIR_RESULTS + Const._GC + Const.SLASH + Const._STRATIFIED + Const.SLASH + framework
					+ Const._TUPLES_ID + Const._CSV;

		} else if (commentType.equalsIgnoreCase(Const.INLINE)) {

			filePath = Const.DIR_RESULTS + Const._IC + Const.SLASH + Const._STRATIFIED + Const.SLASH + framework
					+ Const._TUPLES_ID + Const._CSV;
		}

		Map<String, Integer> map = new HashMap<String, Integer>();

		for (Tuple tuple : listTuples) {

			if (!map.containsKey(tuple.getFeature())) {

				map.put(tuple.getFeature(), 1);

			} else {

				map.put(tuple.getFeature(), map.get(tuple.getFeature()) + 1);
			}
		}

		StringBuffer sbStratifiedOutput = new StringBuffer();

		sbStratifiedOutput.append(Const.CSV_HEADLINE_COMMENTS);

		sbStratifiedOutput.append(Const.NEW_LINE);

		int totalFeatures = 0;

		for (Entry<String, Integer> entry : map.entrySet()) {

			totalFeatures = totalFeatures + entry.getValue();

			sbStratifiedOutput.append(entry.getKey() + Const.SEMICOLON + entry.getValue());

			sbStratifiedOutput.append(Const.NEW_LINE);
		}

		sbStratifiedOutput.append(Const.TOTAL + Const.SEMICOLON + totalFeatures);

		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), Const._UTF_8))) {

			writer.write(sbStratifiedOutput.toString());

		} catch (Exception e) {
			System.out.println(e);
		}

		return totalFeatures;
	}

	public static void writeStratifiedTuplesOutputFile(String framework, String commentType, List<Tuple> listTuples) {

		String filePath = "";

		if (commentType.equalsIgnoreCase(Const.GENERAL)) {

			filePath = Const.DIR_RESULTS + Const._GC + Const.SLASH + Const._STRATIFIED + Const.SLASH + framework
					+ Const._TUPLES_ID + Const._TXT;

		} else if (commentType.equalsIgnoreCase(Const.INLINE)) {

			filePath = Const.DIR_RESULTS + Const._IC + Const.SLASH + Const._STRATIFIED + Const.SLASH + framework
					+ Const._TUPLES_ID + Const._TXT;
		}

		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), Const._UTF_8))) {

			for (Tuple tuple : listTuples) {

				writer.write(tuple.toString());

				writer.write(Const.NEW_LINE);
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static List<Tuple> checkStratifiedIDFromAll(List<Tuple> targetTuples, List<Tuple> sourceTuples1,
			List<Tuple> sourceTuples2, List<Tuple> sourceTuples3, List<Tuple> sourceTuples4,
			List<Tuple> sourceTuples5) {

		List<Tuple> tuplesStratified = new ArrayList<Tuple>();

		for (Tuple tuple : targetTuples) {

			boolean isUnique = true;

			if (isUnique) {
				isUnique = checkStratifiedID(sourceTuples1, tuple, isUnique);
			} else {
				continue;
			}

			if (isUnique) {
				isUnique = checkStratifiedID(sourceTuples2, tuple, isUnique);
			} else {
				continue;
			}

			if (isUnique) {
				isUnique = checkStratifiedID(sourceTuples5, tuple, isUnique);
			} else {
				continue;
			}

			if (isUnique) {
				isUnique = checkStratifiedID(sourceTuples4, tuple, isUnique);
			} else {
				continue;
			}

			if (isUnique) {
				isUnique = checkStratifiedID(sourceTuples3, tuple, isUnique);
			} else {
				continue;
			}

			if (isUnique) {
				tuplesStratified.add(tuple);
			}
		}

		return tuplesStratified;
	}

	public static boolean checkStratifiedID(List<Tuple> tuples, Tuple tuple, boolean isUnique) {

		for (Tuple tmp : tuples) {

			if (tmp.getCommentID().equals(tuple.getCommentID())) {

				isUnique = false;

				break;
			}
		}

		return isUnique;
	}

	public static List<Tuple> readTulpe(String string) {

		List<Tuple> tuples = new ArrayList<Tuple>();

		try {

			List<String> list = Files.readAllLines(Paths.get(string));

			for (String line : list) {

				String[] array = line.split(";");

				tuples.add(new Tuple(array[0], array[1]));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return tuples;
	}

	public static void main(String[] args) {

		String commentType = Const.GENERAL;

		getStratifiedSelection(commentType);
	}
}
