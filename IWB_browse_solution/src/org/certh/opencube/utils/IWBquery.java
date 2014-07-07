package org.certh.opencube.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLConnection;
import org.openrdf.repository.sparql.SPARQLRepository;

import com.fluidops.ajax.components.FHTML;
import com.fluidops.iwb.api.Context;
import com.fluidops.iwb.api.Context.ContextLabel;
import com.fluidops.iwb.api.Context.ContextType;
import com.fluidops.iwb.api.EndpointImpl;
import com.fluidops.iwb.api.ReadDataManager;
import com.fluidops.iwb.api.query.QueryBuilder;

public class IWBquery {

	private static boolean globalDSD = false;
	private static boolean notime = false;

	// Execute a SPARQL select using the native IWB triple store
	// Input: the query to execute
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

	// Execute a SPARQL ASK using the native IWB triple store
	// Input the query to execute
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

	// Execute a SPARQL UPDATE using the native IWB triple store
	// Input the query to execute
	public static void executeUPDATE(String query) {
		ReadDataManager dm = EndpointImpl.api().getDataManager();

		QueryBuilder<Update> queryBuilder = QueryBuilder.createUpdate(query);

		System.out.println(query);
		long startTime = System.currentTimeMillis();

		try {
			Update updatequery = queryBuilder.build(dm);
			updatequery.execute();
		} catch (MalformedQueryException e) {
			e.printStackTrace();
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (UpdateExecutionException e) {
			e.printStackTrace();
		}

		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		System.out.println("Query Time: " + elapsedTime);

	}

	// Execute a SPARQL ASK using an external triple store
	// Input the query to execute and the triple store URI
	public static boolean executeASK_direct(String query, String endpointUrl) {
		SPARQLRepository repo = new SPARQLRepository(endpointUrl);
		RepositoryConnection con = new SPARQLConnection(repo);

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

	// Execute a SPARQL Select using an external triple store
	// Input the query to execute and the triple store URI
	public static TupleQueryResult executeSelect_direct(String query,
			String endpointUrl) {
		SPARQLRepository repo = new SPARQLRepository(endpointUrl);
		RepositoryConnection con = new SPARQLConnection(repo);

		TupleQueryResult res = null;

		try {
			TupleQuery tulpequery = con.prepareTupleQuery(QueryLanguage.SPARQL,
					query);
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
	// Input: The cubeURI, cubeGraph, SPARQL service
	// The cube Graph and SPARQL service can be null if not available
	public static List<LDResource> getDataCubeDimensions(String dataCubeURI,
			String cubeGraph, String cubeDSDGraph, String SPARQLservice) {

		String getCubeDimensions_query = "PREFIX qb: <http://purl.org/linked-data/cube#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"
				+ "select  distinct ?dim ?label ?skoslabel where {";

		// If a SPARQL service is defined
		if (SPARQLservice != null) {
			getCubeDimensions_query += "SERVICE " + SPARQLservice + " {";
		}

		// If a cube graph is defined
		if (cubeGraph != null) {
			getCubeDimensions_query += "GRAPH <" + cubeGraph + "> {";
		}

		getCubeDimensions_query += dataCubeURI + " qb:structure ?dsd.";

		// If a cube graph is defined
		if (cubeGraph != null) {
			getCubeDimensions_query += "}";
		}

		// If a cube DSD graph is defined
		if (cubeDSDGraph != null) {
			getCubeDimensions_query += "GRAPH <" + cubeDSDGraph + "> {";
		}
		getCubeDimensions_query += "?dsd qb:component  ?cs."
				+ "?cs qb:dimension  ?dim."
				+ "OPTIONAL {?dim rdfs:label ?label."
				+ "FILTER (lang(?label) = \"\" || lang(?label) = \"en\")}"
				+ "OPTIONAL {?dim skos:prefLabel ?skoslabel."
				+ "FILTER (lang(?skoslabel) = \"\" || lang(?skoslabel) = \"en\")}"
				+ "OPTIONAL {?dim qb:concept ?cons."
				+ "?cons skos:prefLabel ?skoslabel."
				+ "FILTER (lang(?skoslabel) = \"\" || lang(?skoslabel) = \"en\")}}";

		// If cube DSD graph is defined
		if (cubeDSDGraph != null) {
			getCubeDimensions_query += "}";
		}

		// If a SPARQL service is defined
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

				// check if there is an rdfs:label or skos:prefLabel
				if (bindingSet.getValue("label") != null) {
					ldr.setLabel(bindingSet.getValue("label").stringValue());
				} else if (bindingSet.getValue("skoslabel") != null) {
					ldr.setLabel(bindingSet.getValue("skoslabel").stringValue());
				}

				cubeDimensions.add(ldr);
			}
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		}

		return cubeDimensions;
	}

	// Get all the fixed dimensions of a slice
	// Input: The sliceURI, sliceGraph, SPARQL service
	// The slice Graph and SPARQL service can be null if not available
	public static List<LDResource> getSliceFixedDimensions(String sliceURI,
			String sliceGraph, String cubeDSDGraph, String SPARQLservice) {

		String getSliceDimensions_query = "PREFIX qb: <http://purl.org/linked-data/cube#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"
				+ "select  distinct ?dim ?label ?skoslabel where {";

		// If a SPARQL service is defined
		if (SPARQLservice != null) {
			getSliceDimensions_query += "SERVICE " + SPARQLservice + " {";
		}

		// If a slice graph is defined
		if (sliceGraph != null) {
			getSliceDimensions_query += "GRAPH <" + sliceGraph + "> {";
		}

		getSliceDimensions_query += sliceURI + " qb:sliceStructure ?slicekey. "
				+ "?slicekey qb:componentProperty  ?dim.";

		// If slice graph is defined
		if (sliceGraph != null) {
			getSliceDimensions_query += "}";
		}

		getSliceDimensions_query += "OPTIONAL{";

		// If a cube DSD graph is defined
		if (cubeDSDGraph != null) {
			getSliceDimensions_query += "GRAPH <" + cubeDSDGraph + "> {";
		}
		getSliceDimensions_query += "{?dim rdfs:label ?label."
				+ "FILTER (lang(?label) = \"\" || lang(?label) = \"en\")}"
				+ "UNION {?dim skos:prefLabel ?skoslabel."
				+ "FILTER (lang(?skoslabel) = \"\" || lang(?skoslabel) = \"en\")}"
				+ "UNION {?dim qb:concept ?cons."
				+ "?cons skos:prefLabel ?skoslabel."
				+ "FILTER (lang(?skoslabel) = \"\" || lang(?skoslabel) = \"en\")}}}";

		// If a cube DSD graph is defined
		if (cubeDSDGraph != null) {
			getSliceDimensions_query += "}";
		}

		// If a SPARQL service is defined
		if (SPARQLservice != null) {
			getSliceDimensions_query += "}";
		}

		TupleQueryResult res = executeSelect(getSliceDimensions_query);
		List<LDResource> sliceDimensions = new ArrayList<LDResource>();

		try {

			while (res.hasNext()) {

				BindingSet bindingSet = res.next();
				LDResource ldr = new LDResource(bindingSet.getValue("dim")
						.stringValue());

				// check if there is an rdfs:label or skos:prefLabel
				if (bindingSet.getValue("label") != null) {
					ldr.setLabel(bindingSet.getValue("label").stringValue());
				} else if (bindingSet.getValue("skoslabel") != null) {
					ldr.setLabel(bindingSet.getValue("skoslabel").stringValue());
				}

				sliceDimensions.add(ldr);
			}
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		}

		return sliceDimensions;
	}
	
	// Get the values for all fixed dimensions of a slice
	// Input: The fixed slice dimensions, The sliceURI, sliceGraph, SPARQL service
	// The slice Graph and SPARQL service can be null if not available
	public static HashMap<LDResource, LDResource> getSliceFixedDimensionsValues(
			List<LDResource> sliceFixedDimensions,String sliceURI,String sliceGraph,
			String cubeDSDGraph, String SPARQLservice) {

		HashMap<LDResource,LDResource> sliceFixedDimensionsValues=new HashMap<LDResource, LDResource>();
		
		for(LDResource ldr:sliceFixedDimensions){
			String getSliceFixedDimensionValues_query = "PREFIX qb: <http://purl.org/linked-data/cube#>"
					+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
					+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"
					+ "select  distinct ?fdimvalue ?label ?skoslabel where {";

			// If a SPARQL service is defined
			if (SPARQLservice != null) {
				getSliceFixedDimensionValues_query += "SERVICE " + SPARQLservice + " {";
			}

			// If a slice graph is defined
			if (sliceGraph != null) {
				getSliceFixedDimensionValues_query += "GRAPH <" + sliceGraph + "> {";
			}

			getSliceFixedDimensionValues_query += sliceURI + " <"+ldr.getURI()+"> ?fdimvalue.";

			// If slice graph is defined
			if (sliceGraph != null) {
				getSliceFixedDimensionValues_query += "}";
			}

			getSliceFixedDimensionValues_query += "OPTIONAL{";

			// If a cube DSD graph is defined
			if (cubeDSDGraph != null) {
				getSliceFixedDimensionValues_query += "GRAPH <" + cubeDSDGraph + "> {";
			}
			getSliceFixedDimensionValues_query += "{?fdimvalue rdfs:label ?label."
					+ "FILTER (lang(?label) = \"\" || lang(?label) = \"en\")}"
					+ "UNION {?fdimvalue skos:prefLabel ?skoslabel."
					+ "FILTER (lang(?skoslabel) = \"\" || lang(?skoslabel) = \"en\")}"
					+ "UNION {?fdimvalue qb:concept ?cons."
					+ "?cons skos:prefLabel ?skoslabel."
					+ "FILTER (lang(?skoslabel) = \"\" || lang(?skoslabel) = \"en\")}}}";

			// If a cube DSD graph is defined
			if (cubeDSDGraph != null) {
				getSliceFixedDimensionValues_query += "}";
			}

			// If a SPARQL service is defined
			if (SPARQLservice != null) {
				getSliceFixedDimensionValues_query += "}";
			}

			TupleQueryResult res = executeSelect(getSliceFixedDimensionValues_query);
			

			try {

				while (res.hasNext()) {

					BindingSet bindingSet = res.next();
					LDResource fdimvalue = new LDResource(bindingSet.getValue("fdimvalue")
							.stringValue());

					// check if there is an rdfs:label or skos:prefLabel or the values 
					if (bindingSet.getValue("label") != null) {
						fdimvalue.setLabel(bindingSet.getValue("label").stringValue());
					} else if (bindingSet.getValue("skoslabel") != null) {
						fdimvalue.setLabel(bindingSet.getValue("skoslabel").stringValue());
					}
					sliceFixedDimensionsValues.put(ldr, fdimvalue);
					
				}
			} catch (QueryEvaluationException e) {
				e.printStackTrace();
			}
			
		}		

		return sliceFixedDimensionsValues;
	}

	// Get all cube dimensions from a slice
	// Input: The sliceURI, slice Graph, cube DSD graph, SPARQL service
	// The slice Graph, cube DSD graph and SPARQL service can be null if not available
	public static List<LDResource> getDataCubeDimensionsFromSlice(String sliceURI,
			String sliceGraph, String cubeDSDGraph, String SPARQLservice) {

		String getSliceMeasure_query ="PREFIX qb: <http://purl.org/linked-data/cube#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"
				+ "select  distinct ?dim ?label ?skoslabel where {";

		// If a SPARQL service is defined
		if (SPARQLservice != null) {
			getSliceMeasure_query += "SERVICE " + SPARQLservice + " {";
		}

		// If a cube graph is defined
		if (sliceGraph != null) {
			getSliceMeasure_query += "GRAPH <" + sliceGraph + "> {";
		}

		getSliceMeasure_query += sliceURI + "qb:sliceStructure ?sliceKey."
				+ "?dsd qb:sliceKey ?sliceKey.";

		// If a cube DSD graph is defined
		if (sliceGraph != null) {
			getSliceMeasure_query += "}";
		}

		// If a cube DSD graph is defined
		if (cubeDSDGraph != null) {
			getSliceMeasure_query += "GRAPH <" + cubeDSDGraph + "> {";
		}

		getSliceMeasure_query += "?dsd qb:component  ?cs."
				+ "?cs qb:dimension  ?dim."
				+ "OPTIONAL {?dim rdfs:label ?label."
				+ "FILTER (lang(?label) = \"\" || lang(?label) = \"en\")}"
				+ "OPTIONAL {?dim skos:prefLabel ?skoslabel."
				+ "FILTER (lang(?skoslabel) = \"\" || lang(?skoslabel) = \"en\")}"
				+ "OPTIONAL {?dim qb:concept ?cons."
				+ "?cons skos:prefLabel ?skoslabel."
				+ "FILTER (lang(?skoslabel) = \"\" || lang(?skoslabel) = \"en\")}}";

		// If a cube DSD graph is defined
		if (cubeDSDGraph != null) {
			getSliceMeasure_query += "}";
		}

		// If a SPARQL service is defined
		if (SPARQLservice != null) {
			getSliceMeasure_query += "}";
		}
		

		TupleQueryResult res = executeSelect(getSliceMeasure_query);
		List<LDResource> cubeDimensions = new ArrayList<LDResource>();

		try {

			while (res.hasNext()) {

				BindingSet bindingSet = res.next();
				LDResource ldr = new LDResource(bindingSet.getValue("dim")
						.stringValue());

				// check if there is an rdfs:label or skos:prefLabel
				if (bindingSet.getValue("label") != null) {
					ldr.setLabel(bindingSet.getValue("label").stringValue());
				} else if (bindingSet.getValue("skoslabel") != null) {
					ldr.setLabel(bindingSet.getValue("skoslabel").stringValue());
				}

				cubeDimensions.add(ldr);
			}
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		}

		return cubeDimensions;
		
	}

	// Get all the measure of a data cube
	// Input: The cubeURI, cubeGraph, SPARQL service
	// The cube Graph and SPARQL service can be null if not available
	public static List<String> getDataCubeMeasure(String dataCubeURI,
			String cubeGraph, String cubeDSDGraph, String SPARQLservice) {

		String getCubeMeasure_query = "PREFIX qb: <http://purl.org/linked-data/cube#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"
				+ "select  distinct ?dim where {";

		// If a SPARQL service is defined
		if (SPARQLservice != null) {
			getCubeMeasure_query += "SERVICE " + SPARQLservice + " {";
		}

		// If a cube graph is defined
		if (cubeGraph != null) {
			getCubeMeasure_query += "GRAPH <" + cubeGraph + "> {";
		}

		getCubeMeasure_query += dataCubeURI + "qb:structure ?dsd.";

		// If a cube graph is defined
		if (cubeGraph != null) {
			getCubeMeasure_query += "}";
		}

		// If a cube DSD graph is defined
		if (cubeDSDGraph != null) {
			getCubeMeasure_query += "GRAPH <" + cubeDSDGraph + "> {";
		}

		getCubeMeasure_query += "?dsd qb:component  ?cs."
				+ "?cs qb:measure  ?dim.}";

		// If a cube DSD graph is defined
		if (cubeDSDGraph != null) {
			getCubeMeasure_query += "}";
		}

		// If a SPARQL service is defined
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

	// Get all the measure of a slice
	// Input: The sliceURI, slice Graph, SPARQL service
	// The slice Graph and SPARQL service can be null if not available
	public static List<String> getSliceMeasure(String sliceURI,
			String sliceGraph, String cubeDSDGraph, String SPARQLservice) {

		String getSliceMeasure_query = "PREFIX qb: <http://purl.org/linked-data/cube#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"
				+ "select  distinct ?measure where {";

		// If a SPARQL service is defined
		if (SPARQLservice != null) {
			getSliceMeasure_query += "SERVICE " + SPARQLservice + " {";
		}

		// If a cube graph is defined
		if (sliceGraph != null) {
			getSliceMeasure_query += "GRAPH <" + sliceGraph + "> {";
		}

		getSliceMeasure_query += sliceURI + "qb:sliceStructure ?sliceKey."
				+ "?dsd qb:sliceKey ?sliceKey.";

		// If a cube DSD graph is defined
		if (sliceGraph != null) {
			getSliceMeasure_query += "}";
		}

		// If a cube DSD graph is defined
		if (cubeDSDGraph != null) {
			getSliceMeasure_query += "GRAPH <" + cubeDSDGraph + "> {";
		}

		getSliceMeasure_query += "?dsd qb:component  ?cs."
				+ "?cs qb:measure  ?measure.}";

		// If a cube DSD graph is defined
		if (cubeDSDGraph != null) {
			getSliceMeasure_query += "}";
		}

		// If a SPARQL service is defined
		if (SPARQLservice != null) {
			getSliceMeasure_query += "}";
		}

		TupleQueryResult res = executeSelect(getSliceMeasure_query);

		List<String> cubeMeasure = new ArrayList<String>();

		try {
			while (res.hasNext()) {
				BindingSet bindingSet = res.next();
				cubeMeasure.add(bindingSet.getValue("measure").stringValue());
			}
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		}
		return cubeMeasure;
	}

	public static List<LDResource> getDimensionValues(String dimensionURI,
			String cubeURI, String cubeGraph, String cubeDSDGraph,
			String SPARQLservice) {

		String getDimensionValues_query = "PREFIX qb: <http://purl.org/linked-data/cube#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"
				+ "select  distinct ?value ?label ?skoslabel where {";

		// If a SPARQL service is defined
		if (SPARQLservice != null) {
			getDimensionValues_query += "SERVICE " + SPARQLservice + " {";
		}

		// If a cube graph is defined
		if (cubeGraph != null) {
			getDimensionValues_query += "GRAPH <" + cubeGraph + "> {";
		}

		getDimensionValues_query += "?observation qb:dataSet " + cubeURI + "."
				+ "?observation <" + dimensionURI + "> ?value.";

		// If a cube graph is defined
		if (cubeGraph != null) {
			getDimensionValues_query += "}";
		}

		getDimensionValues_query += "OPTIONAL{";
		// If a cube DSD graph is defined
		if (cubeDSDGraph != null) {
			getDimensionValues_query += "GRAPH <" + cubeDSDGraph + "> {";
		}
		getDimensionValues_query += "{?value rdfs:label ?label."
				+ "FILTER (lang(?label) = \"\" || lang(?label) = \"en\")}"
				+ "UNION {?value skos:prefLabel ?skoslabel."
				+ "FILTER (lang(?skoslabel) = \"\" || lang(?skoslabel) = \"en\")}}}";

		// If a cube DSD graph is defined
		if (cubeDSDGraph != null) {
			getDimensionValues_query += "}";
		}

		// If a SPARQL service is defined
		if (SPARQLservice != null) {
			getDimensionValues_query += "}";
		}

		TupleQueryResult res = executeSelect(getDimensionValues_query);
		List<LDResource> dimensionValues = new ArrayList<LDResource>();

		try {
			while (res.hasNext()) {
				BindingSet b = res.next();
				LDResource resource = new LDResource(b.getValue("value")
						.stringValue());

				if (b.getValue("label") != null) {
					resource.setLabel(b.getValue("label").stringValue());
				} else if (b.getValue("skoslabel") != null) {
					resource.setLabel(b.getValue("skoslabel").stringValue());
				}

				dimensionValues.add(resource);
			}
		} catch (QueryEvaluationException e1) {
			e1.printStackTrace();
			System.out.println(dimensionURI);

		}

		Collections.sort(dimensionValues);
		return dimensionValues;
	}
	
	
	public static List<LDResource> getDimensionValuesFromSlice(String dimensionURI,
			String sliceURI, String cubeGraph, String cubeDSDGraph,String sliceGraph,
			String SPARQLservice) {

		String getDimensionValues_query = "PREFIX qb: <http://purl.org/linked-data/cube#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"
				+ "select  distinct ?value ?label ?skoslabel where {";

		// If a SPARQL service is defined
		if (SPARQLservice != null) {
			getDimensionValues_query += "SERVICE " + SPARQLservice + " {";
		}


		// If a slice graph is defined
		if (sliceGraph != null) {
			getDimensionValues_query += "GRAPH <" + sliceGraph + "> {";
		}

		getDimensionValues_query += sliceURI+ " qb:observation ?observation .";
		
		// If a slice graph is defined
		if (sliceGraph != null) {
			getDimensionValues_query += "}";
		}
		
		// If a cube graph is defined
		if (cubeGraph != null) {
			getDimensionValues_query += "GRAPH <" + cubeGraph + "> {";
		}

	
		getDimensionValues_query+= "?observation <" + dimensionURI + "> ?value.";

		// If a cube graph is defined
		if (cubeGraph != null) {
			getDimensionValues_query += "}";
		}

		getDimensionValues_query += "OPTIONAL{";
		// If a cube DSD graph is defined
		if (cubeDSDGraph != null) {
			getDimensionValues_query += "GRAPH <" + cubeDSDGraph + "> {";
		}
		getDimensionValues_query += "{?value rdfs:label ?label."
				+ "FILTER (lang(?label) = \"\" || lang(?label) = \"en\")}"
				+ "UNION {?value skos:prefLabel ?skoslabel."
				+ "FILTER (lang(?skoslabel) = \"\" || lang(?skoslabel) = \"en\")}}}";

		// If a cube DSD graph is defined
		if (cubeDSDGraph != null) {
			getDimensionValues_query += "}";
		}

		// If a SPARQL service is defined
		if (SPARQLservice != null) {
			getDimensionValues_query += "}";
		}

		TupleQueryResult res = executeSelect(getDimensionValues_query);
		List<LDResource> dimensionValues = new ArrayList<LDResource>();

		try {
			while (res.hasNext()) {
				BindingSet b = res.next();
				LDResource resource = new LDResource(b.getValue("value")
						.stringValue());

				if (b.getValue("label") != null) {
					resource.setLabel(b.getValue("label").stringValue());
				} else if (b.getValue("skoslabel") != null) {
					resource.setLabel(b.getValue("skoslabel").stringValue());
				}

				dimensionValues.add(resource);
			}
		} catch (QueryEvaluationException e1) {
			e1.printStackTrace();
			System.out.println(dimensionURI);

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
			askDimensionValueInDataCube_query += "SERVICE " + SPARQLservice
					+ " {";
		}
		askDimensionValueInDataCube_query += " ?obs <http://purl.org/linked-data/cube#dataSet> "
				+ cubeURI
				+ "."
				+ "?obs <"
				+ dimensionURI
				+ "> <"
				+ value
				+ ">}";

		if (SPARQLservice != null) {
			askDimensionValueInDataCube_query += "}";
		}

		// SPARQLservice=SPARQLservice.replaceAll("<","");
		// SPARQLservice=SPARQLservice.replaceAll(">","");
		// return
		// executeASK_direct(askDimensionValueInDataCube_query,SPARQLservice);
		return executeASK(askDimensionValueInDataCube_query);
	}

	public static List<LDResource> getDimensionValuesFromCodeList(
			String dimensionURI, String cubeURI, String cubeDSDGraph,
			String SPARQLservice) {

		String getDimensionValues_query = "PREFIX qb: <http://purl.org/linked-data/cube#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"
				+ "select  ?value ?label ?skoslabel where {";

		if (SPARQLservice != null) {
			getDimensionValues_query += "SERVICE " + SPARQLservice + " {";
		}

		// If a cube DSD graph is defined
		if (cubeDSDGraph != null) {
			getDimensionValues_query += "GRAPH <" + cubeDSDGraph + "> {";
		}

		getDimensionValues_query += "<"
				+ dimensionURI
				+ "> qb:codeList ?cd."
				+ "?cd skos:hasTopConcept ?value."
				+ "OPTIONAL {?value rdfs:label ?label."
				+ "FILTER (lang(?label) = \"\" || lang(?label) = \"en\")}"
				+ "OPTIONAL {?value skos:prefLabel ?skoslabel."
				+ "FILTER (lang(?skoslabel) = \"\" || lang(?skoslabel) = \"en\")}}";

		// If a cube DSD graph is defined
		if (cubeDSDGraph != null) {
			getDimensionValues_query += "}";
		}

		if (SPARQLservice != null) {
			getDimensionValues_query += "}";
		}

		TupleQueryResult res = executeSelect(getDimensionValues_query);
		List<LDResource> dimensionValues = new ArrayList<LDResource>();

		// Create one thread for each dimension value
		// ExecutorService executor = Executors.newCachedThreadPool();
		// List<Future<List<LDResource>>> list = new
		// ArrayList<Future<List<LDResource>>>();
		try {
			while (res.hasNext()) {
				BindingSet b = res.next();
				LDResource resource = new LDResource(b.getValue("value")
						.stringValue());

				if (b.getValue("label") != null) {
					resource.setLabel(b.getValue("label").stringValue());
				} else if (b.getValue("skoslabel") != null) {
					resource.setLabel(b.getValue("skoslabel").stringValue());
				}

				// Callable<List<LDResource>> worker = new DimValueInCubeThread(
				// dimensionURI, resource, cubeURI,SPARQLservice);
				// Future<List<LDResource>> submit = executor.submit(worker);
				// list.add(submit);

				// Check if a value from the code list exists in the datacube

				// if(askDimensionValueInDataCube(dimensionURI,resource.getURI(),cubeURI,SPARQLservice)){
				dimensionValues.add(resource);
				// }
			}

			/*
			 * for (Future<List<LDResource>> future : list) { try {
			 * List<LDResource> dimValuesList = future.get();
			 * dimensionValues.addAll(dimValuesList); } catch
			 * (InterruptedException e) { e.printStackTrace(); } catch
			 * (ExecutionException e) { e.printStackTrace(); } }
			 * 
			 * executor.shutdown();
			 */

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

		getDimensionValues_query += cubeURI
				+ " qb:structure ?dsd."
				+ " ?dsd qb:component  ?cs."
				+ "?cs qb:dimension  ?dim."
				+ "?observation qb:dataSet "
				+ cubeURI
				+ "."
				+ "?observation ?dim ?value. "
				+ "OPTIONAL {?value rdfs:label ?label."
				+ "FILTER (lang(?label) = \"\" || lang(?label) = \"en\")}"
				+ "OPTIONAL {?value skos:prefLabel ?skoslabel."
				+ "FILTER (lang(?skoslabel) = \"\" || lang(?skoslabel) = \"en\")}}";

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
				} else if (b.getValue("skoslabel") != null) {
					value = new LDResource(b.getValue("value").stringValue(), b
							.getValue("skoslabel").stringValue());
				} else {
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

	public static String getCubeSliceGraph(String cubeURI, String SPARQLservice) {

		String geCubeGraph_query = "PREFIX qb: <http://purl.org/linked-data/cube#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "select distinct ?graph_uri where{";

		if (SPARQLservice != null) {
			geCubeGraph_query += "SERVICE " + SPARQLservice + " {";
		}

		geCubeGraph_query += " GRAPH ?graph_uri{" + cubeURI + " rdf:type ?x }}";

		if (SPARQLservice != null) {
			geCubeGraph_query += "}";
		}

		TupleQueryResult res = executeSelect(geCubeGraph_query);

		String graphURI = null;
		try {
			if (res.hasNext()) {
				graphURI = res.next().getValue("graph_uri").toString();
			}
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		}

		return graphURI;
	}

	// Get the cube URI using as starting point a Slice
	public static String getCubeGraphFromSlice(String sliceURI,
			String sliceGraph, String SPARQLservice) {

		String geCubeGraphFromSlice_query = "PREFIX qb: <http://purl.org/linked-data/cube#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "select distinct ?graph_uri where{";

		if (SPARQLservice != null) {
			geCubeGraphFromSlice_query += "SERVICE " + SPARQLservice + " {";
		}

		if (sliceGraph != null) {
			geCubeGraphFromSlice_query += "GRAPH <" + sliceGraph + "> {";
		}

		geCubeGraphFromSlice_query += "?cube qb:slice " + sliceURI + ". ";

		if (sliceGraph != null) {
			geCubeGraphFromSlice_query += "}";
		}

		geCubeGraphFromSlice_query += " GRAPH ?graph_uri{?cube rdf:type qb:DataSet }}";

		if (SPARQLservice != null) {
			geCubeGraphFromSlice_query += "}";
		}

		TupleQueryResult res = executeSelect(geCubeGraphFromSlice_query);

		String graphURI = null;
		try {
			if (res.hasNext()) {
				graphURI = res.next().getValue("graph_uri").toString();
			}
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		}

		return graphURI;
	}

	public static String getType(String cubeSliceURI, String cubeSliceGraph,
			String SPARQLservice) {

		String getCubeSliceType_query = "PREFIX qb: <http://purl.org/linked-data/cube#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "select ?type where{";

		if (SPARQLservice != null) {
			getCubeSliceType_query += "SERVICE " + SPARQLservice + " {";
		}

		// If a cube/slice graph is defined
		if (cubeSliceGraph != null) {
			getCubeSliceType_query += "GRAPH <" + cubeSliceGraph + "> {";
		}

		getCubeSliceType_query += cubeSliceURI + " rdf:type ?type}";

		// If a cube/slice graph is defined
		if (cubeSliceGraph != null) {
			getCubeSliceType_query += "}";
		}

		if (SPARQLservice != null) {
			getCubeSliceType_query += "}";
		}

		TupleQueryResult res = executeSelect(getCubeSliceType_query);

		String type = null;
		try {
			while (res.hasNext()) {
				type = res.next().getValue("type").toString();
			}
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		}

		return type;
	}

	public static String getCubeStructureGraph(String cubeURI,
			String cubeGraph, String SPARQLservice) {

		String geCubeGraph_query = "PREFIX qb: <http://purl.org/linked-data/cube#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "select distinct ?graph_uri where{";

		if (SPARQLservice != null) {
			geCubeGraph_query += "SERVICE " + SPARQLservice + " {";
		}

		if (cubeGraph != null) {
			geCubeGraph_query += "GRAPH <" + cubeGraph + "> {";
		}

		geCubeGraph_query += cubeURI + "qb:structure ?dsd. ";

		if (cubeGraph != null) {
			geCubeGraph_query += "}";
		}

		geCubeGraph_query += "GRAPH ?graph_uri{"
				+ "?dsd rdf:type qb:DataStructureDefinition }}";

		if (SPARQLservice != null) {
			geCubeGraph_query += "}";
		}

		TupleQueryResult res = executeSelect(geCubeGraph_query);

		String graphURI = null;
		try {
			while (res.hasNext()) {
				String tmpgraphURI = res.next().getValue("graph_uri")
						.toString();
				if (globalDSD && tmpgraphURI.contains("globaldsd")) {
					graphURI = tmpgraphURI;
				} else if (!globalDSD && !tmpgraphURI.contains("globaldsd")) {
					if (notime && tmpgraphURI.contains("notime")) {
						graphURI = tmpgraphURI;
					} else if (!notime && !tmpgraphURI.contains("notime")) {
						graphURI = tmpgraphURI;
					}
				}
			}
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		}

		return graphURI;
	}

	public static String getCubeStructureGraphFromSlice(String sliceURI,
			String sliceGraph, String SPARQLservice) {

		String getCubeStructureGraphFromSlice_query = "PREFIX qb: <http://purl.org/linked-data/cube#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "select distinct ?graph_uri where{";

		if (SPARQLservice != null) {
			getCubeStructureGraphFromSlice_query += "SERVICE " + SPARQLservice
					+ " {";
		}

		if (sliceGraph != null) {
			getCubeStructureGraphFromSlice_query += "GRAPH <" + sliceGraph
					+ "> {";
		}

		getCubeStructureGraphFromSlice_query += sliceURI
				+ " qb:sliceStructure ?slicekey."
				+ "?cubeDSD qb:sliceKey ?slicekey.";

		if (sliceGraph != null) {
			getCubeStructureGraphFromSlice_query += "}";
		}

		getCubeStructureGraphFromSlice_query += " GRAPH ?graph_uri{"
				+ "?cubeDSD rdf:type qb:DataStructureDefinition }}";

		if (SPARQLservice != null) {
			getCubeStructureGraphFromSlice_query += "}";
		}

		TupleQueryResult res = executeSelect(getCubeStructureGraphFromSlice_query);

		String graphURI = null;
		try {
			while (res.hasNext()) {
				String tmpgraphURI = res.next().getValue("graph_uri")
						.toString();
				if (globalDSD && tmpgraphURI.contains("globaldsd")) {
					graphURI = tmpgraphURI;
				} else if (!globalDSD && !tmpgraphURI.contains("globaldsd")) {
					if (notime && tmpgraphURI.contains("notime")) {
						graphURI = tmpgraphURI;
					} else if (!notime && !tmpgraphURI.contains("notime")) {
						graphURI = tmpgraphURI;
					}
				}
			}
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		}

		return graphURI;
	}

	public static String createCubeSlice(String cubeURI, String cubeGraphURI,
			HashMap<LDResource, LDResource> sliceFixedDimensions,
			String sliceMeasure, List<LDResource> sliceObservation) {

		// create random slice graph
		Random rand = new Random();
		long rnd = Math.abs(rand.nextLong());

		String sliceGraph = "<http://www.fluidops.com/resource/graph_" + rnd
				+ ">";

		// check if graph already exists
		while (existGraph(sliceGraph)) {
			rnd = Math.abs(rand.nextLong());
			sliceGraph = "<http://www.fluidops.com/resource/graph_" + rnd + ">";
		}

		// create random slice URI
		String sliceURI = "<http://www.fluidops.com/resource/slice_" + rnd
				+ ">";

		// create random slice key
		String sliceKeyURI = "<http://www.fluidops.com/resource/sliceKey" + rnd
				+ ">";

		// INSERT SLICE STRUCTURE QUERY
		String insert_slice_structure_query = "PREFIX qb: <http://purl.org/linked-data/cube#>"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "INSERT DATA  { graph "+ sliceGraph+ " {"
				+ sliceKeyURI	+ " rdf:type qb:SliceKey. ";

		// Add fixed dimensions to slice-key structure
		for (LDResource ldr : sliceFixedDimensions.keySet()) {
			insert_slice_structure_query += sliceKeyURI
					+ " qb:componentProperty <" + ldr.getURI() + ">. ";
		}

		// insert_slice_structure_query += sliceKeyURI + " qb:measure <"+
		// sliceMeasure + ">. ";

		insert_slice_structure_query += sliceURI + " rdf:type qb:Slice."
				+ sliceURI + " qb:sliceStructure " + sliceKeyURI + ". "
				+ cubeURI + " qb:slice " + sliceURI + ". " + "<"
				+ getCubeDSD(cubeURI, cubeGraphURI) + "> qb:sliceKey "
				+ sliceKeyURI + ". ";

		// Add fixed dimensions values to slice
		for (LDResource ldr : sliceFixedDimensions.keySet()) {
			String dimensionValue="";
			//is URI
			if(sliceFixedDimensions.get(ldr).getURI().contains("http")){
				dimensionValue="<" + sliceFixedDimensions.get(ldr).getURI()	+ ">";

			//	Is literal	
			}else{
				dimensionValue="\""+sliceFixedDimensions.get(ldr).getURI()+"\"";
			}
			insert_slice_structure_query += sliceURI + " <" + ldr.getURI()
					+ "> " +dimensionValue	+ ".";
		}

		insert_slice_structure_query += "}}";
		IWBquery.executeUPDATE(insert_slice_structure_query);

		System.out.println(insert_slice_structure_query);
		// INSERT SLICE DATA QUERY
		String insert_slice_data_query = "PREFIX qb: <http://purl.org/linked-data/cube#>"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "INSERT DATA  { graph " + sliceGraph + " {";

		int i = 0;
		for (LDResource ldr : sliceObservation) {
			i++;
			insert_slice_data_query += sliceURI + " qb:observation <"
					+ ldr.getURI() + ">.";

			// Execute an INSERT for every 1000 observations
			if (i == 1000) {
				i = 0;
				insert_slice_data_query += "}}";
				IWBquery.executeUPDATE(insert_slice_data_query);

				// Empty the Insert query and re-initialize it
				insert_slice_data_query = "PREFIX qb: <http://purl.org/linked-data/cube#>"
						+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
						+ "INSERT DATA  { graph " + sliceGraph + " {";
			}
		}

		insert_slice_data_query += "}}";

		// If there are still observations not inserted
		if (i > 0) {
			IWBquery.executeUPDATE(insert_slice_data_query);
		}

		sliceURI = sliceURI.replaceAll("<", "");
		sliceURI = sliceURI.replaceAll(">", "");

		return sliceURI;

	}

	public static boolean existGraph(String graphURI) {
		String askQuery = "ASK {graph " + graphURI + "{?x ?y ?z}}";
		return executeASK(askQuery);

	}

	public static String getCubeDSD(String cubeURI, String cubeGraphURI) {

		String get_cube_dsd = "PREFIX qb: <http://purl.org/linked-data/cube#>"
				+ "select ?dsd where{";

		if (cubeGraphURI != null) {
			get_cube_dsd += "GRAPH <" + cubeGraphURI + ">{";
		}

		get_cube_dsd += cubeURI + "qb:structure ?dsd.} ";

		if (cubeGraphURI != null) {
			get_cube_dsd += "}";
		}

		TupleQueryResult res = executeSelect(get_cube_dsd);

		String cubeDSD = "";

		try {
			while (res.hasNext()) {

				BindingSet bindingSet = res.next();
				cubeDSD = bindingSet.getValue("dsd").stringValue();
			}
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		}

		return cubeDSD;

	}

	public static TupleQueryResult get2DVisualsiationValues(
			List<LDResource> visualDims,
			HashMap<LDResource, LDResource> fixedDims, List<String> measures,
			HashMap<LDResource, List<LDResource>> alldimensionvalues,
			String cubeURI, String cubeGraph, String SPARQLservice) {

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

		if (cubeGraph != null) {
			sparql_query += "GRAPH <" + cubeGraph + "> {";
		}

		sparql_query += "?obs <http://purl.org/linked-data/cube#dataSet> "
				+ cubeURI + ".";

		i = 1;
		// Add free dimensions to where clause
		for (LDResource vDim : visualDims) {
			sparql_query += "?obs <" + vDim.getURI() + "> " + "?dim" + i + ". ";
			i++;
		}

		// Add fixed dimensions to where clause, select the first value of the
		// list of all values
		int j = 1;
		for (LDResource fDim : fixedDims.keySet()) {
			sparql_query += "?obs <" + fDim.getURI() + "> ";
			if (fixedDims.get(fDim).getURI().contains("http")) {
				sparql_query += "<" + fixedDims.get(fDim).getURI() + ">.";
			} else {
				sparql_query += "?value_" + j + ". FILTER(STR(?value_" + j
						+ ")='" + fixedDims.get(fDim).getURI() + "')";
			}
			j++;

		}

		sparql_query += "?obs  <" + measures.get(0) + "> ?measure.} ";

		if (cubeGraph != null) {
			sparql_query += "}";
		}

		if (SPARQLservice != null) {
			sparql_query += "}";
		}

		TupleQueryResult res = executeSelect(sparql_query);

		return res;

	}

	public static TupleQueryResult get2DVisualsiationValuesFromSlice(
			List<LDResource> visualDims,
			HashMap<LDResource, LDResource> fixedDims, List<String> measures,
			HashMap<LDResource, List<LDResource>> alldimensionvalues,
			String sliceURI, String sliceGraph, String cubeGraph,
			String SPARQLservice) {

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

		if (sliceGraph != null) {
			sparql_query += "GRAPH <" + sliceGraph + "> {";
		}

		sparql_query += sliceURI
				+ " <http://purl.org/linked-data/cube#observation> ?obs.";

		if (sliceGraph != null) {
			sparql_query += "}";
		}

		if (cubeGraph != null) {
			sparql_query += "GRAPH <" + cubeGraph + "> {";
		}

		i = 1;
		// Add free dimensions to where clause
		for (LDResource vDim : visualDims) {
			sparql_query += "?obs <" + vDim.getURI() + "> " + "?dim" + i + ". ";
			i++;
		}

		// Add fixed dimensions to where clause, select the first value of the
		// list of all values
		int j = 1;
		for (LDResource fDim : fixedDims.keySet()) {
			sparql_query += "?obs <" + fDim.getURI() + "> ";
			if (fixedDims.get(fDim).getURI().contains("http")) {
				sparql_query += "<" + fixedDims.get(fDim).getURI() + ">.";
			} else {
				sparql_query += "?value_" + j + ". FILTER(STR(?value_" + j
						+ ")='" + fixedDims.get(fDim).getURI() + "')";
			}
			j++;

		}

		sparql_query += "?obs  <" + measures.get(0) + "> ?measure.} ";

		if (cubeGraph != null) {
			sparql_query += "}";
		}

		if (SPARQLservice != null) {
			sparql_query += "}";
		}

		TupleQueryResult res = executeSelect(sparql_query);

		return res;

	}

	// //////////////////////////////////////Map
	// View/////////////////////////////////////////////////////

	public static LDResource getGeoDimension(String dataCubeURI,
			String cubeGraph, String cubeDSDGraph, String SPARQLservice) {
		LDResource ldr = null;
		String getGeoDimension_query = "PREFIX qb: <http://purl.org/linked-data/cube#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX sdmxDim: <http://purl.org/linked-data/sdmx/2009/dimension#>"
				+ "PREFIX skos:    <http://www.w3.org/2004/02/skos/core#>  " +

				"SELECT DISTINCT ?uri ?label WHERE {";
		// If a SPARQL service is defined
		if (SPARQLservice != null) {
			getGeoDimension_query += "SERVICE " + SPARQLservice + " {";
		}
		// If a cube graph is defined
		if (cubeGraph != null) {
			getGeoDimension_query += "GRAPH <" + cubeGraph + "> {";
		}
		getGeoDimension_query += dataCubeURI + " qb:structure ?dsd.";
		// If a cube graph is defined
		if (cubeGraph != null) {
			getGeoDimension_query += "}";
		}
		// If a cube DSD graph is defined
		if (cubeDSDGraph != null) {
			getGeoDimension_query += "GRAPH <" + cubeDSDGraph + "> {";
		}
		getGeoDimension_query += "?dsd qb:component  ?comp."
				+ "?comp qb:dimension ?uri."
				+ "?uri a ?dimensionType;"
				+ "     a qb:DimensionProperty. "
				+ "OPTIONAL {"
				+ "?uri qb:concept ?concept."
				+ "?concept skos:prefLabel ?label.FILTER(LANGMATCHES(LANG(?label), \"en\"))"
				+ "}" + "{" + "{ ?uri rdfs:subPropertyOf+ sdmxDim:refArea }"
				+ "UNION" + "{ ?uri a sdmxDim:refArea }" + "}}";

		// If cube DSD graph is defined
		if (cubeDSDGraph != null) {
			getGeoDimension_query += "}";
		}
		// If a SPARQL service is defined
		if (SPARQLservice != null) {
			getGeoDimension_query += "}";
		}

		TupleQueryResult res = executeSelect(getGeoDimension_query);
		try {
			while (res.hasNext()) {
				BindingSet bindingSet = res.next();
				ldr = new LDResource(bindingSet.getValue("uri").stringValue());
				if (bindingSet.getValue("label") != null) {
					ldr.setLabel(bindingSet.getValue("label").stringValue());
				}
			}
			return ldr;
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static List<LDResource> getDataCubeAttributes(String dataCubeURI,
			String cubeGraph, String cubeDSDGraph, String SPARQLservice) { // Areti

		String getCubeAttributes_query = "PREFIX property: <http://eurostat.linked-statistics.org/property#> "
				+ "PREFIX  qb: <http://purl.org/linked-data/cube#>  "
				+ "PREFIX skos:    <http://www.w3.org/2004/02/skos/core#>  "
				+ "PREFIX concept:  <http://eurostat.linked-statistics.org/concept#> "
				+ "PREFIX dsd:  <http://stats.data-gov.ie/dsd/> "
				+ "select  distinct ?attribute ?label where {";
		// If a SPARQL service is defined
		if (SPARQLservice != null) {
			getCubeAttributes_query += "SERVICE " + SPARQLservice + " {";
		}
		// If a cube graph is defined
		if (cubeGraph != null) {
			getCubeAttributes_query += "GRAPH <" + cubeGraph + "> {";
		}
		getCubeAttributes_query += dataCubeURI + " qb:structure ?dsd.";
		// If a cube graph is defined
		if (cubeGraph != null) {
			getCubeAttributes_query += "}";
		}
		// If a cube DSD graph is defined
		if (cubeDSDGraph != null) {
			getCubeAttributes_query += "GRAPH <" + cubeDSDGraph + "> {";
		}
		getCubeAttributes_query += "?dsd qb:component ?comp. "
				+ "?comp qb:attribute ?attribute. "
				+ "OPTIONAL {?attribute qb:concept ?concept. "
				+ "?concept skos:prefLabel ?label."
				+ "FILTER(LANGMATCHES(LANG(?label), \"en\")) }}";

		// If cube DSD graph is defined
		if (cubeDSDGraph != null) {
			getCubeAttributes_query += "}";
		}

		// If a SPARQL service is defined
		if (SPARQLservice != null) {
			getCubeAttributes_query += "}";
		}

		TupleQueryResult res = executeSelect(getCubeAttributes_query);
		List<LDResource> cubeAttributes = new ArrayList<LDResource>();

		try {
			while (res.hasNext()) {
				BindingSet bindingSet = res.next();
				LDResource ldr = new LDResource(bindingSet
						.getValue("attribute").stringValue());

				if (bindingSet.getValue("label") != null) {
					ldr.setLabel(bindingSet.getValue("label").stringValue());
				}
				cubeAttributes.add(ldr);
			}
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		}
		return cubeAttributes;
	}

	public static List<LDResource> getAttributeValues(String attributeURI,
			String dataCubeURI, String cubeGraph, String cubeDSDGraph,
			String SPARQLservice) {

		String getAttributeValues_query = "PREFIX qb: <http://purl.org/linked-data/cube#>" // Areti
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"
				+ "select  distinct ?value ?label ?skoslabel where {";
		// If a SPARQL service is defined
		if (SPARQLservice != null) {
			getAttributeValues_query += "SERVICE " + SPARQLservice + " {";
		}
		// If a cube graph is defined
		if (cubeGraph != null) {
			getAttributeValues_query += "GRAPH <" + cubeGraph + "> {";
		}
		getAttributeValues_query += dataCubeURI + " qb:structure ?dsd.";
		// If a cube graph is defined
		if (cubeGraph != null) {
			getAttributeValues_query += "}";
		}
		// If a cube DSD graph is defined
		if (cubeDSDGraph != null) {
			getAttributeValues_query += "GRAPH <" + cubeDSDGraph + "> {";
		}
		getAttributeValues_query += "?observation qb:dataSet "
				+ dataCubeURI
				+ "."
				+ "?observation <"
				+ attributeURI
				+ "> ?value."
				+ "OPTIONAL {?value rdfs:label ?label. FILTER (lang(?label) = \"\" || lang(?label) = \"en\")}"
				+ "OPTIONAL {?value skos:prefLabel ?skoslabel. FILTER (lang(?skoslabel) = \"\" || lang(?skoslabel) = \"en\")}}}";

		// If cube DSD graph is defined
		if (cubeDSDGraph != null) {
			getAttributeValues_query += "}";
		}

		// If a SPARQL service is defined
		if (SPARQLservice != null) {
			getAttributeValues_query += "}";
		}

		TupleQueryResult res = executeSelect(getAttributeValues_query);
		List<LDResource> attributeValues = new ArrayList<LDResource>();

		try {
			while (res.hasNext()) {
				BindingSet bindingSet = res.next();
				LDResource resource = new LDResource();

				Value skoslabel = bindingSet.getValue("skoslabel");
				Value label = bindingSet.getValue("label");
				if (skoslabel != null) {
					resource.setLabel(skoslabel.stringValue());
				} else if (label != null) {
					resource.setLabel(label.stringValue());
				}
				resource.setURI(bindingSet.getValue("value").stringValue());
				attributeValues.add(resource);
			}
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		}
		Collections.sort(attributeValues);
		return attributeValues;
	}

	public static TupleQueryResult getDVisualsiationValues(
			List<LDResource> visualDims,
			HashMap<LDResource, LDResource> fixedDims, List<String> measures,
			List<LDResource> cubeAttributes, String dataCubeURI,
			String cubeGraph, String cubeDSDGraph, String SPARQLservice) {

		String sparql_query = "PREFIX sdmx-measure: <http://purl.org/linked-data/sdmx/2009/measure#> ";
		sparql_query += "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>";
		sparql_query += "PREFIX  qb: <http://purl.org/linked-data/cube#> Select distinct ";

		int i = 1;
		// Add variables ?dim to SPARQL query
		// for (LDResource vDim : visualDims) {
		for (LDResource fDim : fixedDims.keySet()) {
			sparql_query += "?dim" + i + " ";

			i++;
		}

		i = 1;
		// add variables ?attr to SPARQL query
		for (LDResource fAttr : cubeAttributes) { // Areti
			sparql_query += "?attr" + i + " ";
			i++;
		}
		// Select observations of a specific cube (cubeURI)
		sparql_query += "?geolabel ?measure where{ ";
		// If a SPARQL service is defined
		if (SPARQLservice != null) {
			sparql_query += "SERVICE " + SPARQLservice + " {";
		}
		// If a cube graph is defined
		if (cubeGraph != null) {
			sparql_query += "GRAPH <" + cubeGraph + "> {";
		}
		sparql_query += dataCubeURI + " qb:structure ?dsd.";
		// If a cube graph is defined
		if (cubeGraph != null) {
			sparql_query += "}";
		}
		// If a cube DSD graph is defined
		if (cubeDSDGraph != null) {
			sparql_query += "GRAPH <" + cubeDSDGraph + "> {";
		}

		sparql_query += "?obs <http://purl.org/linked-data/cube#dataSet> "
				+ dataCubeURI + ".";

		i = 1;
		// Add free dimensions to where clause
		for (LDResource vDim : visualDims) {
			sparql_query += "?obs <" + vDim.getURI() + "> " + "?dim" + i + ". ";
			i++;
		}

		// Add fixed dimensions to where clause, select the first value of the
		// list of all values
		for (LDResource fDim : fixedDims.keySet()) {
			sparql_query += "?obs <" + fDim.getURI() + ">";
			if (fixedDims.get(fDim).getURI().toString().contains("http"))
				sparql_query += "<" + fixedDims.get(fDim).getURI() + ">.";
			else
				sparql_query += "?obs2" + ". FILTER (STR(?obs2) = '"
						+ fixedDims.get(fDim).getURI() + "')";
		}

		// Add attributes to where clause
		i = 1;
		for (LDResource fAttr : cubeAttributes) {
			sparql_query += "OPTIONAL {?obs <" + fAttr.getURI() + "> "
					+ "?attribute" + i + ". ?attribute" + i
					+ " skos:prefLabel ?attr" + i
					+ ". FILTER(LANGMATCHES(LANG(?attr" + i + "), \"en\"))}";
			i++;
		}
		sparql_query += "?obs <"
				+ getGeoDimension(dataCubeURI, cubeGraph, cubeDSDGraph,
						SPARQLservice).getURI() + "> ?geovalue.";
		sparql_query += "?geovalue skos:prefLabel ?geolabel. ";
		sparql_query += "?obs sdmx-measure:obsValue ?measure. ";

		sparql_query += "FILTER(LANGMATCHES(LANG(?geolabel), \"en\"))";
		// if(!serviceURI.equals(null))
		sparql_query += "}";
		// If cube DSD graph is defined
		if (cubeDSDGraph != null) {
			sparql_query += "}";
		}

		// If a SPARQL service is defined
		if (SPARQLservice != null) {
			sparql_query += "}";
		}
		// /////////////////////////////

		TupleQueryResult res = executeSelect(sparql_query);
		System.out.println("query is " + sparql_query);
		return res;

	}

}
