package org.certh.opencube.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.openrdf.model.util.LiteralUtil;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLConnection;
import org.openrdf.repository.sparql.SPARQLRepository;

import com.fluidops.iwb.api.EndpointImpl;
import com.fluidops.iwb.api.ReadDataManager;
import com.fluidops.iwb.api.query.QueryBuilder;

public class IWBquery {

	// FUSEKI
	// private static String service_url =
	// "<http://localhost:3031/dcbrowser/query>";

	// private static String
	// service_url="<http://eurostat.linked-statistics.org/dataset/query>";

	// VIRTUOSO
	// private static String service_url="<http://localhost:8890/sparql>";

	// Execute a SPARQL select
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
		System.out.println("Query Time: " + elapsedTime);

		return res;
	}

	// Execute a SPARQL ASK
	public static boolean executeASK(String query) {
		ReadDataManager dm = EndpointImpl.api().getDataManager();
		QueryBuilder<BooleanQuery> queryBuilder = QueryBuilder
				.createBooleanQuery(query);

		System.out.println(query);
		long startTime = System.currentTimeMillis();

		boolean result = false;
		try {
			BooleanQuery tulpequery = queryBuilder.build(dm);
			result = tulpequery.evaluate();
		} catch (MalformedQueryException e) {

			e.printStackTrace();
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		}

		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		System.out.println("Query Time: " + elapsedTime);

		return result;
	}

	
	public static boolean executeASK_direct(String query,String endpointUrl){
		SPARQLRepository repo=new SPARQLRepository(endpointUrl);
		RepositoryConnection con=new SPARQLConnection(repo);
		
		boolean result = false;
		BooleanQuery booleanQuery;
		try {
			booleanQuery = con.prepareBooleanQuery(QueryLanguage.SPARQL, query);
			result = booleanQuery.evaluate();
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (MalformedQueryException e) {
			e.printStackTrace();
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		}
		
		return result;
			
	}

	public static TupleQueryResult executeSelect_direct(String query,String endpointUrl){
		SPARQLRepository repo=new SPARQLRepository(endpointUrl);
		RepositoryConnection con=new SPARQLConnection(repo);
		
		TupleQueryResult res = null;
	
		try {
			TupleQuery tulpequery = con.prepareTupleQuery(QueryLanguage.SPARQL, query);
			res = tulpequery.evaluate();
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (MalformedQueryException e) {
			e.printStackTrace();
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		}
		
		return res;
							
	}
	
	// Get all the dimensions of a data cube
	public static List<LDResource> getDataCubeDimensions(String dataCubeURI,
			String SPARQLservice) {

		String getCubeDimensions_query = "PREFIX qb: <http://purl.org/linked-data/cube#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" 
				+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"
				+ "select  distinct ?dim ?label ?skoslabel where {";
		if (SPARQLservice != null) {
			getCubeDimensions_query += "SERVICE " + SPARQLservice + " {";
		}

		getCubeDimensions_query += dataCubeURI	+ " qb:structure ?dsd."
				+ "?dsd qb:component  ?cs."
				+ "?cs qb:dimension  ?dim."
				+ "OPTIONAL {?dim rdfs:label ?label." +
				"FILTER (lang(?label) = \"\" || lang(?label) = \"en\")}" +
				"OPTIONAL {?dim skos:prefLabel ?skoslabel." +
				"FILTER (lang(?skoslabel) = \"\" || lang(?skoslabel) = \"en\")}" +
				"OPTIONAL {?dim qb:concept ?cons." +
				"?cons skos:prefLabel ?skoslabel." +
				"FILTER (lang(?skoslabel) = \"\" || lang(?skoslabel) = \"en\")}}";

		if (SPARQLservice != null) {
			getCubeDimensions_query += "}";
		}

		TupleQueryResult res = executeSelect(getCubeDimensions_query);
		List<LDResource> cubeDimensions = new ArrayList<LDResource>();

		try {

			while (res.hasNext()) {

				BindingSet bindingSet = res.next();
				LDResource ldr = new LDResource(bindingSet.getValue("dim")
						.stringValue());
				
				if (bindingSet.getValue("label") != null) {
					ldr.setLabel(bindingSet.getValue("label").stringValue());
				}else if (bindingSet.getValue("skoslabel") != null) {
					ldr.setLabel(bindingSet.getValue("skoslabel").stringValue());
				}
				
				cubeDimensions.add(ldr);
			}
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		}

		return cubeDimensions;
	}

	// Get the dimension of the datacube
	public static List<String> getDataCubeMeasure(String dataCubeURI,
			String SPARQLservice) {

		String getCubeMeasure_query = "PREFIX qb: <http://purl.org/linked-data/cube#>"
						+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" 
						+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"
						+"select  distinct ?dim where {";

		if (SPARQLservice != null) {
			getCubeMeasure_query += "SERVICE " + SPARQLservice + " {";
		}
		getCubeMeasure_query += dataCubeURI +"qb:structure ?dsd."
				+ "?dsd qb:component  ?cs."
				+ "?cs qb:measure  ?dim.}";

		if (SPARQLservice != null) {
			getCubeMeasure_query += "}";
		}

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

	public static List<LDResource> getDimensionValues(String dimensionURI,
			String cubeURI, String SPARQLservice) {

		String getDimensionValues_query = "PREFIX qb: <http://purl.org/linked-data/cube#>"
						+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" 
						+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"
						+"select  distinct ?value ?label ?skoslabel where {";

		if (SPARQLservice != null) {
			getDimensionValues_query += "SERVICE " + SPARQLservice + " {";
		}

		getDimensionValues_query += "?observation qb:dataSet "	+ cubeURI + "."
				+ "?observation <"+ dimensionURI+ "> ?value."
				+ "OPTIONAL {?value rdfs:label ?label." +
				"FILTER (lang(?label) = \"\" || lang(?label) = \"en\")}" +
				"OPTIONAL {?value skos:prefLabel ?skoslabel." +
				"FILTER (lang(?skoslabel) = \"\" || lang(?skoslabel) = \"en\")}}";

		if (SPARQLservice != null) {
			getDimensionValues_query += "}";
		}

		TupleQueryResult res = executeSelect(getDimensionValues_query);
		List<LDResource> dimensionValues = new ArrayList<LDResource>();

		try {
			while (res.hasNext()) {
				BindingSet b = res.next();
				LDResource resource = new LDResource(b.getValue("value").
						stringValue());
				
				if (b.getValue("label") != null) {
					resource.setLabel(b.getValue("label").stringValue());
				}else if (b.getValue("skoslabel") != null) {
					resource.setLabel(b.getValue("skoslabel").stringValue());
				}
				
				dimensionValues.add(resource);
			}
		} catch (QueryEvaluationException e1) {
			e1.printStackTrace();
		}

		Collections.sort(dimensionValues);
		return dimensionValues;
	}

	// ASK if a cube dimension has a specific value
	// (check if exists an observation with this value)
	public static boolean askDimensionValueInDataCube(String dimensionURI,
			String value, String cubeURI, String SPARQLservice) {
		String askDimensionValueInDataCube_query = "ASK where{";
		
		if (SPARQLservice != null) {
			askDimensionValueInDataCube_query += "SERVICE " + SPARQLservice + " {";
		}	
		askDimensionValueInDataCube_query+= " ?obs <http://purl.org/linked-data/cube#dataSet> " + cubeURI
				+ "." + "?obs <" + dimensionURI + "> <" + value + ">}";
		
		if (SPARQLservice != null) {
			askDimensionValueInDataCube_query += "}";
		}

	//	SPARQLservice=SPARQLservice.replaceAll("<","");
	//	SPARQLservice=SPARQLservice.replaceAll(">","");
	//	return executeASK_direct(askDimensionValueInDataCube_query,SPARQLservice);
		return executeASK(askDimensionValueInDataCube_query);
	}

	public static List<LDResource> getDimensionValuesFromCodeList(
			String dimensionURI, String cubeURI, String SPARQLservice) {

		String getDimensionValues_query ="PREFIX qb: <http://purl.org/linked-data/cube#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" 
				+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" 
				+"select  ?value ?label ?skoslabel where {";
		
		if (SPARQLservice != null) {
			getDimensionValues_query += "SERVICE " + SPARQLservice + " {";
		}
				
		getDimensionValues_query+=
				"<"+ dimensionURI+ "> qb:codeList ?cd."
				+ "?cd skos:hasTopConcept ?value."
				+ "OPTIONAL {?value rdfs:label ?label." +
				"FILTER (lang(?label) = \"\" || lang(?label) = \"en\")}" +
				"OPTIONAL {?value skos:prefLabel ?skoslabel." +
				"FILTER (lang(?skoslabel) = \"\" || lang(?skoslabel) = \"en\")}}";
		
		if (SPARQLservice != null) {
			getDimensionValues_query += "}";
		}

		TupleQueryResult res = executeSelect(getDimensionValues_query);
		List<LDResource> dimensionValues = new ArrayList<LDResource>();

		// Create one thread for each dimension value
		//ExecutorService executor = Executors.newCachedThreadPool();
		//List<Future<List<LDResource>>> list = new ArrayList<Future<List<LDResource>>>();
		try {
			while (res.hasNext()) {
				BindingSet b = res.next();
				LDResource resource = new LDResource(b.getValue("value")
						.stringValue());
				

				if (b.getValue("label") != null) {
					resource.setLabel(b.getValue("label").stringValue());
				}else if (b.getValue("skoslabel") != null) {
					resource.setLabel(b.getValue("skoslabel").stringValue());
				}

		/*		Callable<List<LDResource>> worker = new DimValueInCubeThread(
						dimensionURI, b, cubeURI,SPARQLservice);
				Future<List<LDResource>> submit = executor.submit(worker);
				list.add(submit);*/

				// Check if a value from the code list exists in the datacube

		//	if(askDimensionValueInDataCube(dimensionURI,resource.getURI(),cubeURI,SPARQLservice)){
			 dimensionValues.add(resource);
		//		 }
			}

			/*for (Future<List<LDResource>> future : list) {
				try {
					List<LDResource> dimValuesList = future.get();
					dimensionValues.addAll(dimValuesList);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}

			executor.shutdown();*/

		} catch (QueryEvaluationException e1) {
			e1.printStackTrace();
		}

		Collections.sort(dimensionValues);
		return dimensionValues;
	}

	public static HashMap<LDResource, List<LDResource>> getAllDimensionsValues(
			String cubeURI, String SPARQLservice) {

		String getDimensionValues_query = "PREFIX qb: <http://purl.org/linked-data/cube#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" 
				+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"
				+ " select  distinct ?dim ?value ?label ?skoslabel where{";
		
		if (SPARQLservice != null) {
			getDimensionValues_query += "SERVICE " + SPARQLservice + " {";
		}
				
		getDimensionValues_query += cubeURI	+ " qb:structure ?dsd."
				+ " ?dsd qb:component  ?cs."
				+ "?cs qb:dimension  ?dim."
				+ "?observation qb:dataSet "+ cubeURI+ "."
				+ "?observation ?dim ?value. "
				+ "OPTIONAL {?value rdfs:label ?label." +
				"FILTER (lang(?label) = \"\" || lang(?label) = \"en\")}" +
				"OPTIONAL {?value skos:prefLabel ?skoslabel." +
				"FILTER (lang(?skoslabel) = \"\" || lang(?skoslabel) = \"en\")}}";
		
		if (SPARQLservice != null) {
			getDimensionValues_query += "}";
		}

		TupleQueryResult res = executeSelect(getDimensionValues_query);
		HashMap<LDResource, List<LDResource>> dimensionsAndValues = new HashMap<LDResource, List<LDResource>>();

		try {

			while (res.hasNext()) {
				BindingSet b = res.next();

				LDResource d = new LDResource(b.getValue("dim").stringValue());
				LDResource value = null;
				if (b.getValue("label") != null) {
					value = new LDResource(b.getValue("value").stringValue(), b
							.getValue("label").stringValue());
				} else if(b.getValue("skoslabel") != null) {
					value = new LDResource(b.getValue("value").stringValue(), b
							.getValue("skoslabel").stringValue());
				}else {
					value = new LDResource(b.getValue("value").stringValue());
				}
			
				if (dimensionsAndValues.get(d) == null) {
					List<LDResource> dimensionValues = new ArrayList<LDResource>();
					dimensionValues.add(value);
					dimensionsAndValues.put(d, dimensionValues);
				} else {
					List<LDResource> dimensionValues = dimensionsAndValues
							.get(d);
					dimensionValues.add(value);
					dimensionsAndValues.put(d, dimensionValues);
				}
			}
		} catch (QueryEvaluationException e1) {
			e1.printStackTrace();
		}

		return dimensionsAndValues;
	}

	public static TupleQueryResult get2DVisualsiationValues(
			List<LDResource> visualDims,
			HashMap<LDResource, LDResource> fixedDims, List<String> measures,
			HashMap<LDResource, List<LDResource>> alldimensionvalues,
			String cubeURI, String SPARQLservice) {

		String sparql_query = "Select ";
		int i = 1;
		// Add variables ?dim to SPARQL query
		for (LDResource vDim : visualDims) {
			sparql_query += "?dim" + i + " ";
			i++;
		}

		// Select observations of a specific cube (cubeURI)
		sparql_query += "?measure ?obs where{";
		

		if (SPARQLservice != null) {
			sparql_query += "SERVICE " + SPARQLservice + " {";
		}
		
		sparql_query += "?obs <http://purl.org/linked-data/cube#dataSet> "+ cubeURI + ".";

		i = 1;
		// Add free dimensions to where clause
		for (LDResource vDim : visualDims) {
			sparql_query += "?obs <" + vDim.getURI() + "> " + "?dim" + i + ". ";
			i++;
		}

		// Add fixed dimensions to where clause, select the first value of the
		// list of all values
		int j=1;
		for (LDResource fDim : fixedDims.keySet()) {
			sparql_query += "?obs <" + fDim.getURI() + "> ";
			if(fixedDims.get(fDim).getURI().contains("http")){
				sparql_query+="<"+ fixedDims.get(fDim).getURI() + ">.";
			}else{
				sparql_query+="?value_"+j+". FILTER(STR(?value_"+j+
						")='"+fixedDims.get(fDim).getURI()+"')";
			}
			j++;
			
		}
		
		
		

		sparql_query += "?obs  <" + measures.get(0) + "> ?measure.} ";
		
		if (SPARQLservice != null) {
			sparql_query += "}";
		}

		TupleQueryResult res = executeSelect(sparql_query);

		return res;

	}

}
