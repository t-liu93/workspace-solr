package solr.analysis;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class RunSolrIndexCmds extends Thread {

	private int start;

	private int end;

	private List<String> solrCmds;

	public RunSolrIndexCmds(int start, int end, List<String> solrCmds) {
		this.start = start;
		this.end = end;
		this.solrCmds = solrCmds;
	}

	@Override
	public void run() {
		try {
			for (int i = start; i < end; i++) {
				Runtime.getRuntime().exec(solrCmds.get(i));
				sleep(5000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Done...");
	}

	public static void main(String[] args) {

		try {

			List<String> solrCmds = Files.readAllLines(Paths.get("./results/code-review-solr-index-cmds.txt"));

			RunSolrIndexCmds worker1 = new RunSolrIndexCmds(5000, 6000, solrCmds);
			RunSolrIndexCmds worker2 = new RunSolrIndexCmds(6000, 7000, solrCmds);
			RunSolrIndexCmds worker3 = new RunSolrIndexCmds(7000, 8000, solrCmds);
			RunSolrIndexCmds worker4 = new RunSolrIndexCmds(8000, 9000, solrCmds);
			RunSolrIndexCmds worker5 = new RunSolrIndexCmds(9000, 10000, solrCmds);

			worker1.start();
			worker2.start();
			worker3.start();
			worker4.start();
			worker5.start();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
