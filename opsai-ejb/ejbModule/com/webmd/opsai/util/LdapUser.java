package com.webmd.opsai.util;

public class LdapUser{
	private String name, firstName, lastName, emailAddr, phoneNumber;
	private String[] memberOf;
	
	public LdapUser(){}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmailAddr() {
		return emailAddr;
	}

	public void setEmailAddr(String emailAddr) {
		this.emailAddr = emailAddr;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String[] getMemberOf() {
		return memberOf;
	}

	public void setMemberOf(String[] memberOf) {
		this.memberOf = memberOf;
	}

}