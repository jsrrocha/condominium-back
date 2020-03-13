package com.challenge.condominium.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.challenge.condominium.model.Access; 
import com.challenge.condominium.model.Condominium;
import com.challenge.condominium.model.Group;
import com.challenge.condominium.model.User;
import com.challenge.condominium.model.Access.Functionality;
import com.challenge.condominium.model.Access.Permission;
import com.challenge.condominium.model.Group.Type;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class UserService { 
	
	public List<User> getUsersOfMap(List<Map<String,Object>> usersMap){
		List<User> users = new ArrayList<User>();
		usersMap.forEach(map-> {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
			User user = mapper.convertValue(map, User.class); 
			users.add(user);
		});
		
		return users;
	}
	
	public User findUser(List<User> users, String email) {
		Optional<User> optionalUser = users.stream()
				.filter(user -> user.getEmail().equals(email)).findFirst();
		if(!optionalUser.isPresent()) {
			return null; 
		}  
		return optionalUser.get(); 
	}
	
	@SuppressWarnings("unchecked")
	public void separateAccessesInPermisions(Map<String, Object> map, List<Permission> reservationsPermissions ,
				List<Permission> deliveryPermissions,
				List<Permission> usersPermissions) {
		
		List<Access> accesses = (List<Access>) map.get("accesses");
		accesses.forEach(access -> {
			switch (access.getFunctionality()) {
			case RESERVATION:
				reservationsPermissions.add(access.getPermission());
				break;
			case DELIVERY:
				deliveryPermissions.add(access.getPermission());
				break;
			case USERS:
				usersPermissions.add(access.getPermission());
				break;
			default:
				break; 
			}
		});
	}
	public List<Map<String, Object>> separateAccessesInCondominiums(List<Group> userGroups, List<Integer> condominiumsId) {
		List<Map<String, Object>> accessesMap = new ArrayList<Map<String, Object>>();
		
		condominiumsId.forEach(id -> {
			List<Group> groupsByCondominium = findGroupsByCondominium(userGroups,id);

			if (!groupsByCondominium.isEmpty()) {
				List<Access> accesses = findAccessesByGroups(groupsByCondominium);

				Map<String, Object> map = new HashMap<String, Object>();
				map.put("accesses", accesses);
				map.put("condominiumId", id);
				accessesMap.add(map); 
			}
		});
		return accessesMap;
	}
	
	public String getHigherPermission(List<Permission> permissions) {
		boolean haveWrite = permissions.stream().filter(permission -> 
				permission.equals(Permission.WRITE)).count() > 0;
		if (haveWrite) {
			return Permission.WRITE.getName();
		} else {
			boolean haveRead = permissions.stream().filter(permission -> 
				permission.equals(Permission.READ)).count() > 0;
			if (haveRead) {	
				return Permission.READ.getName();
			}else {
				return Permission.NONE.getName();
			}
		}	
	} 
	
	public List<Group> findGroupsByCondominium(List<Group> groups,Integer condominiumId){
		List<Group> groupList = groups.stream()
				.filter(groupInFilter ->
					groupInFilter.getCondominium().getId().equals(condominiumId.longValue()))
				.collect(Collectors.toList());
		return groupList;
	}
	
	public List<Access> findAccessesByGroups(List<Group> groups){
		List<Access> accesses = new ArrayList<Access>();
		
		groups.stream().forEach(group -> {
			accesses.addAll(group.getAccesses());
		});
		return accesses;
	}	
	
	public String buildOutput(String condominiumId, 
							  List<Permission> reservationsPermissions ,
			                  List<Permission> deliveryPermissions,
			                  List<Permission> usersPermissions) { 
		
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(condominiumId);stringBuilder.append(";");
		stringBuilder.append("[(");
		stringBuilder.append(Functionality.RESERVATION.getName());
		stringBuilder.append(",");
		stringBuilder.append(getHigherPermission(reservationsPermissions));
		stringBuilder.append(")");stringBuilder.append(",");
		stringBuilder.append("(");
		stringBuilder.append(Functionality.DELIVERY.getName());
		stringBuilder.append(",");
		stringBuilder.append(getHigherPermission(deliveryPermissions));
		stringBuilder.append(")");
		stringBuilder.append(",");
		stringBuilder.append("(");
		stringBuilder.append(Functionality.USERS.getName());
		stringBuilder.append(",");
		stringBuilder.append(getHigherPermission(usersPermissions));
		stringBuilder.append(")]");
		return stringBuilder.toString();
	} 
	
	
	public List<Access> getLineAccesses(String field){
		List<Access> accesses = new ArrayList<Access>();
		List<String> substringsList = getsubstringsListWithoutSomeCharacters(field);
		substringsList.forEach(sub ->{ 
			String[] accessFields = sub.split(",");
			
			Access access = new Access();
			Functionality functionality = Functionality.fromString(accessFields[0]);
			access.setFunctionality(functionality);
			Permission permission = Permission.fromString(accessFields[1]);
			access.setPermission(permission);
			accesses.add(access);
		}); 
		return accesses;
	}
	
	public List<Group> getLineGroups(String field){
		List<Group> userGroups = new ArrayList<Group>();
		List<String> substringsList = getsubstringsListWithoutSomeCharacters(field);
		substringsList.forEach(sub -> {
			String[] groupFields = sub.split(",");

			Condominium condominium = new Condominium();
			condominium.setId(Long.parseLong(groupFields[1]));
			Type type = Type.fromString(groupFields[0]);

			Group userGroup = new Group();
			userGroup.setCondominium(condominium);
			userGroup.setType(type);
			userGroups.add(userGroup);
		});
		return userGroups; 
	}
	
	public List<String> getsubstringsListWithoutSomeCharacters(String field){
		field = field.replace("[(", "");
		field = field.replace(")]", "");
		String[] substrings = field.split("\\).\\(");
		return Arrays.asList(substrings); 
	}
	
	public void setUserAccesses(List<User> users, List<Group> groups) {
	    users.forEach(user->{
	    	List<Group> userGroups = user.getGroups();
			userGroups.forEach(userGroup ->{
				Group group = findGroupByTypeAndCondominium(groups, userGroup.getType(), userGroup.getCondominium().getId());
				if(group !=null) {
					userGroup.setAccesses(group.getAccesses());
				}
			});	 
	    });    
	}
	
	public Group findGroupByTypeAndCondominium(List<Group> groups, Type type, Long condominiumId){
		Group group  = null;
		Optional<Group> Optionalgroup  = 
				groups.stream()
				.filter(groupInFilter -> 
						groupInFilter.getType().equals(type)
						&& groupInFilter.getCondominium().getId().equals(condominiumId))
				.findFirst(); 
		
		if(Optionalgroup.isPresent()) {
			group = Optionalgroup.get();
		}  
		return group;
	}
}
