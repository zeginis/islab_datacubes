package org.certh.opencube.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CubeBrowsingUtils {

	/*
	 * Input: a List of dimensions Output: A HashMap with the URI - List of
	 * values for these dimensions
	 */
	public static HashMap<LDResource, List<LDResource>> getDimsValues(
			List<LDResource> dimensions, String cubeURI,boolean useCodeLists,String SPARQLservice) {

		//Create an executor to hold all threads
		ExecutorService executor = Executors.newFixedThreadPool(dimensions.size());
		List<Future<HashMap<LDResource, List<LDResource>>>> list = new ArrayList<Future<HashMap<LDResource, List<LDResource>>>>();
		
		//Create one thread for each dimension
		for (final LDResource vRes : dimensions) {
			Callable<HashMap<LDResource, List<LDResource>>> worker = new DimensionValuesThread(
					vRes, cubeURI,useCodeLists,SPARQLservice);
			Future<HashMap<LDResource, List<LDResource>>> submit = executor
					.submit(worker);
			list.add(submit);

		}

		// Retrieve the results from all threads
		HashMap<LDResource, List<LDResource>> dimensionsAndValues = new HashMap<LDResource, List<LDResource>>();

		for (Future<HashMap<LDResource, List<LDResource>>> future : list) {
			try {
				HashMap<LDResource, List<LDResource>> dimVaulesHashMap = future
						.get();
				dimensionsAndValues.putAll(dimVaulesHashMap);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}

		executor.shutdown();

		return dimensionsAndValues;

	}

	/*
	 * Input: a List with all the dimensions URIs Output: Select the first 2
	 * dimensions for visualization and return them as List
	 */
	public static List<LDResource> getRandomDims4Visualisation(
			List<LDResource> allDimensions,
			HashMap<LDResource, List<LDResource>> allDimensionsValues) {

		// If there exist at least 2 dimensions
		List<LDResource> visualDimensions = null;
		if (allDimensions.size() > 1) {
			visualDimensions=new ArrayList<LDResource>(2);
			
			//find dimension with most values
			LDResource dim1 = allDimensions.get(0);
			for(LDResource ldr:allDimensions){
				int dim1NumberOfValues = allDimensionsValues.get(dim1).size();
				int lrdNumberOfValues = allDimensionsValues.get(ldr).size();
				if(lrdNumberOfValues>dim1NumberOfValues){
					dim1=ldr;
				}
			}
			
			allDimensions.remove(dim1);
			//find 2nd dimension with most values
			LDResource dim2 = allDimensions.get(0);
			for(LDResource ldr:allDimensions){
				int dim2NumberOfValues = allDimensionsValues.get(dim2).size();
				int lrdNumberOfValues = allDimensionsValues.get(ldr).size();
				if(lrdNumberOfValues>dim2NumberOfValues){
					dim2=ldr;
				}
			}
			
			visualDimensions.add(dim2);
			visualDimensions.add(dim1);
			/*
			visualDimensions = new ArrayList<LDResource>(allDimensions.subList(
					0, 2));
			int dim1NumberOfValues = allDimensionsValues.get(
					visualDimensions.get(0)).size();
			int dim2NumberOfValues = allDimensionsValues.get(
					visualDimensions.get(1)).size();

			// Change the order of visual dimensions. The 1st dimension (column
			// headings) must have the less number of values
			if (dim1NumberOfValues > dim2NumberOfValues) {
				LDResource dim1 = visualDimensions.get(0);
				visualDimensions.set(0, visualDimensions.get(1));
				visualDimensions.set(1, dim1);
			}*/
		} else { // If there is only one dimension
			visualDimensions = allDimensions;
		}

		return visualDimensions;

	}

	/*
	 * Input: a List with all the dimensions URIs, Visual dimensions Output:
	 * Select the rest dimensions (other that the visualized) and return them as
	 * List
	 */
	public static List<LDResource> getFixedDimensions(
			List<LDResource> allDimensions, List<LDResource> visualDimensions) {

		List<LDResource> fixedDimensions = new ArrayList<LDResource>(
				allDimensions);

		fixedDimensions.removeAll(visualDimensions);
		return fixedDimensions;

	}

	public static HashMap<LDResource, LDResource> getFixedDimensionsRandomSelectedValues(
			HashMap<LDResource, List<LDResource>> allDimensionValues,
			List<LDResource> fixedDimensions) {

		HashMap<LDResource, LDResource> fixedDimSelectedValues = new HashMap<LDResource, LDResource>();
		for (LDResource fixedDim : fixedDimensions) {
			fixedDimSelectedValues.put(fixedDim,
					allDimensionValues.get(fixedDim).get(0));
		}

		return fixedDimSelectedValues;

	}

}
