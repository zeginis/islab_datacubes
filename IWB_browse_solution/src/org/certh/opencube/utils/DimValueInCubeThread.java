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
    private LDResource value;
    private String cubeURI;
    private String SPARQLservice;

    public DimValueInCubeThread(String dimensionURI, LDResource value,String cubeURI, String SPARQLservice) {
        this.dimensionURI = dimensionURI;
        this.value=value;
        this.cubeURI=cubeURI;
        this.SPARQLservice=SPARQLservice;

    }

    @Override
    public List<LDResource> call() throws Exception {
		
		
    	List<LDResource> dimValue=new ArrayList<LDResource>();
    	if(IWBquery.askDimensionValueInDataCube(dimensionURI,value.getURI(),cubeURI,SPARQLservice)){
    		dimValue.add(value);
		}   		        	
    	
        return dimValue;
    }
}

 
