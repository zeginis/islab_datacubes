package org.certh.opencube.mapview;

import java.util.List;

import com.fluidops.ajax.FClientUpdate;
import com.fluidops.ajax.FClientUpdate.Prio;
import com.fluidops.ajax.components.FButton;
import com.fluidops.ajax.components.FCheckBox;
import com.fluidops.ajax.components.FComponent;
import com.fluidops.ajax.components.FContainer;
import com.fluidops.ajax.components.FHTML;
import com.fluidops.ajax.components.FLabel;
import com.fluidops.ajax.components.FTextInput2;
import com.fluidops.iwb.api.EndpointImpl;
import com.fluidops.iwb.model.ParameterConfigDoc;
import com.fluidops.iwb.widget.AbstractWidget;
import com.google.common.collect.Lists;



/**
 * On some wiki page add
 * 
 * <code>
 * = Test my demo widget =
 * 
 * <br/>
 * {{#widget: com.fluidops.iwb.widget.MyDemoWidget 
 * | labelText = 'Enter your name'
 * }}
 * 
 * </code>
 * 
 */
public class MapView extends AbstractWidget<MapView.Config> {
	
	public static class Config {
		@ParameterConfigDoc(desc = "The types for which the wizard generates data")
		public String labelText;
	}
	
	  @Override
	    public List <String> jsURLs()
	    {
	        String cp = EndpointImpl.api().getRequestMapper().getContextPath();
	        return Lists.newArrayList(cp+"http://cdn.leafletjs.com/leaflet-0.7.3/leaflet.js", cp+"http://www.mapquestapi.com/sdk/leaflet/v1.0/mq-map.js?key=Fmjtd%7Cluur2d0znu%2Cbl%3Do5-9a82dw", cp+"http://www.mapquestapi.com/sdk/leaflet/v1.0/mq-geocoding.js?key=Fmjtd%7Cluur2d0znu%2Cbl%3Do5-9a82dw");
	    
	    }    
	
	   
	@Override
	protected FComponent getComponent(String id) {

		Config config = get();
			
		config.labelText = config.labelText==null ? "Enter some text:" : config.labelText;
		
		// the layouting container for this widget
		// the container must use the provided id
		FContainer cnt = new FContainer(id);

		  FButton mapTest2 = new FButton("map_Test2", "Geocoding3") {
				@Override
				public void onClick() {
					addClientUpdate(new FClientUpdate(Prio.VERYBEGINNING, "var map = L.map('map', { layers: MQ.mapLayer()}); MQ.geocode({ map: map }) .search('Maria Zotou');"));
				}
	      };
		      cnt.add(mapTest2);
		      
		      FButton mapTest3 = new FButton("map_Test3", "Multiple Geocoding2") {
					@Override
					public void onClick() {
						addClientUpdate(new FClientUpdate(Prio.VERYBEGINNING, "MQ.geocode().search(['England','sdfdf','Thessaloniki' ]) .on('success', function(e) {var results = e.result, html = '', group = [], features, marker, result, latlng, prop, best, val, map, r, i; map = L.map('map', { layers: MQ.mapLayer() }); for (i = 0; i < results.length; i++) { result = results[i].best;  if(result.geocodeQualityCode!= \"A1XAX\") {latlng = result.latlng; html += '<div style=\"width:300px; float:left;\">'; html += '<p><strong>Geocoded Location #' + (i + 1) + '</strong></p>'; for (prop in result) { r = result[prop]; if (prop === 'displayLatLng') { val = r.lat + ', ' + r.lng; } else if (prop === 'mapUrl') { val = '<br /><img src=\"' + r + '\" />'; } else { val = r; } html += prop + ' : ' + val + '<br />';} html += '</div>'; marker = L.marker([ latlng.lat, latlng.lng]) .bindPopup(result.adminArea5 + ', ' + result.adminArea3);  group.push(marker); } } features = L.featureGroup(group).addTo(map); map.fitBounds(features.getBounds()); L.DomUtil.get('info').innerHTML = html; });"));
					}
		      };
			      cnt.add(mapTest3);
			      
			    			     
			     
		// now we can add other components to the container
		// the simplest is to add them line by line
		
		// 1) add a label with the labelText
		final FLabel label = new FLabel("label", config.labelText);
		cnt.add(label);	
	
		// 2) add a text field 
		final FTextInput2 input = new FTextInput2("inp");
		cnt.add(input);
		

		cnt.cssURLs();
				
	FHTML map = new FHTML("html");
    String maptext = "<div>New map2</div><div>Hello</div>";
    maptext += "<p id='demo'>My first paragraph.</p>";
    maptext +="<p id=\"demo2\"></p>";
  // maptext +="<div id=\"map\" style=\"width: 600px; height: 400px\"></div>";
    maptext +="<div id=\"map\" style=\"width:600px; height:400px;\"></div>";
    
          map.setValue(maptext);
   cnt.add(map);
    
   // FClientUpdate update = new FClientUpdate(Prio.VERYEND, "document.getElementById(\"demo\").innerHTML = \"Hello dolly.\"");
     
    FButton btnTest = new FButton("btn_Test", "input") {
		@Override
		public void onClick() {
			addClientUpdate(new FClientUpdate("", "document.getElementById(\"demo\").innerHTML = \"Hello dolly.\""));				
		}
	};

      cnt.add(btnTest);
			// 3) add two buttons next two each other
		// a) alert content of text field
		FButton btnOk = new FButton("btn_OK", "Alert input") {
			@Override
			public void onClick() {
				addClientUpdate(new FClientUpdate("alert('" + input.getValue() + "');"));				
			}
		};
		
		FButton btnTest2 = new FButton("btn_Test2", "input") {
			@Override
			public void onClick() {
				addClientUpdate(new FClientUpdate(Prio.BEGINNING, "var x = 5; var y=6; document.getElementById(\"demo2\").innerHTML = x + y;"));				
			}
		};

	      cnt.add(btnTest2);
	 
	      
	    //  FButton mapTest = new FButton("map_Test", "Map4") {
		//		@Override
		//		public void onClick() {
		//			addClientUpdate(new FClientUpdate(Prio.MIDDLE, "var map = L.map('map').setView([51.5, -0.09], 13); L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', { attribution: 'Map data &copy; <a href=\"http://openstreetmap.org\">OpenStreetMap</a> contributors'}).addTo(map); var LeafIcon = L.Icon.extend({ options: { shadowUrl: 'http://leafletjs.com/docs/images/leaf-shadow.png', iconSize:     [38, 95], shadowSize:   [50, 64], iconAnchor:   [22, 94], shadowAnchor: [4, 62], popupAnchor:  [-3, -76]}}); var greenIcon = new LeafIcon({iconUrl: 'http://leafletjs.com/docs/images/leaf-green.png'}), redIcon = new LeafIcon({iconUrl: 'http://leafletjs.com/docs/images/leaf-red.png'}), orangeIcon = new LeafIcon({iconUrl: 'http://leafletjs.com/docs/images/leaf-orange.png'});L.marker([51.5, -0.09], {icon: greenIcon}).bindPopup(\"I am a green leaf.\").addTo(map); L.marker([51.495, -0.083], {icon: redIcon}).bindPopup(\"I am a red leaf.\").addTo(map); L.marker([51.49, -0.1], {icon: orangeIcon}).bindPopup(\"I am an orange leaf.\").addTo(map);"));
			//	}
	   //   };
		 //     cnt.add(mapTest);
		      
		    
		
	//	FClientUpdate client = new FClientUpdate(Prio.BEGINNING, "var x = 5; var y=6; document.getElementById(\"demo2\").innerHTML = x + y;");
		// cnt.addClientUpdate(client);
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