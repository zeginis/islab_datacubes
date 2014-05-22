package org.certh.opencube.utils;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryException;

import com.fluidops.iwb.api.EndpointImpl;
import com.fluidops.iwb.api.ReadDataManager;
import com.fluidops.iwb.api.query.QueryBuilder;

public class IWBquery {
	
	
	public static TupleQueryResult executeSelect(String query){
	ReadDataManager dm = EndpointImpl.api().getDataManager();
	QueryBuilder<TupleQuery> queryBuilder = QueryBuilder.createTupleQuery(query);

		TupleQueryResult res = null;
	
		try {
			TupleQuery tulpequery = queryBuilder.build(dm);
			 res = tulpequery.evaluate();
		} catch (MalformedQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (QueryEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 
		return res;
	}

}
