package org.certh.opencube.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.openrdf.query.BindingSet;

/**
 *
 * @author user
 */
public class DimValueInCubeThread implements Callable<List<LDResource>> {

    private String dimensionURI;
    private BindingSet value;
    private String cubeURI;
    private String SPARQLservice;

    public DimValueInCubeThread(String dimensionURI, BindingSet value,String cubeURI, String SPARQLservice) {
        this.dimensionURI = dimensionURI;
        this.value=value;
        this.cubeURI=cubeURI;
        this.SPARQLservice=SPARQLservice;

    }

    @Override
    public List<LDResource> call() throws Exception {
		LDResource resource = new LDResource(value.getValue("value")
				.stringValue());
		
    	List<LDResource> dimValue=new ArrayList<LDResource>();
    	if(IWBquery.askDimensionValueInDataCube(dimensionURI,resource.getURI(),cubeURI,SPARQLservice)){
    		dimValue.add(resource);
		}   		        	
    	
        return dimValue;
    }
}

 
