package com.mossle.bpm.web.bpm;


import java.util.List;


import com.mossle.core.struts2.BaseAction;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;

import org.springframework.jdbc.core.JdbcTemplate;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.Group;
import org.activiti.engine.ProcessEngine;

@Results({
	@Result(name = IdentityAction.RELOAD_USER, location = "identity!listUsers.do?operationMode=RETRIEVE", type = "redirect"),
	@Result(name = IdentityAction.RELOAD_GROUP, location = "identity!listGroups.do?operationMode=RETRIEVE", type = "redirect")
})
public class IdentityAction extends BaseAction {
    public static final String RELOAD_USER = "reload-user";
    public static final String RELOAD_GROUP = "reload-group";
    private ProcessEngine processEngine;
	private List<User> users;
	private User user;
	private String userId;
	private String firstName;
	private String lastName;
	private String email;
	private List<Group> groups;
	private Group group;
	private String groupId;
	private String name;
	private String type;

	public String listUsers() {
		users = processEngine.getIdentityService().createUserQuery().list();
		return "listUsers";
	}

	public String inputUser() {
		if (userId != null) {
			user = processEngine.getIdentityService().createUserQuery().userId(userId).singleResult();
		}
		return "inputUser";
	}

	public String saveUser() {
		user = processEngine.getIdentityService().createUserQuery().userId(userId).singleResult();
		if (user == null) {
			user = processEngine.getIdentityService().newUser(userId);
		}
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setEmail(email);
		processEngine.getIdentityService().saveUser(user);
		return RELOAD_USER;
	}

	public String removeUser() {
		processEngine.getIdentityService().deleteUser(userId);
		return RELOAD_USER;
	}

	public String listGroups() {
		groups = processEngine.getIdentityService().createGroupQuery().list();
		return "listGroups";
	}

	public String inputGroup() {
		if (groupId != null) {
			group = processEngine.getIdentityService().createGroupQuery().groupId(groupId).singleResult();
		}
		return "inputGroup";
	}

	public String saveGroup() {
		group = processEngine.getIdentityService().createGroupQuery().groupId(groupId).singleResult();
		if (group == null) {
			group = processEngine.getIdentityService().newGroup(groupId);
		}
		group.setName(name);
		group.setType(type);
		processEngine.getIdentityService().saveGroup(group);
		return RELOAD_GROUP;
	}

	public String removeGroup() {
		processEngine.getIdentityService().deleteGroup(groupId);
		return RELOAD_GROUP;
	}

	// ~ ==================================================
	public void setProcessEngine(ProcessEngine processEngine) {
		this.processEngine = processEngine;
	}

	public List<User> getUsers() {
		 return users;
	}

	public User getUser() {
		return user;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public List<Group> getGroups() {
		return groups;
	}

	public Group getGroup() {
		return group;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setType(String type) {
		this.type = type;
	}
}
