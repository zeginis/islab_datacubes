package org.certh.opencube.aggregation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.certh.opencube.SPARQL.CubeSPARQL;
import org.certh.opencube.utils.LDResource;

public class createAggregations {

	
	public static void main(String[] args){
		
		String cubeURI="<http://eurostat.linked-statistics.org/data/aei_pr_gnb>";
		String cubeGraph = CubeSPARQL.getCubeSliceGraph(cubeURI, null);
		// Get Cube Structure graph
		String cubeDSDGraph = CubeSPARQL.getCubeStructureGraph(cubeURI,cubeGraph, null);

		// Get all Cube dimensions
		List<LDResource> cubeDimensions=CubeSPARQL.getDataCubeDimensions(cubeURI,	cubeGraph,
				cubeDSDGraph, null);

		// Get the Cube measure
		List<String> cubeMeasure =CubeSPARQL.getDataCubeMeasure(cubeURI,cubeGraph, cubeDSDGraph, null);
		
		OrderedPowerSet<LDResource> ops=new OrderedPowerSet<LDResource>((ArrayList<LDResource>) cubeDimensions);
		List<LinkedHashSet<LDResource>> perms=ops.getPermutationsList(cubeDimensions.size());
		for(Set<LDResource> myset:perms){
			String st="";
			for(LDResource l:myset){
				st+=l.getURI()+" ";
			}
			System.out.println(st);
		}
	}
}
