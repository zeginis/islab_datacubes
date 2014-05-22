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
 * | labelText = 'Enter your name'
 * }} 
 * 
 * </code>
 * 
 */
public class DataCubeBrowser extends AbstractWidget<DataCubeBrowser.Config> {

	public static class Config {
		@ParameterConfigDoc(desc = "The types for which the wizard generates data")
		public String labelText;		
	}

	@Override
	protected FComponent getComponent(String id) {

		Config config = get();

		config.labelText = config.labelText == null ? "Enter some text:"
				: config.labelText;

		// the layouting container for this widget
		// the container must use the provided id
		FContainer cnt = new FContainer(id);

		// now we can add other components to the container
		// the simplest is to add them line by line

		// 1) add a label with the labelText
		final FLabel label = new FLabel("label", config.labelText);
		cnt.add(label);

		// 2) add a text field
		final FTextInput2 input = new FTextInput2("inp");
		cnt.add(input);

		// final FTextArea area=new FTextArea("tarea");
		// cnt.add(area);

		final FHTML html = new FHTML("htmlarea");

		final FTable ftable = new FTable("ftable");
		FTableModel tm = new FTableModel();
		FTableModel tm2 = new FTableModel();

		ReadDataManager dm = EndpointImpl.api().getDataManager();

		QueryBuilder<TupleQuery> queryBuilder = QueryBuilder
				.createTupleQuery("select distinct ?x ?y where"
						+ " {<http://www.w3.org/TR/2003/PR-owl-guide-20031209/wine#WhiteLoire>"
						+ " ?x ?y}");

		TupleQuery query;
		String result = "<table>";

		try {
			query = queryBuilder.build(dm);
			TupleQueryResult res = query.evaluate();
			List<String> bindingNames = res.getBindingNames();

			for (String name : bindingNames) {
				tm.addColumn(name);
			}

			ftable.setModel(tm);
			ftable.setShowCSVExport(true);
			ftable.setNumberOfRows(20);
			ftable.setEnableFilter(true);
			ftable.setOverFlowContainer(true);
			ftable.setFilterPos(FilterPos.TOP);
			ftable.setSortable(true);

			while (res.hasNext()) {
				BindingSet bindingSet = res.next();

				int size2 = bindingSet.size();
				result += "<tr>";

				String[] data = new String[3];

				for (int i = 0; i < size2; i++) {
					result += "<td>"
							+ bindingSet.getValue(bindingNames.get(i))
									.stringValue() + "</td>";

					data[i] = bindingSet.getValue(bindingNames.get(i))
							.stringValue();

				}
				tm.addRow(data);

				result += "</tr>";
			}
			

		} catch (MalformedQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (QueryEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		result += "</table>";
		html.setValue(result);
		ftable.setModel(tm);
		cnt.add(ftable);
		cnt.add(html);
		// 3) add two buttons next two each other
		// a) alert content of text field
		FButton btnOk = new FButton("btn_OK", "Alert input") {
			@Override
			public void onClick() {

				// addClientUpdate(new FClientUpdate("alert('" + result +
				// "');"));
			}

		};
		btnOk.appendClazz("floatLeft");
		cnt.add(btnOk);
		// b) cancel button to clear text input
		FButton btnCancel = new FButton("btn_Cancel", "Clear input") {
			@Override
			public void onClick() {
				input.setValueAndRefresh("");
			}
		};
		cnt.add(btnCancel);

		// 4) button to trigger API call
		FButton triggerApi = new FButton("btn_Api", "Trigger Api") {
			@Override
			public void onClick() {
				// DemoApi.doSomething();
			}
		};
		triggerApi.addStyle("margin-left", "50px");
		cnt.add(triggerApi);

		return cnt;
	}

	@Override
	public String getTitle() {
		return "My first widget";
	}

	@Override
	public Class<?> getConfigClass() {
		return Config.class;
	}
}