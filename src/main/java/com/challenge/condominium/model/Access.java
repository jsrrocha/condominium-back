package com.challenge.condominium.model;

import java.util.Arrays;
import java.util.List;

public class Access {
	private Functionality functionality; 
	private Permission permission; 
	
	public Functionality getFunctionality() {
		return functionality;
	}

	public void setFunctionality(Functionality functionality) {
		this.functionality = functionality;
	}

	public Permission getPermission() {
		return permission;
	}

	public void setPermission(Permission permission) {
		this.permission = permission;
	}

	public enum Functionality {
		RESERVATION("Reservas"), 
		DELIVERY("Entregas"), 
		USERS("Usuarios");
		
		private String name;

		private Functionality(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}	
		
		public static Functionality fromString(String name) throws IllegalArgumentException {
	    	
			List<Functionality> values = Arrays.asList(Functionality.values());  
			return values.stream()
	                .filter(value -> value.name.equals(name))
	                .findFirst()
	                .orElseThrow(() -> new IllegalArgumentException("Valor não existe: " + name));
	    }
	}
	
	public enum Permission { 
		WRITE("Escrita"),   
		READ("Leitura"), 
		NONE("Nenhuma");

		private String name;

		private Permission(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
		
		public static Permission fromString(String name) throws IllegalArgumentException {
	    	
			List<Permission> values = Arrays.asList(Permission.values());  
			return values.stream()
	                .filter(value -> value.name.equals(name))
	                .findFirst()
	                .orElseThrow(() -> new IllegalArgumentException("Valor não existe: " + name));
	    }
	}
}
