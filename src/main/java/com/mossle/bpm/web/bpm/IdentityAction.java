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
	@Result(name = IdentityAction.RELOAD_USER, location = "identity!listUsers.do?operationMode=RETRIEVE", type = "redirect")
})
public class IdentityAction extends BaseAction {
    public static final String RELOAD_USER = "reload-user";
    private ProcessEngine processEngine;
	private List<User> users;
	private String userId;
	private User user;
	private String firstName;
	private String lastName;
	private String email;
	private List<Group> groups;

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
}
