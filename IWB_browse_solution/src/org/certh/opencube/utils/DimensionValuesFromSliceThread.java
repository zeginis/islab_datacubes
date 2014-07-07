package org.certh.opencube.utils;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * 
 * @author user
 */
public class DimensionValuesFromSliceThread implements
		Callable<HashMap<LDResource, List<LDResource>>> {

	private String cubeURI;
	private String cubeGraph;
	private String cubeDSDGraph;
	private String sliceGraph;
	private LDResource dimension;
	private boolean useCodeLists;
	private String SPARQLservice;

	public DimensionValuesFromSliceThread(LDResource dimension, String cubeURI,
			String cubeGraph, String cubeDSDGraph,String sliceGraph, boolean useCodeLists,
			String SPARQLservice) {
		this.cubeURI = cubeURI;
		this.cubeGraph = cubeGraph;
		this.cubeDSDGraph = cubeDSDGraph;
		this.sliceGraph=sliceGraph;
		this.dimension = dimension;
		this.useCodeLists = useCodeLists;
		this.SPARQLservice = SPARQLservice;

	}

	@Override
	public HashMap<LDResource, List<LDResource>> call() throws Exception {
		List<LDResource> vDimValues = null;
		if (useCodeLists) {
			vDimValues = IWBquery.getDimensionValuesFromCodeList(
					dimension.getURI(), cubeURI, cubeDSDGraph, SPARQLservice);
			if (vDimValues.isEmpty()) {
				vDimValues = IWBquery.getDimensionValuesFromSlice(dimension.getURI(),
						cubeURI, cubeGraph, cubeDSDGraph, sliceGraph,SPARQLservice);
			}
		} else {
			vDimValues = IWBquery.getDimensionValuesFromSlice(dimension.getURI(),
					cubeURI, cubeGraph, cubeDSDGraph,sliceGraph, SPARQLservice);
		}

		HashMap<LDResource, List<LDResource>> dimensionsAndValues = new HashMap<LDResource, List<LDResource>>();
		dimensionsAndValues.put(dimension, vDimValues);
		return dimensionsAndValues;
	}
}
