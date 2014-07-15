package org.certh.opencube.cubebrowser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.certh.opencube.SPARQL.AggregationSPARQL;
import org.certh.opencube.SPARQL.CubeBrowserSPARQL;
import org.certh.opencube.SPARQL.CubeSPARQL;
import org.certh.opencube.SPARQL.SliceSPARQL;
import org.certh.opencube.utils.CubeHandlingUtils;
import org.certh.opencube.utils.LDResource;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

import com.fluidops.ajax.components.FButton;
import com.fluidops.ajax.components.FCheckBox;
import com.fluidops.ajax.components.FComboBox;
import com.fluidops.ajax.components.FComponent;
import com.fluidops.ajax.components.FContainer;
import com.fluidops.ajax.components.FDialog;
import com.fluidops.ajax.components.FGrid;
import com.fluidops.ajax.components.FHTML;
import com.fluidops.ajax.components.FLabel;
import com.fluidops.ajax.components.FTable;
import com.fluidops.ajax.components.FTable.FilterPos;
import com.fluidops.ajax.helper.HtmlString;
import com.fluidops.ajax.models.FTableModel;
import com.fluidops.iwb.model.ParameterConfigDoc;
import com.fluidops.iwb.widget.AbstractWidget;
import com.fluidops.iwb.widget.config.WidgetBaseConfig;
import com.fluidops.util.Pair;

/**
 * On some wiki page add
 * 
 * <code>
 * = Test my demo widget =
 * 
 * <br/>
 * {{#widget: org.certh.opencube.cubebrowser.DataCubeBrowser|
 *  dataCubeURI= '<http://eurostat.linked-statistics.org/data/cdh_e_fsp>'
 *  |asynch='true'|useCodeLists='true'|sparqlService='<http://localhost:3031/dcbrowser/query>'
 *  }}
 * 
 * </code>
 * 
 */
public class DataCubeBrowser extends AbstractWidget<DataCubeBrowser.Config> {

	// The top container to show the check boxes with the available aggregation
	// set dimensions
	private FContainer topcontainer = new FContainer("topcontainer");

	// The left container to show the combo boxes with the visual dimensions
	private FContainer leftcontainer = new FContainer("leftcontainer");

	// The right container to show the combo boxes with the fixed dimensions
	// values
	private FContainer rightcontainer = new FContainer("rightcontainer");

	// All the cube dimensions
	private List<LDResource> cubeDimensions = new ArrayList<LDResource>();

	// All the dimensions of all cubes of the same aggregation set
	private List<LDResource> aggregationSetDims = new ArrayList<LDResource>();

	// All the dimensions per cube of the aggregation set
	private HashMap<LDResource, List<LDResource>> cubeDimsOfAggregationSet = new HashMap<LDResource, List<LDResource>>();

	// The cube dimensions to visualize (2 dimensions)
	private List<LDResource> visualDimensions = new ArrayList<LDResource>();

	// The fixed dimensions
	private List<LDResource> fixedDimensions = new ArrayList<LDResource>();

	// The slice fixed dimensions
	private List<LDResource> sliceFixedDimensions = new ArrayList<LDResource>();

	// All the cube observations - to be used to create a slice
	private List<LDResource> sliceObservations = new ArrayList<LDResource>();

	// A map (dimension URI - dimension values) with all the cube dimension
	// values
	private HashMap<LDResource, List<LDResource>> allDimensionsValues = new HashMap<LDResource, List<LDResource>>();

	// The selected value for each fixed dimension
	private HashMap<LDResource, LDResource> fixedDimensionsSelectedValues = new HashMap<LDResource, LDResource>();

	// The selected value for each fixed dimension
	private HashMap<LDResource, LDResource> sliceFixedDimensionsValues = new HashMap<LDResource, LDResource>();

	// A map with the corresponding components for each fixed cube dimension
	private HashMap<LDResource, List<FComponent>> dimensionURIfcomponents = new HashMap<LDResource, List<FComponent>>();

	// A map with the Aggregation Set Dimension URi and the corresponding Check
	// box
	private HashMap<LDResource, FCheckBox> mapDimURIcheckBox = new HashMap<LDResource, FCheckBox>();

	// The cube measures
	private List<String> cubeMeasure = new ArrayList<String>();

	// The cube URI to visualize (required)
	private String cubeSliceURI = "";

	// The SPARQL service to get data (not required)
	private String SPARQL_service = "";

	// The graph of the cube
	private String cubeGraph = null;

	// The graph of the cube structure
	private String cubeDSDGraph = null;

	// The graph of the cube structure
	private String sliceGraph = null;

	// The table model for visualization of the cube
	private FTable ftable = new FTable("ftable");

	// True if URI is type qb:DataSet
	private boolean isCube;

	// True if URI is type qb:Slice
	private boolean isSlice;

	// True if code list will be used to get the cube dimension values
	private boolean useCodeLists;

	// True if widget is loaded for the first time
	private boolean isFirstLoad = true;

	// The central container
	private FContainer cnt = null;

	public static class Config extends WidgetBaseConfig {
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
		cnt = new FContainer(id);

		// Get the cube URI from the widget configuration
		cubeSliceURI = config.dataCubeURI;

		// Get the SPARQL service (if any) from the cube configuration
		SPARQL_service = config.sparqlService;

		// The the use code list parameter from the widget config
		useCodeLists = config.useCodeLists;

		// Prepare and show the widget
		populateCentralContainer();

		isFirstLoad = false;

		return cnt;
	}

	private void populateCentralContainer() {

		// Get Cube/Slice Graph
		String cubeSliceGraph = CubeSPARQL.getCubeSliceGraph(cubeSliceURI,
				SPARQL_service);

		// Get the type of the URI i.e. cube / slice
		String cubeSliceType = CubeSPARQL.getType(cubeSliceURI, cubeSliceGraph,
				SPARQL_service);

		if (cubeSliceType != null) {
			// The URI corresponds to a data cube
			isCube = cubeSliceType
					.equals("http://purl.org/linked-data/cube#DataSet");

			// The URI corresponds to a cube Slice
			isSlice = cubeSliceType
					.equals("http://purl.org/linked-data/cube#Slice");
		} else {
			isCube = false;
			isSlice = false;
		}

		// If the URI is a valid cube or slice URI
		if (isCube || isSlice) {

			if (isCube) {

				// The cube graph is the graph of the URI computed above
				cubeGraph = cubeSliceGraph;

				// Get Cube Structure graph
				cubeDSDGraph = CubeSPARQL.getCubeStructureGraph(cubeSliceURI,
						cubeGraph, SPARQL_service);

				// Get all Cube dimensions
				cubeDimensions = CubeSPARQL.getDataCubeDimensions(cubeSliceURI,
						cubeGraph, cubeDSDGraph, SPARQL_service);

				// Get the Cube measure
				cubeMeasure = CubeSPARQL.getDataCubeMeasure(cubeSliceURI,
						cubeGraph, cubeDSDGraph, SPARQL_service);

				// Get all the dimensions of the aggregation set the cube
				// belongs
				aggregationSetDims = AggregationSPARQL
						.getAggegationSetDimsFromCube(cubeSliceURI,
								cubeDSDGraph, SPARQL_service);

				// Get all the dimensions per cube of the aggregations set
				cubeDimsOfAggregationSet = AggregationSPARQL
						.getCubeAndDimensionsOfAggregateSet(cubeSliceURI,
								cubeDSDGraph, SPARQL_service);

			} else if (isSlice) {

				// The slice graph is the graph of the URI computed above
				sliceGraph = cubeSliceGraph;

				// Get the cube graph from the slice
				cubeGraph = SliceSPARQL.getCubeGraphFromSlice(cubeSliceURI,
						sliceGraph, SPARQL_service);

				// Get Cube Structure graph from slice
				cubeDSDGraph = SliceSPARQL.getCubeStructureGraphFromSlice(
						cubeSliceURI, sliceGraph, SPARQL_service);

				// Get slice fixed dimensions
				sliceFixedDimensions = SliceSPARQL.getSliceFixedDimensions(
						cubeSliceURI, sliceGraph, cubeDSDGraph, SPARQL_service);

				sliceFixedDimensionsValues = SliceSPARQL
						.getSliceFixedDimensionsValues(sliceFixedDimensions,
								cubeSliceURI, sliceGraph, cubeDSDGraph,
								SPARQL_service);

				// Get all cube dimensions
				List<LDResource> cubeDimsFromSlice = SliceSPARQL
						.getDataCubeDimensionsFromSlice(cubeSliceURI,
								sliceGraph, cubeDSDGraph, SPARQL_service);

				// The slice visual dimensions are: (all cube dims) - (slice
				// fixed dims)
				// cubeDimensions.addAll(cubeDimsFromSlice);
				cubeDimensions = cubeDimsFromSlice;
				cubeDimensions.removeAll(sliceFixedDimensions);

				// Get the Cube measure
				cubeMeasure = SliceSPARQL.getSliceMeasure(cubeSliceURI,
						sliceGraph, cubeDSDGraph, SPARQL_service);
			}

			// Add top container if it is a cube not slice
			if (isCube) {

				// top container styling
				topcontainer.addStyle("border-style", "solid");
				topcontainer.addStyle("border-width", "1px");
				topcontainer.addStyle("padding", "20px");
				topcontainer.addStyle("border-radius", "5px");
				topcontainer.addStyle("border-color", "#C8C8C8 ");
				topcontainer.addStyle("display", "table-cell ");
				topcontainer.addStyle("vertical-align", "middle ");
				// topcontainer.addStyle("align", "center");
				topcontainer.addStyle("margin-left", "auto");
				topcontainer.addStyle("margin-right", "auto");

				
				// If an aggregation set has already been created
				if (aggregationSetDims.size() > 0) {
					
					FLabel OLAPbrowsing_label = new FLabel("OLAPbrowsing_label",
							"<b>Please select the dimensions of the cube to browse:<b>");
					topcontainer.add(OLAPbrowsing_label);


					// Show Aggregation set dimensions
					for (LDResource aggdim : aggregationSetDims) {

						// show one check box for each aggregation set dimension
						FCheckBox aggrDimCheckBox = new FCheckBox(
								"aggregation_" + aggdim.getURIorLabel(),
								aggdim.getURIorLabel()) {

							public void onClick() {
	

								// Get all selected aggregation set dimensions
								// for browsing
								List<LDResource> aggregationSetSelectedDims = new ArrayList<LDResource>();
								for (LDResource aggSetDimURI : mapDimURIcheckBox
										.keySet()) {
									FCheckBox check = mapDimURIcheckBox
											.get(aggSetDimURI);

									// Get selected dimensions
									if (check.checked) {
										aggregationSetSelectedDims
												.add(aggSetDimURI);
									}

								}

								// Identify the cube of the aggregation set that
								// contains exactly the dimension selected to be
								// browsed
								for (LDResource cube : cubeDimsOfAggregationSet
										.keySet()) {
									List<LDResource> cubeDims = cubeDimsOfAggregationSet
											.get(cube);
									if ((cubeDims.size() == aggregationSetSelectedDims
											.size())
											&& cubeDims
													.containsAll(aggregationSetSelectedDims)) {
										System.out.println("NEW CUBE URI: "
												+ cube.getURI());

										// The new cube to visualize
										cubeSliceURI = "<" + cube.getURI()
												+ ">";

										// clear the previous visualization and
										// create a new one for the new cube
										cnt.removeAll();
										topcontainer.removeAll();
										leftcontainer.removeAll();
										rightcontainer.removeAll();
										dimensionURIfcomponents.clear();

										// show the cube
										populateCentralContainer();

										break;
									}
								}

							}

						};

						// set as checked if the dimension is contained at the
						// selected cube
						aggrDimCheckBox.setChecked(cubeDimensions
								.contains(aggdim));
						mapDimURIcheckBox.put(aggdim, aggrDimCheckBox);

						topcontainer.add(aggrDimCheckBox);

					}
				} else {

					FLabel notOLAP = new FLabel("notOLAP",
							"<b>OLAP-like browsing is not supoorted for this cube<b>");
					topcontainer.add(notOLAP);
				}

				// Get values for each cube dimension
				allDimensionsValues = CubeHandlingUtils.getDimsValues(
						cubeDimensions, cubeSliceURI, cubeGraph, cubeDSDGraph,
						useCodeLists, SPARQL_service);
			} else if (isSlice) {
				// Get values for each slice dimension
				allDimensionsValues = CubeHandlingUtils.getDimsValuesFromSlice(
						cubeDimensions, cubeSliceURI, cubeGraph, cubeDSDGraph,
						sliceGraph, useCodeLists, SPARQL_service);
			}

			// Select the two dimension with the most values for visualization
			visualDimensions = CubeHandlingUtils.getRandomDims4Visualisation(
					cubeDimensions, allDimensionsValues);

			// Get the fixed cube dimensions
			fixedDimensions = CubeHandlingUtils.getFixedDimensions(
					cubeDimensions, visualDimensions);

			// Selected values for the fixed dimensions
			fixedDimensionsSelectedValues = CubeHandlingUtils
					.getFixedDimensionsRandomSelectedValues(
							allDimensionsValues, fixedDimensions);

			TupleQueryResult res = null;
			// Get query tuples for visualization
			if (isCube) {
				res = CubeBrowserSPARQL.get2DVisualsiationValues(visualDimensions,
						fixedDimensionsSelectedValues, cubeMeasure,
						allDimensionsValues, cubeSliceURI, cubeGraph,
						SPARQL_service);
			} else if (isSlice) {
				res = CubeBrowserSPARQL.get2DVisualsiationValuesFromSlice(
						visualDimensions, fixedDimensionsSelectedValues,
						cubeMeasure, allDimensionsValues, cubeSliceURI,
						sliceGraph, cubeGraph, SPARQL_service);
			}

			// Create an FTable model based on the query tuples
			FTableModel tm = create2DCubeTableModel(res, allDimensionsValues,
					visualDimensions);

			// Initialize FTable
			ftable.setShowCSVExport(true);
			ftable.setNumberOfRows(20);
			ftable.setEnableFilter(true);
			ftable.setOverFlowContainer(true);
			ftable.setFilterPos(FilterPos.TOP);
			ftable.setSortable(false);
			ftable.setModel(tm);

			// //////////Left container//////////////////

			// Show left container if there are 2 visual dimensions
			if (visualDimensions.size() == 2) {

				// left container styling
				leftcontainer.addStyle("border-style", "solid");
				leftcontainer.addStyle("border-width", "1px");
				leftcontainer.addStyle("padding", "20px");
				leftcontainer.addStyle("border-radius", "5px");
				leftcontainer.addStyle("border-color", "#C8C8C8 ");
				leftcontainer.addStyle("height", "200px ");
				leftcontainer.addStyle("width", "400px ");
				leftcontainer.addStyle("display", "table-cell ");
				leftcontainer.addStyle("vertical-align", "middle ");
				leftcontainer.addStyle("align", "center");

				FLabel fixeddimlabel = new FLabel("fixeddimlabel",
						"<b>Please select the two dimensions that define the table of the browser:<b>");
				leftcontainer.add(fixeddimlabel);

				// New line
				leftcontainer.add(getNewLineComponent(false));

				// Add label for Dim1 (column headings)
				FLabel dim1Label = new FLabel("dim1Label",
						"<b>Column Headings<b>");
				leftcontainer.add(dim1Label);

				// Add Combo box for Dim1
				final FComboBox dim1Combo = new FComboBox("dim1Combo") {
					@Override
					public void onChange() {

						// Get the URI of the 1st selected dimension
						List<String> d1Selected = this.getSelectedAsString();

						// Get the URI of the 2nd selected dimension
						List<String> d2Selected = null;
						for (FComponent fc : leftcontainer.getAllComponents()) {
							if (fc.getId().contains("_dim2Combo")) {
								d2Selected = ((FComboBox) fc)
										.getSelectedAsString();

								// If both combo boxes have the same selected
								// value
								// Select randomly another value for d2
								if (d1Selected.get(0).equals(d2Selected.get(0))) {
									List<Pair<String, Object>> d2choices = ((FComboBox) fc)
											.getChoices();
									for (Pair<String, Object> pair : d2choices) {
										if (!pair.snd.toString().equals(
												d2Selected.get(0))) {
											d2Selected.clear();
											d2Selected.add(pair.snd.toString());
											((FComboBox) fc)
													.setPreSelected(pair.snd
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

				// populate Dim1 combo box
				for (LDResource ldr : cubeDimensions) {
					dim1Combo.addChoice(ldr.getURIorLabel(), ldr.getURI());
				}

				dim1Combo.setPreSelected(visualDimensions.get(0).getURI());
				leftcontainer.add(dim1Combo);

				// NEW LINE
				leftcontainer.add(getNewLineComponent(false));

				// Add label for Dim2 (column headings)
				FLabel dim2Label = new FLabel("dim2Label",
						"<b>Rows (values in first column)</b>");
				leftcontainer.add(dim2Label);

				// Add Combo box for Dim2
				final FComboBox dim2Combo = new FComboBox("dim2Combo") {
					@Override
					public void onChange() {

						// Get the URI of the 2nd selected dimension
						List<String> d2Selected = this.getSelectedAsString();

						// Get the URI of the 1st selected dimension
						List<String> d1Selected = null;
						for (FComponent fc : leftcontainer.getAllComponents()) {
							if (fc.getId().contains("_dim1Combo")) {
								d1Selected = ((FComboBox) fc)
										.getSelectedAsString();

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
											((FComboBox) fc)
													.setPreSelected(pair.snd
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

				// populate Dim2 combo box
				for (LDResource ldr : cubeDimensions) {
					dim2Combo.addChoice(ldr.getURIorLabel(), ldr.getURI());
				}

				dim2Combo.setPreSelected(visualDimensions.get(1).getURI());
				leftcontainer.add(dim2Combo);

			}
			// //////////Right container//////////////////

			// Right container styling
			rightcontainer.addStyle("border-style", "solid");
			rightcontainer.addStyle("border-width", "1px");
			rightcontainer.addStyle("padding", "20px");
			rightcontainer.addStyle("border-radius", "5px");
			rightcontainer.addStyle("border-color", "#C8C8C8 ");
			rightcontainer.addStyle("height", "200px ");
			rightcontainer.addStyle("width", "400px ");
			rightcontainer.addStyle("display", "table-cell ");
			rightcontainer.addStyle("vertical-align", "middle ");
			rightcontainer.addStyle("align", "center");

			// If it is a slice and there are fixed dimension values
			if (!sliceFixedDimensionsValues.isEmpty()) {
				FLabel sliceFDim = new FLabel("sliceFDim",
						"<b>The slice fixed dimensions are:<b>");
				rightcontainer.add(sliceFDim);
				rightcontainer.add(getNewLineComponent(false));

				// An FGrid to show visual and fixed cube dimensions
				FGrid slicegrid = new FGrid("slicegrid");

				for (LDResource sliceFixedDim : sliceFixedDimensionsValues
						.keySet()) {

					ArrayList<FComponent> farray = new ArrayList<FComponent>();
					Random rnd = new Random();
					Long rndLong = Math.abs(rnd.nextLong());
					// Add the label for the fixed cube dimension
					FLabel fDimLabel = new FLabel(sliceFixedDim.getURIorLabel()
							+ "_" + rndLong + "_name", "<b>"
							+ sliceFixedDim.getURIorLabel() + ": </b>");

					farray.add(fDimLabel);

					// rightcontainer.add(fDimLabel);

					LDResource fDimValue = sliceFixedDimensionsValues
							.get(sliceFixedDim);
					FLabel fDimValueLabel = new FLabel(
							fDimValue.getURIorLabel() + "_" + rndLong
									+ "_value", fDimValue.getURIorLabel());

					farray.add(fDimValueLabel);

					slicegrid.addRow(farray);

					farray = new ArrayList<FComponent>();
					farray.add(getNewLineComponent(false));

					slicegrid.addRow(farray);

					// rightcontainer.add(fDimValueLabel);

					// rightcontainer.add(getNewLineComponent(false));

				}

				rightcontainer.add(slicegrid);

			}

			// If there are fixed dimensions
			if (fixedDimensions.size() > 0) {
				FLabel otheropts = new FLabel("Options",
						"<b>Please select the values of the fixed dimensions:<b>");
				rightcontainer.add(otheropts);

				// Add labels and combo boxed for the fixed cube dimensions
				addFixedDimensions(false);
			}

			// An FGrid to show visual and fixed cube dimensions
			FGrid fg = new FGrid("mygrid");

			// Styling of FGrid
			fg.addStyle("align", "center");
			fg.addStyle("margin", "auto");

			ArrayList<FComponent> farray = new ArrayList<FComponent>();

			// Add components to FGrid

			// Show visual dimensions panel if there are 2 visual dimension
			if (visualDimensions.size() == 2) {
				farray.add(leftcontainer);
			}

			// Show fixed dimensions panel if there are any
			if ((fixedDimensions.size() > 0)
					|| (!sliceFixedDimensionsValues.isEmpty())) {
				farray.add(rightcontainer);
			}

			fg.addRow(farray);

			// Add components to central container

			// Add top container with available dimensions to browse only
			// if a cube URi is provided
			if (isCube) {
				cnt.add(topcontainer);
			}

			cnt.add(ftable);
			cnt.add(getNewLineComponent(false));
			cnt.add(fg);

			// Show the create slice button if there are fixed dimensions
			// i.e. a
			// slice is not already visualized
			if (fixedDimensions.size() > 0) {
				FContainer bottomcontainer = new FContainer("bottomcontainer");

				// Bottom container styling
				bottomcontainer.addStyle("border-style", "solid");
				bottomcontainer.addStyle("border-width", "1px");
				bottomcontainer.addStyle("padding", "20px");
				bottomcontainer.addStyle("border-radius", "5px");
				bottomcontainer.addStyle("width", "400px ");
				bottomcontainer.addStyle("border-color", "#C8C8C8 ");
				bottomcontainer.addStyle("display", "table-cell ");
				bottomcontainer.addStyle("vertical-align", "middle ");
				bottomcontainer.addStyle("align", "center");

				FLabel bottomLabel = new FLabel(
						"bottomlabel",
						"<b>In case you want to store a two-dimensional"
								+ "	slice of the cube based on the data presented in"
								+ " the browser</b>");
				bottomLabel.addStyle("width", "400px ");

				bottomcontainer.add(bottomLabel);

				// Button to create slice
				FButton createSlice = new FButton("createSlice", "createSlice") {
					@Override
					public void onClick() {
						String sliceURI = SliceSPARQL.createCubeSlice(
								cubeSliceURI, cubeGraph,
								fixedDimensionsSelectedValues,
								cubeMeasure.get(0), sliceObservations);
						String message = "A new slice with the following URI has been created: "
								+ sliceURI;

						FDialog.showMessage(this.getPage(),
								"New Slice created", message, "ok");

					}
				};

				bottomcontainer.add(createSlice);
				cnt.add(bottomcontainer);
			}

			/*
			 * // Button to create aggregation set FButton createPermutations =
			 * new FButton("createPermutations", "createPermutations") {
			 * 
			 * @Override public void onClick() { OrderedPowerSet<LDResource> ops
			 * = new OrderedPowerSet<LDResource>( (ArrayList<LDResource>)
			 * cubeDimensions); String aggregationSetURI = AggregationSPARQL
			 * .createNewAggregationSet(cubeDSDGraph, SPARQL_service);
			 * AggregationSPARQL.attachCube2AggregationSet(aggregationSetURI,
			 * cubeDSDGraph, cubeSliceURI, SPARQL_service); for (int j = 1; j <
			 * cubeDimensions.size(); j++) { System.out.println("SIZE = " + j);
			 * List<LinkedHashSet<LDResource>> perms = ops
			 * .getPermutationsList(j); for (Set<LDResource> myset : perms) {
			 * String st = ""; for (LDResource l : myset) { st += l.getURI() +
			 * " "; } System.out.println(st);
			 * 
			 * String newCubeURI = AggregationSPARQL
			 * .createCubeForAggregationSet(myset, cubeMeasure, cubeSliceURI,
			 * cubeGraph, cubeDSDGraph, aggregationSetURI, SPARQL_service);
			 * System.out.println("NEW CUBE: " + newCubeURI); }
			 * System.out.println("----------");
			 * 
			 * } } }; cnt.add(createPermutations);
			 */
			// //////// Not a valid cube or Slice URI/////////////
		} else {

			String uri = cubeSliceURI.replaceAll("<", "");
			uri = uri.replaceAll(">", "");
			String message = "The URI <b>" + uri
					+ "</b> is not a valid cube or slice URI.";
			FLabel invalidURI_label = new FLabel("invalidURI", message);
			cnt.add(invalidURI_label);

		}

		// If this is not the first load of the widget
		if (!isFirstLoad) {
			cnt.populateView();
		}

	}

	// Set visual and fixed dimensions based on user selection of combo boxes
	private void setVisualDimensions(List<String> d1Selected,
			List<String> d2Selected) {
		// A tmp list to store the new dimensions for visualization
		List<LDResource> tmpvisualDimensions = new ArrayList<LDResource>();
		tmpvisualDimensions.add(null);
		tmpvisualDimensions.add(null);

		// The the visual dimensions from the combo boxes
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
		visualDimensions = tmpvisualDimensions;

		// remove previous combo boxes, labels and newlines
		for (LDResource fDim : fixedDimensions) {
			Collection<FComponent> allcomp = rightcontainer.getAllComponents();
			for (FComponent comp : allcomp) {
				if (comp.getId().contains(fDim.getURIorLabel() + "_label")
						|| comp.getId().contains(
								fDim.getURIorLabel() + "_combo")
						|| comp.getId().contains("fhtmlnewlinedelete_"))
					rightcontainer.removeAndRefresh(comp);
			}
		}

		// Tmp Fixed dimensions
		List<LDResource> tmpFixedDimensions = CubeHandlingUtils
				.getFixedDimensions(cubeDimensions, visualDimensions);

		// Update Global fixed dimensions
		fixedDimensions.clear();
		fixedDimensions = tmpFixedDimensions;

		// if there are fixed dimensions
		if (fixedDimensions.size() > 0) {

			// Tmp Selected values for the fixed dimensions
			HashMap<LDResource, LDResource> tmpFixedDimensionsSelectedValues = CubeHandlingUtils
					.getFixedDimensionsRandomSelectedValues(
							allDimensionsValues, fixedDimensions);

			// Update global selected values
			fixedDimensionsSelectedValues.clear();
			fixedDimensionsSelectedValues = tmpFixedDimensionsSelectedValues;

			// Add labels and combo boxed for the fixed cube dimensions
			addFixedDimensions(true);
		}
	}

	// Add labels and combo boxed for the fixed cube dimensions
	// Input: refresh, true: when refresh is needed i.e. not at the
	// initialization,
	// false when refresh is not need i.e. at the initialization
	private void addFixedDimensions(boolean refresh) {

		// NEW LINE
		if (refresh) {
			rightcontainer.addAndRefresh(getNewLineComponent(true));
		} else {
			rightcontainer.add(getNewLineComponent(true));
		}

		dimensionURIfcomponents.clear();
		// Add a Label - Combo box for each fixed cube dimension
		for (LDResource fDim : fixedDimensions) {

			List<FComponent> dimComponents = new ArrayList<FComponent>();

			// Add the label for the fixed cube dimension
			FLabel fDimLabel = new FLabel(fDim.getURIorLabel() + "_label",
					"<b>" + fDim.getURIorLabel() + "</b>");
			dimComponents.add(fDimLabel);

			if (refresh) {
				rightcontainer.addAndRefresh(fDimLabel);
			} else {
				rightcontainer.add(fDimLabel);
			}

			//IWB does not support too long IDs
			String comboID=fDim.getURIorLabel();
			if(comboID.length()>10){
				comboID=fDim.getURIorLabel().substring(0,10);
			}
			
			// Add the combo box for the fixed cube dimension
			FComboBox fDimCombo = new FComboBox(comboID + "_combo") {
				@Override
				public void onChange() {
					showCube();
				}
			};

			// Populate the combo box with the values of the fixed cube
			// dimension
			for (LDResource ldr : allDimensionsValues.get(fDim)) {
				fDimCombo.addChoice(ldr.getURIorLabel(), ldr.getURI());
			}

			// Combo box pre-selected value
			fDimCombo.setPreSelected(fixedDimensionsSelectedValues.get(fDim)
					.getURI());

			dimComponents.add(fDimCombo);

			if (refresh) {
				rightcontainer.addAndRefresh(fDimCombo);
			} else {
				rightcontainer.add(fDimCombo);
			}

			// NEW LINE
			if (refresh) {
				rightcontainer.addAndRefresh(getNewLineComponent(true));
			} else {
				rightcontainer.add(getNewLineComponent(true));
			}

			dimensionURIfcomponents.put(fDim, dimComponents);
		}

	}

	// Show the data cube
	private void showCube() {

		HashMap<LDResource, LDResource> tmpFixedDimensionsSelectedValues = new HashMap<LDResource, LDResource>();

		// Get the selected value for each fixed dimension
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

		TupleQueryResult res = null;
		if (isCube) {
			// Get query tuples for visualization
			res = CubeBrowserSPARQL.get2DVisualsiationValues(visualDimensions,
					fixedDimensionsSelectedValues, cubeMeasure,
					allDimensionsValues, cubeSliceURI, cubeGraph,
					SPARQL_service);
		} else if (isSlice) {
			res = CubeBrowserSPARQL.get2DVisualsiationValuesFromSlice(visualDimensions,
					fixedDimensionsSelectedValues, cubeMeasure,
					allDimensionsValues, cubeSliceURI, sliceGraph, cubeGraph,
					SPARQL_service);
		}

		// create table model for visualization
		FTableModel newTableModel = create2DCubeTableModel(res,
				allDimensionsValues, visualDimensions);
		ftable.setModel(newTableModel);
		ftable.populateView();

	}

	//Create a table model from the Tuple query result in order to visualize
	private FTableModel create2DCubeTableModel(
			TupleQueryResult res,
			HashMap<LDResource, List<LDResource>> dimensions4VisualisationValues,
			List<LDResource> visualDimensions) {

		// Get all values for 1st visual dimension
		List<LDResource> dim1 = dimensions4VisualisationValues
				.get(visualDimensions.get(0));

		
		List<LDResource> dim2 = null;

		// If there are 2 visual dimensions
		// Get all values for 2nd visual dimension
		if (visualDimensions.size() == 2) {

			dim2 = dimensions4VisualisationValues.get(visualDimensions.get(1));
		}

		LDResource[][] v2DCube = null;

		//Set the table dimensions
		// If there are 2 visual dimensions
		if (visualDimensions.size() == 2) {
			v2DCube = new LDResource[dim2.size()][dim1.size()];

		// One visual dimension
		} else {
			v2DCube = new LDResource[dim1.size()][1];
		}

		FTableModel tm = new FTableModel();

		// Add all visual observations to a list - to create a slice later on
		sliceObservations.clear();

		try {

			///// Set the first row of FTableModel////

			// If there are 2 visual dimensions
			if (visualDimensions.size() == 2) {

				// The first column of 1st row - the name of the 2nd dimension
				tm.addColumn(visualDimensions.get(1).getURIorLabel());

				// The rest columns of the first row - the values of the 1st
				// dimension
				for (LDResource dim1Val : dim1) {
					tm.addColumn(dim1Val.getURIorLabel());
				}

			// If there is 1 visual dimensions
			} else {
				tm.addColumn(visualDimensions.get(0).getURIorLabel());
				tm.addColumn("Measure");
			}

			// for each cube observation
			while (res.hasNext()) {

				BindingSet bindingSet = res.next();

				// get Dim1 value
				String dim1Val = bindingSet.getValue("dim1").stringValue();
				LDResource r1 = new LDResource(dim1Val);

				// get measure
				String measure = bindingSet.getValue("measure").stringValue();

				// get observation URI
				String obsURI = bindingSet.getValue("obs").stringValue();
				LDResource obs = new LDResource(obsURI);
				obs.setLabel(measure);

				// If there are 2 visual dimensions
				if (visualDimensions.size() == 2) {
					// get Dim2 value
					String dim2Val = bindingSet.getValue("dim2").stringValue();
					LDResource r2 = new LDResource(dim2Val);

					// Add the observation to the corresponding (row, column)
					// position of the table
					v2DCube[dim2.indexOf(r2)][dim1.indexOf(r1)] = obs;
				} else {

					v2DCube[dim1.indexOf(r1)][0] = obs;
				}

				// add observation to potential slice
				sliceObservations.add(obs);
			}

			// If there are 2 visual dimensions
			if (visualDimensions.size() == 2) {
				// populate the FTableModel based on the v2DCube created
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

				// If there is 1 visual dimensions
			} else {

				// Add observations
				for (int j = 0; j < dim1.size(); j++) {
					Object[] data = new Object[dim1.size()];
					// Add row header (values in first column)
					data[0] = getHTMLStringFromLDResource(dim1.get(j));

					data[1] = getHTMLStringFromLDResource(v2DCube[j][0]);
					tm.addRow(data);
				}

			}

		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		}
		return tm;
	}

	// get an HTML representation of an LDResource
	private HtmlString getHTMLStringFromLDResource(LDResource ldr) {
		if (ldr == null) {
			return new HtmlString("-");
		}
		String linktext = ldr.getURIorLabel();
		String linkURI = ldr.getURI();
		String html = linktext;
		// String html = "<a href=\"" + linkURI + "\">" + linktext + "</a>";

		HtmlString html_string = new HtmlString(html);
		// HtmlString html_string = new HtmlString(linkURI);
		return html_string;

	}

	// Adds a new line to UI
	private FHTML getNewLineComponent(boolean delete) {
		Random rand = new Random();
		FHTML fhtml = null;
		if (delete) {
			fhtml = new FHTML("fhtmlnewlinedelete_" + Math.abs(rand.nextLong()));
		} else {
			fhtml = new FHTML("fhtmlnewline_" + Math.abs(rand.nextLong()));
		}
		fhtml.setValue("<br><br>");
		return fhtml;
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
