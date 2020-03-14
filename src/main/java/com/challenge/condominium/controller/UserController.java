package com.challenge.condominium.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.challenge.condominium.model.Condominium;
import com.challenge.condominium.model.Group;
import com.challenge.condominium.model.User;
import com.challenge.condominium.service.UserService;
import com.challenge.condominium.model.Access.Permission;
import com.challenge.condominium.model.Group.Type;

@RestController
public class UserController {
	@Autowired
	private UserService userService;  
	
	@PostMapping("/user/{email}/permission/find") 
	@SuppressWarnings("unchecked")
	public ResponseEntity<?> findHigherUserPermissions(@PathVariable String email, @RequestBody Map<String,Object> DBmap){ 
		try {	
			StringBuilder stringBuilder = new StringBuilder();
			
			//Get database
			List<User> users =  userService.getUsersOfMap((List<Map<String, Object>>) DBmap.get("users")); 
			List<Integer> condominiumsId =  (List<Integer>) DBmap.get("condominiumsId");
			
			//Find user
			User user = userService.findUser(users, email); 
			if(user == null) {
				return new ResponseEntity<String>("Usuário não encontrado",HttpStatus.BAD_REQUEST); 
			} 
			
			List<Map<String, Object>> accessesMap = 
					userService.separateAccessesInCondominiums(user.getGroups(), condominiumsId);
			accessesMap.forEach(map -> {
				// Separate accesses in permissions
				List<Permission> reservationsPermissions = new ArrayList<Permission>();
				List<Permission> deliveryPermissions = new ArrayList<Permission>();
				List<Permission> usersPermissions = new ArrayList<Permission>();
				userService.separateAccessesInPermisions(map, reservationsPermissions, deliveryPermissions, usersPermissions);
				
				String out = userService.buildOutput(map.get("condominiumId").toString(), 
						    reservationsPermissions, deliveryPermissions, usersPermissions);
				stringBuilder.append(out);
				stringBuilder.append("\n");

			}); 
			
			Map<String,String> result = new HashMap<String, String>();
			result.put("result", stringBuilder.toString());
			
			return new ResponseEntity<>(result,HttpStatus.OK); 
		}catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST); 
		}
	} 
	
	@PostMapping("/database/read")
	public ResponseEntity<?> readDatabase(@RequestBody byte[] fileBytes){
		try {
			List<Group> groups = new ArrayList<Group>();
			List<User> users = new ArrayList<User>();
			List<Long> condominiumsId =  new ArrayList<Long>();
			  
			File newFile = userService.createFile(fileBytes);
			BufferedReader reader = new BufferedReader(new FileReader(newFile.getPath())); 
			String line = "";
			
			while ((line = reader.readLine()) != null){
				line = line.trim();
				String[] fields = line.split("\\;");
				if(line.contains("Grupo")) {		    	
		    		Condominium condominium = new Condominium(); 
	    			condominium.setId(Long.parseLong(fields[2]));
	    			if(!condominiumsId.contains(condominium.getId())){
	    				condominiumsId.add(condominium.getId()); 
	    			}
	    			Group group =  new Group(); 
					group.setCondominium(condominium);
					group.setType(Type.fromString(fields[1]));
					group.setAccesses(userService.getLineAccesses(fields[3]));
					groups.add(group); 
					
				}else if(line.contains("Usuario")) {		
			    	User user =  new User();
					user.setEmail(fields[1]);
					user.setGroups(userService.getLineGroups(fields[2]));
					users.add(user);
				}
		    }
		    reader.close();
		    
		    //Set accesses of users
		    userService.setUserAccesses(users, groups);
		    
		    Map<String,Object> DBmap  = new HashMap<String, Object>();
		    DBmap.put("users",users);
		    DBmap.put("condominiumsId",condominiumsId);   

			return new ResponseEntity<>(DBmap,HttpStatus.OK); 
		}catch (FileNotFoundException e) { 
			return new ResponseEntity<>("Arquivo não existe", HttpStatus.BAD_REQUEST); 
		}catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(e, HttpStatus.BAD_REQUEST); 
		}
	}
}
