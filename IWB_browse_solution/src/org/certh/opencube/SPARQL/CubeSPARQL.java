package org.certh.opencube.SPARQL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.certh.opencube.utils.LDResource;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.model.Value;

public class CubeSPARQL {
	
	private static boolean globalDSD = false;
	private static boolean notime = false;
	
	// Get all the dimensions of a data cube
		// Input: The cubeURI, cubeGraph, SPARQL service
		// The cube Graph, cube DSD graph and SPARQL service can be null if not available
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

			TupleQueryResult res = QueryExecutor.executeSelect(getCubeDimensions_query);
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

			TupleQueryResult res = QueryExecutor.executeSelect(getCubeMeasure_query);

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

			TupleQueryResult res = QueryExecutor.executeSelect(getDimensionValues_query);
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
			return QueryExecutor.executeASK(askDimensionValueInDataCube_query);
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

			TupleQueryResult res = QueryExecutor.executeSelect(getDimensionValues_query);
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

			TupleQueryResult res = QueryExecutor.executeSelect(getDimensionValues_query);
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

			geCubeGraph_query += " GRAPH ?graph_uri{ {" + cubeURI + " rdf:type qb:DataSet }" +
					"UNION {"+cubeURI + " rdf:type qb:Slice}}}";

			if (SPARQLservice != null) {
				geCubeGraph_query += "}";
			}

			TupleQueryResult res = QueryExecutor.executeSelect(geCubeGraph_query);

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

			TupleQueryResult res = QueryExecutor.executeSelect(geCubeGraph_query);

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

			TupleQueryResult res = QueryExecutor.executeSelect(get_cube_dsd);

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

			TupleQueryResult res = QueryExecutor.executeSelect(getCubeSliceType_query);

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

			TupleQueryResult res = QueryExecutor.executeSelect(getCubeAttributes_query);
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
					+ "OPTIONAL {?value skos:prefLabel ?skoslabel. FILTER (lang(?skoslabel) = \"\" || lang(?skoslabel) = \"en\")}}";

			// If cube DSD graph is defined
			if (cubeDSDGraph != null) {
				getAttributeValues_query += "}";
			}

			// If a SPARQL service is defined
			if (SPARQLservice != null) {
				getAttributeValues_query += "}";
			}

			TupleQueryResult res = QueryExecutor.executeSelect(getAttributeValues_query);
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

			TupleQueryResult res = QueryExecutor.executeSelect(getGeoDimension_query);
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
		
		
		/////TO DELETE
/*	
		public static List<LDResource> getAttributeValues(String attributeURI, String cubeURI, String serviceURI) {

			
			String getAttributeValues_query = "PREFIX qb: <http://purl.org/linked-data/cube#>" 							//Areti
					+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
					+ "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"
					+ "select  distinct ?value ?label ?skoslabel where {"
					+ "SERVICE " + serviceURI+ "{"
					+ "?observation qb:dataSet "+ cubeURI + "."
					+ "?observation <" + attributeURI + "> ?value."
					+ "OPTIONAL {?value rdfs:label ?label. FILTER (lang(?label) = \"\" || lang(?label) = \"en\")}"
					+ "OPTIONAL {?value skos:prefLabel ?skoslabel. FILTER (lang(?skoslabel) = \"\" || lang(?skoslabel) = \"en\")}}}";

			TupleQueryResult res = QueryExecutor.executeSelect(getAttributeValues_query);
			List<LDResource> attributeValues = new ArrayList<LDResource>();

			try {
				while (res.hasNext()) {
					BindingSet bindingSet = res.next();
					LDResource resource = new LDResource();

					Value skoslabel = bindingSet.getValue("skoslabel");
					Value label = bindingSet.getValue("label");
					if (skoslabel != null) {
						resource.setLabel(skoslabel.stringValue());
					}
					else if (label != null) {
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
*/
}
