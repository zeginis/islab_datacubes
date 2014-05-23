package org.certh.opencube.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

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

		return res;
	}

	public static List<String> getDataCubeDimensions(String dataCubeURI) {

		String getCubeDimensions_query = "select  distinct ?dim where {"
				+ dataCubeURI+ " <http://purl.org/linked-data/cube#structure> ?dsd."
				+ "?dsd <http://purl.org/linked-data/cube#component>  ?cs."
				+ "?cs <http://purl.org/linked-data/cube#dimension>  ?dim.}";

		TupleQueryResult res = executeSelect(getCubeDimensions_query);

		List<String> cubeDimensions = new ArrayList<String>();

		try {
			while (res.hasNext()) {
				BindingSet bindingSet = res.next();
				cubeDimensions.add(bindingSet.getValue("dim").stringValue());
			}
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		}
		return cubeDimensions;
	}
	
	public static List<String> getDataCubeMeasure(String dataCubeURI) {

		String getCubeDimensions_query = "select  distinct ?dim where {"
				+ dataCubeURI+ " <http://purl.org/linked-data/cube#structure> ?dsd."
				+ "?dsd <http://purl.org/linked-data/cube#component>  ?cs."
				+ "?cs <http://purl.org/linked-data/cube#measure>  ?dim.}";

		TupleQueryResult res = executeSelect(getCubeDimensions_query);

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
	
	public static List<String> getDimensionValues(String dimensionURI) {

		String getDimensionValues_query = "select  distinct ?value where {"
				+ "?observation <"+ dimensionURI+"> ?value.}";
		
		TupleQueryResult res = executeSelect(getDimensionValues_query);

		List<String> dimensionValues = new ArrayList<String>();

		try {
			while (res.hasNext()) {
				BindingSet bindingSet = res.next();
				dimensionValues.add(bindingSet.getValue("value").stringValue());
			}
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		}
		return dimensionValues;
	}
	
	public static TupleQueryResult get2DVisualsiationValues(Set<String> visualDims,
			HashMap<String,List<String>> fixedDims, List<String> measures, String cubeURI){
		
		String sparql_query="Select ";
		int i=1;
		//Add variables ?dim to SPARQL query
		for(String vDim:visualDims){
			sparql_query+="?dim"+i+" ";
			i++;
		}
		
		//Select observations of a specific cube (cubeURI)
		sparql_query+="?measure where{ ?obs <http://purl.org/linked-data/cube#dataSet> "+cubeURI+".";
		
        i=1;
        //Add free dimensions to where clause
		for(String vDim:visualDims){
					sparql_query+="?obs <"+vDim+"> "+"?dim"+i+". ";
					i++;
		}		
		 
		//Add fixed dimensions to where clause, select the first value of the list of all values
		for(String fDim:fixedDims.keySet()){
			sparql_query+="?obs <"+fDim+"> <"+fixedDims.get(fDim).get(0)+">.";
		}
		
	   	sparql_query+="?obs  <"+measures.get(0)+"> ?measure}";
	   	System.out.println(sparql_query);
	   	return executeSelect(sparql_query);
		
	}

}
