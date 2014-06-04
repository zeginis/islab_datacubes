package org.certh.opencube.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CubeBrowsingUtils {

	/*
	 * Input: a List of dimensions Output: A HashMap with the URI - List of
	 * values for these dimensions
	 */
	public static HashMap<LDResource, List<LDResource>> getDimsValues(
			List<LDResource> dimensions) {
		HashMap<LDResource, List<LDResource>> dimensionsAndValues = new HashMap<LDResource, List<LDResource>>();

		for (LDResource vRes : dimensions) {

			List<LDResource> vDimValues = IWBquery.getDimensionValues(vRes
					.getURI());
			dimensionsAndValues.put(vRes, vDimValues);
		}

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
			}
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

	/*
	 * 
	 * public static HashMap<String, Integer> getListIndexMap(List<LDResource>
	 * dim){ HashMap<String, Integer> listIndex=new HashMap<String, Integer>();
	 * int i=0; for(LDResource ldr:dim){ listIndex.put(ldr.getURI(),i); i++; }
	 * 
	 * return listIndex; }
	 */

}
