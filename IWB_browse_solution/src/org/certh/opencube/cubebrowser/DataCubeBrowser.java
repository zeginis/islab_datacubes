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
import org.certh.opencube.utils.CubeBrowsingUtils;
import org.certh.opencube.utils.IWBquery;
import org.certh.opencube.utils.LDResource;
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
 * | dataCubeURI = 'Enter the cube URI'
 * }} 
 * 
 * </code>
 * 
 */
public class DataCubeBrowser extends AbstractWidget<DataCubeBrowser.Config> {

	public static class Config {
		@ParameterConfigDoc(desc = "The data cube URI to visualise", required = true)
		public String dataCubeURI;

		@ParameterConfigDoc(desc = "Show URIs", required = true)
		public boolean showURIs;
	}

	@Override
	protected FComponent getComponent(String id) {

		Config config = get();

		// the layouting container for this widget
		// the container must use the provided id
		FContainer cnt = new FContainer(id);

		// now we can add other components to the container
		// the simplest is to add them line by line

		// Get all Cube dimensions
		List<String> cubeDimensions = IWBquery
				.getDataCubeDimensions(config.dataCubeURI);

		// Get the Cube measure
		List<String> cubeMeasure = IWBquery
				.getDataCubeMeasure(config.dataCubeURI);

		// Get Cube dimensions to visualize
		HashMap<String, List<LDResource>> dimensions4Visualisation = CubeBrowsingUtils
				.getDimsAndValues4Visualisation(cubeDimensions);

		// Get Cube dimensions with fixed values
		HashMap<String, List<LDResource>> fixedDimensionValues = CubeBrowsingUtils
				.getFixedDimensionValues(cubeDimensions);

		final FLabel freeVariables = new FLabel("freeVariables");
		String freeVarText = "Visualized variables: ";
		for (String fVar : dimensions4Visualisation.keySet()) {
			if (config.showURIs) {
				freeVarText += fVar + " ";
			} else {
				freeVarText += CubeBrowsingUtils.getURIlabel(fVar) + " ";
			}
		}
		freeVariables.setText(freeVarText);
		cnt.add(freeVariables);

		final FLabel fixedVariables = new FLabel("fixedVariables");
		String fixedVarText = "Fixed variables: ";
		for (String fixVar : fixedDimensionValues.keySet()) {
			if (config.showURIs) {
				fixedVarText += fixVar + "("
						+ fixedDimensionValues.get(fixVar).get(0) + ") ";
			} else {
				fixedVarText += CubeBrowsingUtils.getURIlabel(fixVar)
						+ "("
						+ CubeBrowsingUtils.getURIlabel(fixedDimensionValues
								.get(fixVar).get(0).getURI()) + ") ";
			}
		}
		fixedVariables.setText(fixedVarText);
		cnt.add(fixedVariables);

		// Get query tuples for visualization
		TupleQueryResult res = IWBquery.get2DVisualsiationValues(
				dimensions4Visualisation.keySet(), fixedDimensionValues,
				cubeMeasure, config.dataCubeURI);

		final FTable ftable = new FTable("ftable");
		FTableModel tm = create2DCubeTableModel(res, dimensions4Visualisation,
				config.showURIs);
		ftable.setShowCSVExport(true);
		ftable.setNumberOfRows(20);
		ftable.setEnableFilter(true);
		ftable.setOverFlowContainer(true);
		ftable.setFilterPos(FilterPos.TOP);
		ftable.setSortable(true);
		ftable.setModel(tm);
		cnt.add(ftable);

		return cnt;
	}

	private FTableModel create2DCubeTableModel(TupleQueryResult res,
			HashMap<String, List<LDResource>> dimensions4Visualisation,
			boolean showURIs) {

		List<String> vDimsList = new ArrayList<String>();

		for (String vDim : dimensions4Visualisation.keySet()) {
			vDimsList.add(vDim);
		}

		List<LDResource> dim1 = dimensions4Visualisation.get(vDimsList.get(0));
		List<LDResource> dim2 = dimensions4Visualisation.get(vDimsList.get(1));

		String[][] v2DCube = new String[dim2.size()][dim1.size()];

		FTableModel tm = new FTableModel();

		List<String> bindingNames;
		try {
			bindingNames = res.getBindingNames();
			if (showURIs) {
				tm.addColumn(vDimsList.get(1));
			} else {
				tm.addColumn(CubeBrowsingUtils.getURIlabel(vDimsList.get(1)));

			}
			for (LDResource dim1Val : dim1) {
				if (dim1Val.getLabel() != null) {
					tm.addColumn(dim1Val.getLabel());

				} else {
					if (showURIs) {
						tm.addColumn(dim1Val.getURI());
					} else {
						tm.addColumn(CubeBrowsingUtils.getURIlabel(dim1Val
								.getURI()));
					}
				}
			}

			while (res.hasNext()) {
				BindingSet bindingSet = res.next();

				String dim1Val = bindingSet.getValue(bindingNames.get(0))
						.stringValue();
				String dim2Val = bindingSet.getValue(bindingNames.get(1))
						.stringValue();
				String measure = bindingSet.getValue(bindingNames.get(2))
						.stringValue();
				LDResource r1=new LDResource();
				r1.setURI(dim1Val);
				
				LDResource r2=new LDResource();
				r2.setURI(dim2Val);
				v2DCube[dim2.indexOf(r2)][dim1.indexOf(r1)] = measure;
			}

			for (int i = 0; i < dim2.size(); i++) {
				String[] data = new String[dim1.size() + 1];

				if (dim2.get(i).getLabel() != null) {
					data[0] = dim2.get(i).getLabel();

				} else {
					if (showURIs) {
						data[0] = dim2.get(i).getURI();
					} else {
						data[0] = CubeBrowsingUtils.getURIlabel(dim2.get(i)
								.getURI());
					}
				}

				for (int j = 1; j <= dim1.size(); j++) {
					data[j] = v2DCube[i][j - 1];
				}
				tm.addRow(data);
			}
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