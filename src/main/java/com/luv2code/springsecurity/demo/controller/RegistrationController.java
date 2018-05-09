package com.luv2code.springsecurity.demo.controller;

import java.util.List;
import java.util.logging.Logger;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.luv2code.springsecurity.demo.user.CrmUser;

@Controller
@RequestMapping("/register")
public class RegistrationController {

	@Autowired
	private UserDetailsManager userDetailsManager;

	private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	private Logger logger = Logger.getLogger(getClass().getName());

	public void initBinder(WebDataBinder dataBinder) {

		StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);

		dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
	}

	@GetMapping("/showRegistrationForm")
	public String showRegistrationPage(Model theModel) {
		theModel.addAttribute("crmUser", new CrmUser());
		return "registration-form";
	}

	@PostMapping("processRegistrationForm")
	public String processRegistrationForm(@Valid @ModelAttribute("crmUser") CrmUser theCrmUser,
			BindingResult theBindingResult, Model theModel) {

		String userName = theCrmUser.getUserName();
		// form validation
		if (theBindingResult.hasErrors()) {
			// dodaje nowy obiekt CrmUser()
			theModel.addAttribute("crmUser", new CrmUser());
			// dodaję komunikat
			theModel.addAttribute("registrationError", "User/password can not be empty");
			logger.warning("User name/password can not be empty.");
			// wracamy do formularza rejestracyjnego
			return "registration-form";

		}

		// check the database if user already exists

		if (doesUserExists(userName)) {
			// dodaje nowy obiekt CrmUser()
			theModel.addAttribute("crmUser", new CrmUser());
			// dodaję komunikat
			theModel.addAttribute("registrationError", "User name already exists");
			logger.warning("User name already exists.");
			// wracamy do formularza rejestracyjnego
			return "registration-form";
		}
		
		// encrypt the password
		String encodedPassword = passwordEncoder.encode(theCrmUser.getPassword());
		
		// prepend the encoding algorithm id
		encodedPassword = "{bcrypt}" + encodedPassword; 
		
		// give user default role of "employee"
		
		List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("ROLE_EMPLOYEE");
		// create user details object
		User tempUser = new User(userName,encodedPassword,authorities);
		// save user in the database
		userDetailsManager.createUser(tempUser);

		return "registration-confirmation";
	}

	private boolean doesUserExists(String userName) {
		// check in databesa if the user already exists
		boolean exists = userDetailsManager.userExists(userName);
		return exists;
	}
}
