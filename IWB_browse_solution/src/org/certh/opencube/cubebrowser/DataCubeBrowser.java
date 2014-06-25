package org.certh.opencube.cubebrowser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.fluidops.ajax.XMLBuilder.Attribute;
import com.fluidops.ajax.components.FAsynchContainer;
import com.fluidops.ajax.components.FButton;
import com.fluidops.ajax.components.FComboBox;
import com.fluidops.ajax.components.FComponent;
import com.fluidops.ajax.components.FContainer;
import com.fluidops.ajax.components.FGrid;
import com.fluidops.ajax.components.FHTML;
import com.fluidops.ajax.components.FLabel;
import com.fluidops.ajax.components.FSlider;
import com.fluidops.ajax.components.FTable;
import com.fluidops.ajax.components.FTable.FilterPos;
import com.fluidops.ajax.helper.HtmlString;
import com.fluidops.ajax.models.FTableModel;

import com.fluidops.iwb.model.ParameterConfigDoc;
import com.fluidops.iwb.page.PageContext;
import com.fluidops.iwb.repository.SPARQLBasicAuthRepositoryConfig;
import com.fluidops.iwb.widget.AbstractWidget;
import com.fluidops.iwb.widget.Widget;
import com.fluidops.iwb.widget.config.WidgetBaseConfig;
import com.fluidops.util.Array;
import com.fluidops.util.Pair;
import com.fluidops.util.Rand;

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

	private FContainer leftcontainer = new FContainer("leftcontainer");

	private FContainer rightcontainer = new FContainer("rightcontainer");

	private List<LDResource> cubeDimensions = new ArrayList<LDResource>();

	private List<LDResource> visualDimensions = new ArrayList<LDResource>();

	private List<LDResource> fixedDimensions = new ArrayList<LDResource>();

	private HashMap<LDResource, List<LDResource>> allDimensionsValues = new HashMap<LDResource, List<LDResource>>();

	private HashMap<LDResource, LDResource> fixedDimensionsSelectedValues = new HashMap<LDResource, LDResource>();

	private HashMap<LDResource, List<FComponent>> dimensionURIfcomponents = new HashMap<LDResource, List<FComponent>>();

	private List<String> cubeMeasure = new ArrayList<String>();

	private String datacubeURI = "";
	
	private String SPARQL_service="";

	// create table model for visualization
	private FTable ftable = new FTable("ftable");

	public static class Config extends WidgetBaseConfig{
		@ParameterConfigDoc(desc = "The data cube URI to visualise", required = true)
		public String dataCubeURI;

		@ParameterConfigDoc(desc = "Use code lists to get dimension values", required = false)
		public boolean useCodeLists;
		
		@ParameterConfigDoc(desc = "SPARQL service to forward queries", required = false)
		public String sparqlService;
				
	}

	@Override
	protected FComponent getComponent(String id) {

		final Config config = get();

		// Central container
		FContainer cnt = new FContainer(id);
	
		datacubeURI = config.dataCubeURI;
		
		SPARQL_service=config.sparqlService;

		// Get all Cube dimensions
		long totalstarttime = System.currentTimeMillis();

		long startTime = System.currentTimeMillis();

		cubeDimensions.addAll(IWBquery.getDataCubeDimensions(datacubeURI,SPARQL_service));

		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		System.out.println("Time cubeDimensions: " + elapsedTime);

		// Get the Cube measure
		startTime = System.currentTimeMillis();

		cubeMeasure.addAll(IWBquery.getDataCubeMeasure(datacubeURI,SPARQL_service));

		stopTime = System.currentTimeMillis();
		elapsedTime = stopTime - startTime;
		System.out.println("Time cubeMeasure: " + elapsedTime);
		// Get values for each dimension
		startTime = System.currentTimeMillis();
		allDimensionsValues.putAll(CubeBrowsingUtils.getDimsValues(
				cubeDimensions, datacubeURI,config.useCodeLists,SPARQL_service));
		
		stopTime = System.currentTimeMillis();
		elapsedTime = stopTime - startTime;
		System.out.println("Time allDimensionsValues: " + elapsedTime);

		// Dimensions for visualization
		startTime = System.currentTimeMillis();

		visualDimensions.addAll(CubeBrowsingUtils.getRandomDims4Visualisation(
				cubeDimensions, allDimensionsValues));

		stopTime = System.currentTimeMillis();
		elapsedTime = stopTime - startTime;
		System.out.println("Time visualDimensions: " + elapsedTime);

		// Fixed dimensions
		startTime = System.currentTimeMillis();

		fixedDimensions.addAll(CubeBrowsingUtils.getFixedDimensions(
				cubeDimensions, visualDimensions));

		stopTime = System.currentTimeMillis();
		elapsedTime = stopTime - startTime;
		System.out.println("Time fixedDimensions: " + elapsedTime);

		// Selected values for the fixed dimensions
		startTime = System.currentTimeMillis();

		fixedDimensionsSelectedValues.putAll(CubeBrowsingUtils
				.getFixedDimensionsRandomSelectedValues(allDimensionsValues,
						fixedDimensions));
		stopTime = System.currentTimeMillis();
		elapsedTime = stopTime - startTime;
		System.out
				.println("Time fixedDimensionsSelectedValues: " + elapsedTime);

		// Get query tuples for visualization
		startTime = System.currentTimeMillis();

		TupleQueryResult res = IWBquery.get2DVisualsiationValues(
				visualDimensions, fixedDimensionsSelectedValues, cubeMeasure,
				allDimensionsValues, datacubeURI,SPARQL_service);

		stopTime = System.currentTimeMillis();
		elapsedTime = stopTime - startTime;
		System.out.println("Time IWBquery.get2DVisualsiationValues: "
				+ elapsedTime);

		startTime = System.currentTimeMillis();

		FTableModel tm = create2DCubeTableModel(res, allDimensionsValues,
				visualDimensions);

		stopTime = System.currentTimeMillis();
		elapsedTime = stopTime - startTime;
		System.out.println("Time create2DCubeTableModel: " + elapsedTime);

		startTime = System.currentTimeMillis();

		// set ftable attributes
		// ftable.setShowCSVExport(true);
		ftable.setNumberOfRows(20);
		ftable.setEnableFilter(true);
		ftable.setOverFlowContainer(true);
		ftable.setFilterPos(FilterPos.TOP);
		ftable.setSortable(false);
		ftable.setModel(tm);

		cnt.add(ftable);

		// cnt.add(ftable_cnt);

		// Add label for Dim1 (column headings)
		FLabel dim1Label = new FLabel("dim1Label", "<b>Column Headings<b>");
		leftcontainer.add(dim1Label);

		// Add Combobox for Dim1
		final FComboBox dim1Combo = new FComboBox("dim1Combo") {
			@Override
			public void onChange() {

				// Get the URI of the 1st selected dimension
				List<String> d1Selected = this.getSelectedAsString();

				// Get the URI of the 2nd selected dimension
				List<String> d2Selected = null;

				for (FComponent fc : leftcontainer.getAllComponents()) {
					if (fc.getId().contains("_dim2Combo")) {
						d2Selected = ((FComboBox) fc).getSelectedAsString();

						// If both combo boxes have the same selected value
						// Select randomly another value for d2
						if (d1Selected.get(0).equals(d2Selected.get(0))) {
							List<Pair<String, Object>> d2choices = ((FComboBox) fc)
									.getChoices();
							for (Pair<String, Object> pair : d2choices) {
								if (!pair.snd.toString().equals(
										d2Selected.get(0))) {
									d2Selected.clear();
									d2Selected.add(pair.snd.toString());
									((FComboBox) fc).setPreSelected(pair.snd
											.toString());
									((FComboBox) fc).populateView();
									break;
								}
							}
						}
						break;
					}
				}
				setVisualDimensions(d1Selected, d2Selected);
				showCube();
			}
		};

		for (LDResource ldr : cubeDimensions) {
			dim1Combo.addChoice(ldr.getURIorLabel(), ldr.getURI());
		}

		dim1Combo.setPreSelected(visualDimensions.get(0).getURI());
		leftcontainer.add(dim1Combo);

		// NEW LINE
		leftcontainer.add(getNewLineComponent());

		// Add label for Dim1 (column headings)
		FLabel dim2Label = new FLabel(
				"dim2Label",
				"<b>Rows (values in first column)</b> &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp");
		leftcontainer.add(dim2Label);

		// Add Combobox for Dim2
		final FComboBox dim2Combo = new FComboBox("dim2Combo") {
			@Override
			public void onChange() {
				List<String> d1Selected = null;

				// Get the URI of the 2nd selected dimension
				List<String> d2Selected = this.getSelectedAsString();

				// Get the URI of the 1st selected dimension
				for (FComponent fc : leftcontainer.getAllComponents()) {
					if (fc.getId().contains("_dim1Combo")) {
						d1Selected = ((FComboBox) fc).getSelectedAsString();

						// Both combo boxes have the same selected value
						// Select randomly another value for d2
						if (d1Selected.get(0).equals(d2Selected.get(0))) {
							List<Pair<String, Object>> d1choices = ((FComboBox) fc)
									.getChoices();
							for (Pair<String, Object> pair : d1choices) {
								if (!pair.snd.toString().equals(
										d1Selected.get(0))) {
									d1Selected.clear();
									d1Selected.add(pair.snd.toString());
									((FComboBox) fc).setPreSelected(pair.snd
											.toString());
									((FComboBox) fc).populateView();
									break;
								}

							}

						}
						break;
					}
				}

				setVisualDimensions(d1Selected, d2Selected);
				showCube();
			}
		};

		for (LDResource ldr : cubeDimensions) {
			dim2Combo.addChoice(ldr.getURIorLabel(), ldr.getURI());
		}

		dim2Combo.setPreSelected(visualDimensions.get(1).getURI());
		leftcontainer.add(dim2Combo);

		// NEW LINE
		leftcontainer.add(getNewLineComponent());

		dimensionURIfcomponents.clear();
		FLabel otheropts = new FLabel("Oprions", "<b>Other options<b>");

		rightcontainer.add(otheropts);

		// NEW LINE
		rightcontainer.add(getNewLineComponent());

		for (LDResource fDim : fixedDimensions) {

			List<FComponent> dimComponents = new ArrayList<FComponent>();
			FLabel fDimLabel = new FLabel(fDim.getURIorLabel() + "_label",
					"<b>" + fDim.getURIorLabel() + "</b>");
			dimComponents.add(fDimLabel);
			rightcontainer.add(fDimLabel);
			FComboBox fDimCombo = new FComboBox(fDim.getURIorLabel() + "_combo") {
				@Override
				public void onChange() {
					showCube();
				}
			};

			for (LDResource ldr : allDimensionsValues.get(fDim)) {
				fDimCombo.addChoice(ldr.getURIorLabel(), ldr.getURI());
			}

			fDimCombo.setPreSelected(fixedDimensionsSelectedValues.get(fDim)
					.getURI());

			dimComponents.add(fDimCombo);
			rightcontainer.add(fDimCombo);

			rightcontainer.add(getNewLineComponent());
			dimensionURIfcomponents.put(fDim, dimComponents);
		}

		stopTime = System.currentTimeMillis();
		elapsedTime = stopTime - startTime;
		System.out.println("Time other visual: " + elapsedTime);

		elapsedTime = stopTime - totalstarttime;
		System.out.println("Total time: " + elapsedTime);

		FGrid fg = new FGrid("mygrid");
		ArrayList<FComponent> farray = new ArrayList<FComponent>();
		
		farray.add(leftcontainer);
		farray.add(rightcontainer);
		fg.addRow(farray);
		fg.render();
		cnt.add(fg);

		return cnt;
	}

	private void setVisualDimensions(List<String> d1Selected,
			List<String> d2Selected) {
		// A tmp list to store the new dimensions for visualization
		List<LDResource> tmpvisualDimensions = new ArrayList<LDResource>();
		tmpvisualDimensions.add(null);
		tmpvisualDimensions.add(null);

		for (LDResource ldres : cubeDimensions) {
			// The first dimension
			if (ldres.getURI().equals(d1Selected.get(0))) {
				tmpvisualDimensions.set(0, ldres);
			}

			// The second dimension
			if (ldres.getURI().equals(d2Selected.get(0))) {
				tmpvisualDimensions.set(1, ldres);
			}
		}

		// Update the Global visual dimensions
		visualDimensions.clear();
		visualDimensions.addAll(tmpvisualDimensions);

		// remove previous combo boxes
		for (LDResource fDim : fixedDimensions) {
			Collection<FComponent> allcomp = rightcontainer.getAllComponents();
			for (FComponent comp : allcomp) {
				// System.out.println(comp.getId());
				if (comp.getId().contains(fDim.getURIorLabel() + "_label")
						|| comp.getId().contains(
								fDim.getURIorLabel() + "_combo"))
					rightcontainer.removeAndRefresh(comp);
			}
		}

		// Tmp Fixed dimensions
		List<LDResource> tmpFixedDimensions = CubeBrowsingUtils
				.getFixedDimensions(cubeDimensions, visualDimensions);

		// Update Global fixed dimensions
		fixedDimensions.clear();
		fixedDimensions.addAll(tmpFixedDimensions);

		// Tmp Selected values for the fixed dimensions
		HashMap<LDResource, LDResource> tmpFixedDimensionsSelectedValues = CubeBrowsingUtils
				.getFixedDimensionsRandomSelectedValues(allDimensionsValues,
						fixedDimensions);

		// Update global selected values
		fixedDimensionsSelectedValues.clear();
		fixedDimensionsSelectedValues.putAll(tmpFixedDimensionsSelectedValues);

		// Clear the map with Dimension URI - List of components (Label,
		// Combobox)
		dimensionURIfcomponents.clear();

		// For each fixed dimension add new Label/Combobox
		for (LDResource fDim : fixedDimensions) {

			List<FComponent> dimComponents = new ArrayList<FComponent>();

			// Add label
			FLabel fDimLabel = new FLabel(fDim.getURIorLabel() + "_label",
					"<b>" + fDim.getURIorLabel() + "</b>");
			dimComponents.add(fDimLabel);
			rightcontainer.addAndRefresh(fDimLabel);

			// Add Combobox
			FComboBox fDimCombo = new FComboBox(fDim.getURIorLabel() + "_combo") {
				@Override
				public void onChange() {
					showCube();

				}
			};

			// Add choices to the combo box
			for (LDResource ldr : allDimensionsValues.get(fDim)) {
				fDimCombo.addChoice(ldr.getURIorLabel(), ldr.getURI());
			}

			// Set preselected value to combo box
			fDimCombo.setPreSelected(fixedDimensionsSelectedValues.get(fDim)
					.getURI());

			dimComponents.add(fDimCombo);
			rightcontainer.addAndRefresh(fDimCombo);

			rightcontainer.add(getNewLineComponent());
			// Add both components to the URI - Component list Map
			dimensionURIfcomponents.put(fDim, dimComponents);
		}
		rightcontainer.initializeView();

	}

	private void showCube() {

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
					tmpFixedDimensionsSelectedValues.put(dimres, dimValue);

				}
			}
		}

		fixedDimensionsSelectedValues.clear();
		fixedDimensionsSelectedValues.putAll(tmpFixedDimensionsSelectedValues);

		// Get query tuples for visualization
		TupleQueryResult res = IWBquery.get2DVisualsiationValues(
				visualDimensions, fixedDimensionsSelectedValues, cubeMeasure,
				allDimensionsValues, datacubeURI,SPARQL_service);

		// create table model for visualization

		FTableModel newTableModel = create2DCubeTableModel(res,
				allDimensionsValues, visualDimensions);
		ftable.setModel(newTableModel);
		ftable.populateView();

	}

	private FTableModel create2DCubeTableModel(TupleQueryResult res,
			HashMap<LDResource, List<LDResource>> dimensions4Visualisation,
			List<LDResource> visualDimensions) {

		List<LDResource> dim1 = dimensions4Visualisation.get(visualDimensions
				.get(0));
		List<LDResource> dim2 = dimensions4Visualisation.get(visualDimensions
				.get(1));

		LDResource[][] v2DCube = new LDResource[dim2.size()][dim1.size()];

		FTableModel tm = new FTableModel();

		List<String> bindingNames;
		try {
			bindingNames = res.getBindingNames();
			tm.addColumn(visualDimensions.get(1).getURIorLabel());

			for (LDResource dim1Val : dim1) {
				tm.addColumn(dim1Val.getURIorLabel());
			}

			while (res.hasNext()) {

				BindingSet bindingSet = res.next();

				String dim1Val = bindingSet.getValue(bindingNames.get(0))
						.stringValue();
				String dim2Val = bindingSet.getValue(bindingNames.get(1))
						.stringValue();
				String measure = bindingSet.getValue(bindingNames.get(2))
						.stringValue();
				String obsURI = bindingSet.getValue(bindingNames.get(3))
						.stringValue();

				LDResource r1 = new LDResource(dim1Val);

				LDResource r2 = new LDResource(dim2Val);

				LDResource obs = new LDResource(obsURI);
				obs.setLabel(measure);

				v2DCube[dim2.indexOf(r2)][dim1.indexOf(r1)] = obs;
			}

			for (int i = 0; i < dim2.size(); i++) {
				Object[] data = new Object[dim1.size() + 1];

				// Add row header (values in first column)
				data[0] = getHTMLStringFromLDResource(dim2.get(i));

				// Add observations
				for (int j = 1; j <= dim1.size(); j++) {
					data[j] = getHTMLStringFromLDResource(v2DCube[i][j - 1]);
				}

				tm.addRow(data);
			}

		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		}
		return tm;
	}

	private HtmlString getHTMLStringFromLDResource(LDResource ldr) {
		if (ldr == null) {
			return new HtmlString("EMPTY VALUE");
		}
		String linktext = ldr.getURIorLabel();
		String linkURI = ldr.getURI();
		String html = "<a href=\"" + linkURI + "\">" + linktext + "</a>";

		HtmlString html_string = new HtmlString(html);
		return html_string;

	}

	private FHTML getNewLineComponent() {
		Random rand = new Random();
		FHTML fhtml = new FHTML("ftml_" + rand.nextLong());
		fhtml.setValue("<br><br>");
		return fhtml;
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
				String[] data = new String[size2];
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
