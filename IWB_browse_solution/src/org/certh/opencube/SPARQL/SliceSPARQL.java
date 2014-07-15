package org.certh.opencube.SPARQL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.certh.opencube.utils.LDResource;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

public class SliceSPARQL {
	
	private static boolean globalDSD = false;
	private static boolean notime = false;

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

			TupleQueryResult res = QueryExecutor.executeSelect(getSliceDimensions_query);
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

				TupleQueryResult res = QueryExecutor.executeSelect(getSliceFixedDimensionValues_query);
				

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
			

			TupleQueryResult res = QueryExecutor.executeSelect(getSliceMeasure_query);
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

			TupleQueryResult res = QueryExecutor.executeSelect(getSliceMeasure_query);

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

			TupleQueryResult res = QueryExecutor.executeSelect(geCubeGraphFromSlice_query);

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

			TupleQueryResult res = QueryExecutor.executeSelect(getCubeStructureGraphFromSlice_query);

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

		
		//TO DO ADD SPARQL SERVICE
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
					+ CubeSPARQL.getCubeDSD(cubeURI, cubeGraphURI) + "> qb:sliceKey "
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
			QueryExecutor.executeUPDATE(insert_slice_structure_query);

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
					QueryExecutor.executeUPDATE(insert_slice_data_query);

					// Empty the Insert query and re-initialize it
					insert_slice_data_query = "PREFIX qb: <http://purl.org/linked-data/cube#>"
							+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
							+ "INSERT DATA  { graph " + sliceGraph + " {";
				}
			}

			insert_slice_data_query += "}}";

			// If there are still observations not inserted
			if (i > 0) {
				QueryExecutor.executeUPDATE(insert_slice_data_query);
			}

			sliceURI = sliceURI.replaceAll("<", "");
			sliceURI = sliceURI.replaceAll(">", "");

			return sliceURI;

		}
		
		public static boolean existGraph(String graphURI) {
			String askQuery = "ASK {graph " + graphURI + "{?x ?y ?z}}";
			return QueryExecutor.executeASK(askQuery);

		}

}
