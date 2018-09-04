package semanticdw;

/*
 * #%L
 * ontop-quest-owlapi3
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLConnection;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLFactory;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLResultSet;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLStatement;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

public class QuestOWLExample {

	private static final String OWLFILE = "res/tourism/tourism_v2.owl";
	private static final String OBDAFILE = "res/tourism/tourism_v2.obda";

	String sqlQuery = null;
	List<List<String>> results = null;
	List<String> signature = null;
	String log = null;

	public List<String> getSignature() {
		return signature;
	}

	public String getLog() {
		return log;
	}

	public List<List<String>> getResults() {
		return results;
	}

	public String getOwlfile() {
		return OWLFILE;
	}

	public String getObdafile() {
		return OBDAFILE;
	}

	public String getSqlQuery() {
		return sqlQuery;
	}

	public void runQuery(final String sparqlQuery) throws Exception {

		/*
		 * Load the ontology from an external .owl file.
		 */
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(OWLFILE));

		/*
		 * Load the OBDA model from an external .obda file
		 */
		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
		OBDAModel obdaModel = fac.getOBDAModel();
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(OBDAFILE);

		/*
		 * Prepare the configuration for the Quest instance. The example below shows the setup for
		 * "Virtual ABox" mode
		 */
		QuestPreferences preference = new QuestPreferences();
		preference.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);

		/*
		 * Create the instance of Quest OWL reasoner.
		 */
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);
		factory.setPreferenceHolder(preference);
		QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

		/*
		 * Prepare the data connection for querying.
		 */
		QuestOWLConnection conn = reasoner.getConnection();
		QuestOWLStatement st = conn.createStatement();

		try {

			/*
			 * Collect the query summary
			 */
			QuestOWLStatement qst = st;
			String sqlQuery = qst.getUnfolding(sparqlQuery);
			this.sqlQuery = sqlQuery;

			/*
			 * Collect result set
			 */
			results = new ArrayList<List<String>>();
			QuestOWLResultSet rs = st.executeTuple(sparqlQuery);

			signature = rs.getSignature();
			int columnSize = rs.getColumnCount();
			while (rs.nextRow()) {
				List<String> row = new ArrayList<String>();
				for (int idx = 1; idx <= columnSize; idx++) {
					OWLObject binding = rs.getOWLObject(idx);
					if (binding == null)
						row.add("**null**");
					else
						row.add(binding.toString());

				}
				results.add(row);
			}
			rs.close();
		} catch (Exception e) {
			this.log = e.getMessage();
			throw e;
		} finally {

			/*
			 * Close connection and resources
			 */
			if (st != null && !st.isClosed()) {
				st.close();
			}
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}
			reasoner.dispose();
		}
	}

	public static void main(String[] args) throws Exception {
		QuestOWLExample example = new QuestOWLExample();
		String sparqlQuery = "select * where {?a ?b ?c .} limit 5";
		example.runQuery(sparqlQuery);

		List<List<String>> result = example.getResults();

		System.out.println(result);

		System.out.println(example.getSignature());
	}

}
