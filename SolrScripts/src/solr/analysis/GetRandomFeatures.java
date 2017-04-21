package solr.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import solr.basics.Tuple;
import solr.utils.Const;
import solr.utils.Utils;

public class GetRandomFeatures {

	public static void getRandomFeatures(String commentType, String feature, int numberExamples) {

		String filePath = "";

		if (commentType.equalsIgnoreCase(Const.GENERAL)) {

			filePath = Const.DIR_RESULTS + Const._GC + Const.SLASH;

		} else if (commentType.equalsIgnoreCase(Const.INLINE)) {

			filePath = Const.DIR_RESULTS + Const._IC + Const.SLASH;
		}

		List<Tuple> hedges = Utils.readTulpe(filePath + Const.HEDGES + Const._TUPLES_ID + Const._TXT);

		Map<String, String> map = new HashMap<String, String>();

		for (Tuple tuple : hedges) {

			if (tuple.getFeature().equalsIgnoreCase(feature) || feature == null) {

				map.put(tuple.getCommentID(), tuple.getFeature());

			}
		}

		StringBuffer sbOutput = new StringBuffer();

		for (int i = 0; i < numberExamples; i++) {

			Random random = new Random();

			List<String> keys = new ArrayList<String>(map.keySet());
			
			String randomKey = keys.get(random.nextInt(keys.size()));
			
			sbOutput.append("id:" + randomKey + " ");
			
			map.remove(randomKey);
		}
		
		System.out.println(sbOutput);

	}

	public static void main(String[] args) {

		String commentType = Const.GENERAL;

		String feature = null;

		int numberExamples = 25;

		getRandomFeatures(commentType, feature, numberExamples);
	}
}
