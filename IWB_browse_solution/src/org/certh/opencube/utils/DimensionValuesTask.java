package org.certh.opencube.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

class Globals {
	static ForkJoinPool fjPool = new ForkJoinPool();
}

class DimensionValuesTask extends RecursiveTask<List<LDResource>> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static final int SEQUENTIAL_THRESHOLD = 1000;

	int low;
	int high;
	List<BindingSet> resList;

	DimensionValuesTask(List<BindingSet> arr, int lo, int hi) {
		resList = arr;
		low = lo;
		high = hi;
	}

	protected List<LDResource> compute() {
		if (high - low <= SEQUENTIAL_THRESHOLD) {

			List<LDResource> dimensionValues = new ArrayList<LDResource>();
			for (int i = low; i < high; ++i) {
				BindingSet b = resList.get(i);

				LDResource resource = new LDResource();

				Value label = b.getValue("label");

				if (label != null) {
					resource.setLabel(label.stringValue());
				}

				resource.setURI(b.getValue("value").stringValue());

				dimensionValues.add(resource);

			}

			return dimensionValues;

		} else {
			
			System.out.println("FORK");
			int mid = low + (high - low) / 2;
			DimensionValuesTask left = new DimensionValuesTask(resList, low,
					mid);
			DimensionValuesTask right = new DimensionValuesTask(resList, mid,
					high);
			left.fork();
			List<LDResource> rightAns = right.compute();
			List<LDResource> leftAns = left.join();
			rightAns.addAll(leftAns);
			return rightAns;
		}
	}

	static List<LDResource> getDimValues(List<BindingSet> array) {
	//	int nThreads = Runtime.getRuntime().availableProcessors();
	//	System.out.println(nThreads);
	//	DimensionValuesTask mfj = new DimensionValuesTask(array, 0,
	//			array.size());
	//	ForkJoinPool pool = new ForkJoinPool(nThreads);
	//	pool.invoke(mfj);
	//	return mfj.getRawResult();

		 return Globals.fjPool.invoke(new DimensionValuesTask(array, 0, array
		 .size()));
	}
}