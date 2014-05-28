package org.certh.opencube.utils;

public class LDResource {
	private String URI;
	private String label;
	public String getURI() {
		return URI;
	}
	public void setURI(String uRI) {
		URI = uRI;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
	 public boolean equals(Object obj) {
	        // if the two objects are equal in reference, they are equal
	        if (this == obj) {
	            return true;
	        } else if (obj == null) {
	            return false;
	        } else if (obj instanceof LDResource) {
	        	LDResource cust = (LDResource) obj;
	            if (cust.getURI()!=null && cust.getURI().equals(URI) ) {
	                return true;
	            }
	        }
	        return false;
	    }
	

}
