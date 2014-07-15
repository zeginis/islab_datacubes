package org.certh.opencube.aggregation;

import com.fluidops.ajax.components.FComponent;
import com.fluidops.iwb.model.ParameterConfigDoc;
import com.fluidops.iwb.widget.AbstractWidget;
import com.fluidops.iwb.widget.config.WidgetBaseConfig;

/**
 * On some wiki page add
 * 
 * <code>
 * = Test my demo widget =
 * 
 * <br/>
 * {{#widget: org.certh.opencube.aggregation.AggregationSetCreator
 *  }}
 * 
 * </code>
 * 
 */
public class AggregationSetCreator extends AbstractWidget<AggregationSetCreator.Config> {

	

	public static class Config extends WidgetBaseConfig {
		
		@ParameterConfigDoc(desc = "SPARQL service to forward queries", required = false)
		public String sparqlService;

	}

	@Override
	protected FComponent getComponent(String id) {
return null;
		
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