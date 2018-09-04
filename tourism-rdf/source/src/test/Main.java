package test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import semanticdw.Aggregation;
import semanticdw.QFileParser;
import semanticdw.QuestOWLExample;

public class Main {

	private static final String QUERYFILETOURISM = "res/tourism/tourism_v2.q";

	private static QFileParser tourismQueries;

	private static void readQueryFiles() throws IOException {
		tourismQueries = new QFileParser(QUERYFILETOURISM);
	}

	public static void main(String[] args) {

		try {
			readQueryFiles();

			List<String> codes = tourismQueries.getCodes();

			QuestOWLExample example = new QuestOWLExample();
			String sparqlQuery = codes.get(1);
			example.runQuery(sparqlQuery);

			List<List<String>> result = example.getResults();
			if (result.size() == 0)
				throw new Exception("Query did not return any result");

			Aggregation count = new Aggregation(result);
			Map<List<String>, Integer> groupings = count.getResult();
			for (Entry<List<String>, Integer> e : groupings.entrySet()) {
				System.out.println("group:" + e.getKey() + " -> " + e.getValue());
			}

		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

}
