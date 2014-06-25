package org.certh.opencube.utils;

public class LDResource implements Comparable {
	
	private String URI;
	private String label;
	
	public LDResource() {
		super();
	}

	public LDResource(String uRI) {
		URI = uRI;
	}
	
	public LDResource(String uRI,String label) {
		URI = uRI;
		if(label!=null){
			this.label=label;
		}
	}



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

	// If label exists return the label
	// else return the last part of the URI (either after last '#' or after last
	// '/')
	public String getURIorLabel() {

		if (label != null) {
			return label;
		} else if (URI.contains("#")) {
			return URI.substring(URI.lastIndexOf("#") + 1, URI.length());
		} else {
			return URI.substring(URI.lastIndexOf("/") + 1, URI.length());
		}

	}

	@Override
	public boolean equals(Object obj) {
		// if the two objects are equal in reference, they are equal
		if (this == obj) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (obj instanceof LDResource) {
			LDResource cust = (LDResource) obj;
			if (cust.getURI() != null && cust.getURI().equals(URI)) {
				return true;
			}
		}
	
		return false;
	}
	
	public int hashCode(){
		return URI.hashCode();
		
	}
	

	@Override
	public int compareTo(Object otherResource) {
		
		return  this.getURIorLabel().compareTo(((LDResource)otherResource).getURIorLabel());
	}
	
	

}
