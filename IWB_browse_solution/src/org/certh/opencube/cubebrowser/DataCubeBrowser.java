package org.certh.opencube.cubebrowser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.fluidops.ajax.components.FButton;
import com.fluidops.ajax.components.FComboBox;
import com.fluidops.ajax.components.FComponent;
import com.fluidops.ajax.components.FContainer;
import com.fluidops.ajax.components.FHTML;
import com.fluidops.ajax.components.FLabel;
import com.fluidops.ajax.components.FTable;
import com.fluidops.ajax.components.FTable.FilterPos;
import com.fluidops.ajax.models.FTableModel;

import com.fluidops.iwb.model.ParameterConfigDoc;
import com.fluidops.iwb.widget.AbstractWidget;
import com.sun.javafx.collections.MappingChange.Map;

import org.certh.opencube.utils.CubeBrowsingUtils;
import org.certh.opencube.utils.IWBquery;
import org.certh.opencube.utils.LDResource;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

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

		final Config config = get();

		// the layouting container for this widget
		// the container must use the provided id
		final FContainer cnt = new FContainer(id);

		// now we can add other components to the container
		// the simplest is to add them line by line

		// Get all Cube dimensions
		final List<LDResource> cubeDimensions = IWBquery
				.getDataCubeDimensions(config.dataCubeURI);

		// Get the Cube measure
		final List<String> cubeMeasure = IWBquery
				.getDataCubeMeasure(config.dataCubeURI);

		// Get values for each dimension
		final HashMap<LDResource, List<LDResource>> allDimensionsValues = CubeBrowsingUtils
				.getDimsValues(cubeDimensions);

		// Dimensions for visualization
		final List<LDResource> visualDimensions = CubeBrowsingUtils
				.getRandomDims4Visualisation(cubeDimensions,
						allDimensionsValues);

		// Fixed dimensions
		final List<LDResource> fixedDimensions = CubeBrowsingUtils
				.getFixedDimensions(cubeDimensions, visualDimensions);

		// Selected values for the fixed dimensions
		final HashMap<LDResource, LDResource> fixedDimensionsSelectedValues = CubeBrowsingUtils
				.getFixedDimensionsRandomSelectedValues(allDimensionsValues,
						fixedDimensions);

		// Get query tuples for visualization
		TupleQueryResult res = IWBquery.get2DVisualsiationValues(
				visualDimensions, fixedDimensionsSelectedValues, cubeMeasure,
				config.dataCubeURI);

		// create table model for visualization
		final FTable ftable = new FTable("ftable");
		FTableModel tm = create2DCubeTableModel(res, allDimensionsValues,
				visualDimensions, config.showURIs);

		// set ftable attributes
		ftable.setShowCSVExport(true);
		ftable.setNumberOfRows(20);
		ftable.setEnableFilter(true);
		ftable.setOverFlowContainer(true);
		ftable.setFilterPos(FilterPos.TOP);
		ftable.setSortable(true);
		ftable.setModel(tm);

		cnt.add(ftable);

		// Add label for Dim1 (column headings)
		FLabel dim1Label = new FLabel("dim1Label", "Column Headings");
		cnt.add(dim1Label);

		// Add Combobox for Dim1
		final FComboBox dim1Combo = new FComboBox("dim1Combo");

		for (LDResource ldr : cubeDimensions) {
			dim1Combo.addChoice(ldr.getURIorLabel(), ldr.getURI());
		}

		dim1Combo.setPreSelected(visualDimensions.get(0).getURI());
		cnt.add(dim1Combo);

		//NEW LINE
		FHTML fhtml2 = new FHTML("ftml2");
		fhtml2.setValue("<br><br>");
		cnt.add(fhtml2);

		// Add label for Dim1 (column headings)
		FLabel dim2Label = new FLabel("dim2Label",
				"Rows (values in first column) ");
		cnt.add(dim2Label);

		// Add Combobox for Dim2
		final FComboBox dim2Combo = new FComboBox("dim2Combo");

		for (LDResource ldr : cubeDimensions) {
			dim2Combo.addChoice(ldr.getURIorLabel(), ldr.getURI());
		}

		dim2Combo.setPreSelected(visualDimensions.get(1).getURI());
		cnt.add(dim2Combo);

		//NEW LINE
		FHTML fhtml3 = new FHTML("ftml3");
		fhtml3.setValue("<br><br>");
		cnt.add(fhtml3);

		final HashMap<LDResource, List<FComponent>> dimensionURIfcomponents = new HashMap<LDResource, List<FComponent>>();

		//Button to set free (visual) dimensions
		FButton btnSetFreeDims = new FButton("btn_setFreeDims", "Set free dims") {
			@Override
			public void onClick() {
				
				//Get the URI of the 1st selected dimension
				List<String> d1Selected = dim1Combo.getSelectedAsString();

				//Get the URI of the 2nd selected dimension
				List<String> d2Selected = dim2Combo.getSelectedAsString();

				// A tmp list to store the new dimensions for visualization
				List<LDResource> tmpvisualDimensions = new ArrayList<LDResource>(2);
		
				for (LDResource ldres : cubeDimensions) {
					//The first dimension
					if (ldres.getURI().equals(d1Selected.get(0))) {
						tmpvisualDimensions.set(0, ldres);
					}

					//The second dimension
					if (ldres.getURI().equals(d2Selected.get(0))) {
						tmpvisualDimensions.set(1, ldres);
					}
				}

				//Update the Global visual dimensions
				visualDimensions.clear();
				visualDimensions.addAll(tmpvisualDimensions);

				// remove previous combo boxes
				for (LDResource fDim : fixedDimensions) {
					Collection<FComponent> allcomp = cnt.getAllComponents();
					for (FComponent comp : allcomp) {
						System.out.println(comp.getId());
						if (comp.getId().contains(
								fDim.getURIorLabel() + "_label")
								|| comp.getId().contains(
										fDim.getURIorLabel() + "_combo"))
							cnt.removeAndRefresh(comp);

					}
				}

				// Tmp Fixed dimensions
				List<LDResource> tmpFixedDimensions = CubeBrowsingUtils
						.getFixedDimensions(cubeDimensions, visualDimensions);

				//Update Global fixed dimensions
				fixedDimensions.clear();
				fixedDimensions.addAll(tmpFixedDimensions);

				// Tmp Selected values for the fixed dimensions
				HashMap<LDResource, LDResource> tmpFixedDimensionsSelectedValues = CubeBrowsingUtils
						.getFixedDimensionsRandomSelectedValues(
								allDimensionsValues, fixedDimensions);

				//Update global selected values
				fixedDimensionsSelectedValues.clear();
				fixedDimensionsSelectedValues
						.putAll(tmpFixedDimensionsSelectedValues);

				//Clear the map with Dimension URI - List of components (Label, Combobox) 
				dimensionURIfcomponents.clear();
				
				//For each fixed dimension add new Label/Combobox 
				for (LDResource fDim : fixedDimensions) {

					List<FComponent> dimComponents = new ArrayList<FComponent>();
								
					//Add label
					FLabel fDimLabel = new FLabel(fDim.getURIorLabel()
							+ "_label", fDim.getURIorLabel());
					dimComponents.add(fDimLabel);
					cnt.addAndRefresh(fDimLabel);
					
					//Add Combobox
					FComboBox fDimCombo = new FComboBox(fDim.getURIorLabel()
							+ "_combo");
					
					//Add choices to the combo box
					for (LDResource ldr : allDimensionsValues.get(fDim)) {
						fDimCombo.addChoice(ldr.getURIorLabel(), ldr.getURI());
					}

					//Set preselected value to combo box
					fDimCombo.setPreSelected(fixedDimensionsSelectedValues.get(
							fDim).getURI());
					
					dimComponents.add(fDimCombo);
					cnt.addAndRefresh(fDimCombo);
					
					//Add both components to the URI - Component list Map
					dimensionURIfcomponents.put(fDim, dimComponents);
				}
				cnt.initializeView();

			}
		};

		//Button to show the new cube based on user selections
		FButton btnShowCube = new FButton("btn_showCube", "Show cube") {
			@Override
			public void onClick() {

				HashMap<LDResource, LDResource> tmpFixedDimensionsSelectedValues = new HashMap<LDResource, LDResource>();

				for (LDResource dimres : dimensionURIfcomponents.keySet()) {
					List<FComponent> dimComponents = dimensionURIfcomponents
							.get(dimres);
					String selectedValue = ((FComboBox) dimComponents.get(1))
							.getSelectedAsString().get(0);
					List<LDResource> selectedDimValues = allDimensionsValues
							.get(dimres);
					for (LDResource dimValue : selectedDimValues) {
						if (dimValue.getURI().equals(selectedValue)) {
							tmpFixedDimensionsSelectedValues.put(dimres,
									dimValue);

						}
					}
				}

				for (LDResource l : visualDimensions) {
					System.out.println("visual: " + l.getURI());
				}
				fixedDimensionsSelectedValues.clear();
				fixedDimensionsSelectedValues
						.putAll(tmpFixedDimensionsSelectedValues);

				// Get query tuples for visualization
				TupleQueryResult res = IWBquery.get2DVisualsiationValues(
						visualDimensions, fixedDimensionsSelectedValues,
						cubeMeasure, config.dataCubeURI);

				// create table model for visualization

				FTableModel newTableModel = create2DCubeTableModel(res,
						allDimensionsValues, visualDimensions, config.showURIs);
				ftable.setModel(newTableModel);
				ftable.populateView();

			}
		};

		cnt.add(btnSetFreeDims);
		cnt.add(btnShowCube);

		dimensionURIfcomponents.clear();
		for (LDResource fDim : fixedDimensions) {

			List<FComponent> dimComponents = new ArrayList<FComponent>();
			FLabel fDimLabel = new FLabel(fDim.getURIorLabel() + "_label",
					fDim.getURIorLabel());
			dimComponents.add(fDimLabel);
			cnt.add(fDimLabel);
			FComboBox fDimCombo = new FComboBox(fDim.getURIorLabel() + "_combo");

			for (LDResource ldr : allDimensionsValues.get(fDim)) {
				fDimCombo.addChoice(ldr.getURIorLabel(), ldr.getURI());
			}

			fDimCombo.setPreSelected(fixedDimensionsSelectedValues.get(fDim)
					.getURI());

			dimComponents.add(fDimCombo);
			cnt.add(fDimCombo);

			dimensionURIfcomponents.put(fDim, dimComponents);
		}

		return cnt;
	}

	private FTableModel create2DCubeTableModel(TupleQueryResult res,
			HashMap<LDResource, List<LDResource>> dimensions4Visualisation,
			List<LDResource> visualDimensions, boolean showURIs) {

		long startTime = System.currentTimeMillis();
	
		List<LDResource> dim1 = dimensions4Visualisation.get(visualDimensions
				.get(0));
		List<LDResource> dim2 = dimensions4Visualisation.get(visualDimensions
				.get(1));

		// HashMap<String, Integer>
		// dim1Index=CubeBrowsingUtils.getListIndexMap(dim1);
		// HashMap<String, Integer>
		// dim2Index=CubeBrowsingUtils.getListIndexMap(dim2);

		String[][] v2DCube = new String[dim2.size()][dim1.size()];

		FTableModel tm = new FTableModel();

		List<String> bindingNames;
		try {
			bindingNames = res.getBindingNames();
			tm.addColumn(visualDimensions.get(1).getURIorLabel());

			for (LDResource dim1Val : dim1) {
				tm.addColumn(dim1Val.getURIorLabel());

			}

			long stopTime = System.currentTimeMillis();
			long elapsedTime = stopTime - startTime;
			System.out.println("Time 1: " + elapsedTime);

			startTime = System.currentTimeMillis();

			startTime = System.currentTimeMillis();
			while (res.hasNext()) {

				BindingSet bindingSet = res.next();

				String dim1Val = bindingSet.getValue(bindingNames.get(0))
						.stringValue();
				String dim2Val = bindingSet.getValue(bindingNames.get(1))
						.stringValue();
				String measure = bindingSet.getValue(bindingNames.get(2))
						.stringValue();

				LDResource r1 = new LDResource(dim1Val);

				LDResource r2 = new LDResource(dim2Val);

				v2DCube[dim2.indexOf(r2)][dim1.indexOf(r1)] = measure;

				// v2DCube[dim2Index.get(dim2Val)][dim1Index.get(dim1Val)] =
				// measure;

			}
			stopTime = System.currentTimeMillis();
			elapsedTime = stopTime - startTime;
			System.out.println("Time 3: " + elapsedTime);

			for (int i = 0; i < dim2.size(); i++) {
				String[] data = new String[dim1.size() + 1];

				String linktext=dim2.get(i).getURIorLabel();
				String linkURI=dim2.get(i).getURI();
			//	FHTML fhtml=new FHTML("test");
				
			//	fhtml.setValue("<a href=\""+linkURI+"\">"+linktext+"</a>");
			//	tm.addColumn(fhtml);
				
				data[0]=linkURI;
			//	data[0] = dim2.get(i).getURIorLabel();

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