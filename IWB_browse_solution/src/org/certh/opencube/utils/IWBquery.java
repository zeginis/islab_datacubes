package org.certh.opencube.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryException;

import com.fluidops.iwb.api.EndpointImpl;
import com.fluidops.iwb.api.ReadDataManager;
import com.fluidops.iwb.api.query.QueryBuilder;

public class IWBquery {

	public static TupleQueryResult executeSelect(String query) {
		ReadDataManager dm = EndpointImpl.api().getDataManager();
		QueryBuilder<TupleQuery> queryBuilder = QueryBuilder
				.createTupleQuery(query);

		TupleQueryResult res = null;
		System.out.println(query);
		long startTime = System.currentTimeMillis();
		try {
			TupleQuery tulpequery = queryBuilder.build(dm);
			res = tulpequery.evaluate();
		} catch (MalformedQueryException e) {
			e.printStackTrace();
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		}

		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		System.out.println("Time: " + elapsedTime);

		return res;
	}

	public static List<LDResource> getDataCubeDimensions(String dataCubeURI) {

		String getCubeDimensions_query = "select  distinct ?dim ?label where {"
				+ dataCubeURI
				+ " <http://purl.org/linked-data/cube#structure> ?dsd."
				+ "?dsd <http://purl.org/linked-data/cube#component>  ?cs."
				+ "?cs <http://purl.org/linked-data/cube#dimension>  ?dim."
				+ "OPTIONAL {?dim <http://www.w3.org/2000/01/rdf-schema#label> ?label}}";

		TupleQueryResult res = executeSelect(getCubeDimensions_query);
		List<LDResource> cubeDimensions = new ArrayList<LDResource>();

		try {
			while (res.hasNext()) {
				BindingSet bindingSet = res.next();
				LDResource ldr = new LDResource(bindingSet.getValue("dim").stringValue());
				if (bindingSet.getValue("label") != null) {
					ldr.setLabel(bindingSet.getValue("label").stringValue());
				}
				cubeDimensions.add(ldr);
			}
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		}
		return cubeDimensions;
	}

	public static List<String> getDataCubeMeasure(String dataCubeURI) {

		String getCubeMeasure_query = "select  distinct ?dim where {"
				+ dataCubeURI
				+ " <http://purl.org/linked-data/cube#structure> ?dsd."
				+ "?dsd <http://purl.org/linked-data/cube#component>  ?cs."
				+ "?cs <http://purl.org/linked-data/cube#measure>  ?dim.}";

		TupleQueryResult res = executeSelect(getCubeMeasure_query);

		List<String> cubeMeasure = new ArrayList<String>();

		try {
			while (res.hasNext()) {
				BindingSet bindingSet = res.next();
				cubeMeasure.add(bindingSet.getValue("dim").stringValue());
			}
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		}
		return cubeMeasure;
	}

	public static List<LDResource> getDimensionValues(String dimensionURI) {

		String getDimensionValues_query = "select  distinct ?value ?label where {"
				+ "?observation <"
				+ dimensionURI
				+ "> ?value."
				+ "OPTIONAL {?value <http://www.w3.org/2000/01/rdf-schema#label> ?label}}";

		TupleQueryResult res = executeSelect(getDimensionValues_query);
		List<LDResource> dimensionValues = new ArrayList<LDResource>();

		try {
			while (res.hasNext()) {
				BindingSet bindingSet = res.next();
				LDResource resource = new LDResource();

				Value label = bindingSet.getValue("label");
				if (label != null) {
					resource.setLabel(label.stringValue());
				}
				resource.setURI(bindingSet.getValue("value").stringValue());
				dimensionValues.add(resource);
			}
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		}
		Collections.sort(dimensionValues);
		return dimensionValues;
	}

	public static TupleQueryResult get2DVisualsiationValues(
			List<LDResource> visualDims,
			HashMap<LDResource, LDResource> fixedDims, List<String> measures,
			String cubeURI) {

		String sparql_query = "Select ";
		int i = 1;
		// Add variables ?dim to SPARQL query
		for (LDResource vDim : visualDims) {
			sparql_query += "?dim" + i + " ";
			i++;
		}

		// Select observations of a specific cube (cubeURI)
		sparql_query += "?measure where{ ?obs <http://purl.org/linked-data/cube#dataSet> "
				+ cubeURI + ".";

		i = 1;
		// Add free dimensions to where clause
		for (LDResource vDim : visualDims) {
			sparql_query += "?obs <" + vDim.getURI() + "> " + "?dim" + i + ". ";
			i++;
		}

		// Add fixed dimensions to where clause, select the first value of the
		// list of all values
		for (LDResource fDim : fixedDims.keySet()) {
			sparql_query += "?obs <" + fDim.getURI() + "> <"
					+ fixedDims.get(fDim).getURI() + ">.";
		}

		sparql_query += "?obs  <" + measures.get(0) + "> ?measure}";
		TupleQueryResult res = executeSelect(sparql_query);

		return res;

	}

}
