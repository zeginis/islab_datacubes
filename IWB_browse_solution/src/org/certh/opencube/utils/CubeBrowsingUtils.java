package org.certh.opencube.utils;

import java.util.HashMap;
import java.util.List;

public class CubeBrowsingUtils {
	
	/*Input: a List with all the dimensions URIs
	  Output: Select the first 2 dimensions for visualization and return a HashMap with the
	          URI - List of values for the visualized dimensions    
  	*/        
	public static HashMap<String, List<LDResource>> getDimsAndValues4Visualisation(
			List<String> allDimensions) {

		HashMap<String, List<LDResource>> dimensionsAndValues = new HashMap<String, List<LDResource>>();

		// If there exist at least 2 dimensions
		List<String> visualDimensions = null;
		if (allDimensions.size() > 1) {
			visualDimensions = allDimensions.subList(0, 2);
		} else { // If there is only one dimension
			visualDimensions = allDimensions;
		}

		for (String vDim : visualDimensions) {
			List<LDResource> vDimValues = IWBquery.getDimensionValues(vDim);
			dimensionsAndValues.put(vDim, vDimValues);
		}

		return dimensionsAndValues;
	}

	/*Input: a List with all the dimensions URIs
	  Output: Select the rest dimensions (other that the visualized) and return a HashMap with the
	          URI - List of values for the visualized dimensions    
	*/ 
	public static  HashMap<String, List<LDResource>> getFixedDimensionValues(
			List<String> allDimensions) {

		HashMap<String, List<LDResource>> fixedDimensionValues = new HashMap<String, List<LDResource>>();

		// If there are at least 3 dimensions (2 are visualised an 1 fixed)
		if (allDimensions.size() > 2) {
			List<String> fixedDimensions = allDimensions.subList(2,
					allDimensions.size());
			for (String fixedDim : fixedDimensions) {
				List<LDResource> dimValues = IWBquery.getDimensionValues(fixedDim);
				fixedDimensionValues.put(fixedDim, dimValues);
			}
		}

		return fixedDimensionValues;

	}
	
	public static String getURIlabel(String URI){
		
		if(URI.contains("#")){
			return URI.substring(URI.lastIndexOf("#")+1, URI.length());
			
		}else{
			return URI.substring(URI.lastIndexOf("/")+1, URI.length());
		}
		
	}

}
