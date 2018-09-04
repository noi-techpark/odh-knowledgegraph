package semanticdw;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;

public class DBPedia {

	private static final String QUERYFILEDBPEDIA = "res/tourism/dbpedia.q";
	private static QFileParser dbpediaQueries;
	private Map<String, String> bindingsUnique;
	private String town;
	private String thumbnail;

	public DBPedia() throws IOException {
		dbpediaQueries = new QFileParser(QUERYFILEDBPEDIA);
	}

	public Map<String, String> getResult() {
		return bindingsUnique;
	}

	public String getTown() {
		return this.town;
	}

	public String getThumbnail() {
		return this.thumbnail;
	}

	public void fetchDBPedia(String town) {

		town = QFileParser.replaceLiteralDatatype(town);
		town = town.trim();

		this.town = town;

		String sparqlEndpoint = "http://dbpedia.org/sparql";
		Repository repo = new SPARQLRepository(sparqlEndpoint);
		repo.initialize();

		RepositoryConnection conn = repo.getConnection();
		List<String> codes = dbpediaQueries.getCodes();
		String queryString = codes.get(1);
		queryString = queryString.replace("Bozen", town);

		TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
		TupleQueryResult res = tupleQuery.evaluate();
		bindingsUnique = new HashMap<>();

		BindingSet bindingSet = null;
		try {
			while (res.hasNext()) {
				bindingSet = res.next();
				bindingsUnique.put(bindingSet.getValue("x").stringValue(), bindingSet.getValue("y").stringValue());
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

		if (bindingSet != null) {
			thumbnail = bindingSet.getValue("thumbnail").stringValue();
		}
	}
}
