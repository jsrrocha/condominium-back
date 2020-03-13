package com.challenge.condominium.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;


public class Group implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private Type type; 
	private Condominium condominium;
	private List<Access> accesses;
	
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	public Condominium getCondominium() {
		return condominium;
	}
	public void setCondominium(Condominium condominium) {
		this.condominium = condominium;
	}
	public List<Access> getAccesses() {
		return accesses;
	}
	public void setAccesses(List<Access> accesses) {
		this.accesses = accesses;
	}


	public enum Type {
		RESIDENT("Morador"),  
		MANAGER("Sindico"), 
		DOORMAN("Porteiro");

		private String name;

		private Type(String name) {
			this.name = name; 
		}

		public String getName() {
			return name;
		}	
		
		public static Type fromString(String name) throws IllegalArgumentException {
	    	
			List<Type> values = Arrays.asList(Type.values());  
			return values.stream()
	                .filter(value -> value.name.equals(name))
	                .findFirst()
	                .orElseThrow(() -> new IllegalArgumentException("Valor não existe: " + name));
	    }
	}
}
