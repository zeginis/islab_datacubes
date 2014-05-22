package org.certh.opencube.cubebrowser;

import java.util.ArrayList;
import java.util.List;

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

		
		String query = "select distinct ?x ?y where"
				+ "{<http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#WhiteLoire> ?x ?y}";
		
		query="select  distinct ?dim where {"+config.dataCubeURI+" <http://purl.org/linked-data/cube#structure> " +
				" ?dsd.?dsd <http://purl.org/linked-data/cube#component>  ?cs." +
				"?cs <http://purl.org/linked-data/cube#dimension>  ?dim.}";
		
		//GET CUBE DIMENSIONS THEN SELECT TWO AND SHOW OBSERVATIONS

		//Execute select query and get tuples
		TupleQueryResult res = IWBquery.executeSelect(query);

		//create the table model from result to visualize
		FTableModel tm = createTableModel(res);

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

	private FTableModel createTableModel(TupleQueryResult res) {

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