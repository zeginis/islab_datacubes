package org.certh.opencube.cubebrowser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.plaf.DimensionUIResource;

import com.fluidops.ajax.FClientUpdate;
import com.fluidops.ajax.components.FButton;
import com.fluidops.ajax.components.FComponent;
import com.fluidops.ajax.components.FContainer;
import com.fluidops.ajax.components.FHTML;
import com.fluidops.ajax.components.FLabel;
import com.fluidops.ajax.components.FTable;
import com.fluidops.ajax.components.FTable.FilterPos;
import com.fluidops.ajax.components.FTextArea;
import com.fluidops.ajax.components.FTextInput2;
import com.fluidops.ajax.models.FTableModel;

import com.fluidops.iwb.model.ParameterConfigDoc;
import com.fluidops.iwb.api.ReadDataManager;
import com.fluidops.iwb.api.EndpointImpl;
import com.fluidops.iwb.widget.AbstractWidget;
import com.fluidops.iwb.api.query.QueryBuilder;

import org.apache.axis.attachments.DimeTypeNameFormat;
import org.certh.opencube.utils.IWBquery;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryException;

/**
 * On some wiki page add
 * 
 * <code>
 * = Test my demo widget =
 * 
 * <br/>
 * {{#widget: org.certh.opencube.cubebrowser.DataCubeBrowser
 * | dataCubeURI = 'Enter your name'
 * }} 
 * 
 * </code>
 * 
 */
public class DataCubeBrowser extends AbstractWidget<DataCubeBrowser.Config> {

	public static class Config {
		@ParameterConfigDoc(desc = "The data cube URI to visualise", required = true)
		public String dataCubeURI;
	}

	@Override
	protected FComponent getComponent(String id) {

		Config config = get();

		// the layouting container for this widget
		// the container must use the provided id
		FContainer cnt = new FContainer(id);

		// now we can add other components to the container
		// the simplest is to add them line by line

		final FTable ftable = new FTable("ftable");

		// Get all Cube dimensions
		List<String> cubeDimensions = IWBquery
				.getDataCubeDimensions(config.dataCubeURI);

		// Get the Cube measure
		List<String> cubeMeasure = IWBquery
				.getDataCubeMeasure(config.dataCubeURI);

		// Get Cube dimensions to visualize
		HashMap<String, List<String>> dimensions4Visualisation = getDimsAndValues4Visualisation(cubeDimensions);

		// Get Cube dimensions with fixed values
		HashMap<String, List<String>> fixedDimensionValues = getFixedDimensionValues(cubeDimensions);

		// Get query tuples for visualization
		TupleQueryResult res = IWBquery.get2DVisualsiationValues(
				dimensions4Visualisation.keySet(), fixedDimensionValues,
				cubeMeasure, config.dataCubeURI);

		FTableModel tm = createTupleQueryTableModel(res);
		FTableModel tm2=create2DCubeTableModel(res,dimensions4Visualisation);
		ftable.setShowCSVExport(true);
		ftable.setNumberOfRows(20);
		ftable.setEnableFilter(true);
		ftable.setOverFlowContainer(true);
		ftable.setFilterPos(FilterPos.TOP);
		ftable.setSortable(true);
		ftable.setModel(tm2);

		cnt.add(ftable);
		return cnt;
	}

	private HashMap<String, List<String>> getDimsAndValues4Visualisation(
			List<String> allDimensions) {

		HashMap<String, List<String>> dimensionsAndValues = new HashMap<String, List<String>>();

		// If there exist at least 2 dimensions
		List<String> visualDimensions = null;
		if (allDimensions.size() > 1) {
			visualDimensions = allDimensions.subList(0, 2);
		} else { // If there is only one dimension
			visualDimensions = allDimensions;
		}

		for (String vDim : visualDimensions) {
			List<String> vDimValues = IWBquery.getDimensionValues(vDim);
			// Select the first value for each dimension
			dimensionsAndValues.put(vDim, vDimValues);
		}

		return dimensionsAndValues;
	}

	private HashMap<String, List<String>> getFixedDimensionValues(
			List<String> allDimensions) {

		HashMap<String, List<String>> fixedDimensionValues = new HashMap<String, List<String>>();

		// If there are at least 3 dimensions (2 are visualised an 1 fixed)
		if (allDimensions.size() > 2) {
			List<String> fixedDimensions = allDimensions.subList(2,
					allDimensions.size());
			for (String fixedDim : fixedDimensions) {
				List<String> dimValues = IWBquery.getDimensionValues(fixedDim);
				// Select the first value for each dimension
				fixedDimensionValues.put(fixedDim, dimValues);
			}
		}

		return fixedDimensionValues;

	}

	private FTableModel create2DCubeTableModel(TupleQueryResult res,
			HashMap<String, List<String>> dimensions4Visualisation) {

		List<String> vDimsList=new ArrayList<String>();
		for(String vDim:dimensions4Visualisation.keySet()){
			vDimsList.add(vDim);
		}
		
		List<String> dim1=dimensions4Visualisation.get(vDimsList.get(0));
		List<String> dim2=dimensions4Visualisation.get(vDimsList.get(1));
		
		String[][] v2DCube=new String[dim2.size()][dim1.size()];
		
		
		FTableModel tm = new FTableModel();

		List<String> bindingNames;
		try {
			bindingNames = res.getBindingNames();

			tm.addColumn(vDimsList.get(1));
			
    		for (String dim1Val : dim1) {
				tm.addColumn(dim1Val);
			}

			while (res.hasNext()) {
				BindingSet bindingSet = res.next();
				int size2 = bindingSet.size();
				String[] data = new String[3];
				
				String dim1Val=bindingSet.getValue(bindingNames.get(0)).stringValue();
				String dim2Val=bindingSet.getValue(bindingNames.get(1)).stringValue();
				String measure=bindingSet.getValue(bindingNames.get(2)).stringValue();
	    		v2DCube[dim2.indexOf(dim2Val)][dim1.indexOf(dim1Val)]=measure;
			}
			
			for(int i=0;i<dim2.size()-1;i++){
				String[] data = new String[dim1.size()+1];
				data[0]=dim2.get(i);
				for(int j=1;j<dim1.size();j++){
					data[j]=v2DCube[i][j-1];
				}
				tm.addRow(data);
			}
			System.out.println(v2DCube);
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		}
		return tm;
	}

	private FTableModel createTupleQueryTableModel(TupleQueryResult res) {

		FTableModel tm = new FTableModel();

		List<String> bindingNames;
		try {
			bindingNames = res.getBindingNames();

			for (String name : bindingNames) {
				tm.addColumn(name);
			}

			while (res.hasNext()) {
				BindingSet bindingSet = res.next();
				int size2 = bindingSet.size();
				String[] data = new String[3];
				for (int i = 0; i < size2; i++) {
					data[i] = bindingSet.getValue(bindingNames.get(i))
							.stringValue();
				}
				tm.addRow(data);
			}
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		}
		return tm;
	}

	@Override
	public String getTitle() {
		return "Data Cube Browser widget";
	}

	@Override
	public Class<?> getConfigClass() {
		return Config.class;
	}
}