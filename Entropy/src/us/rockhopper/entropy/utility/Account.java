package us.rockhopper.entropy.utility;

/**
 * Contains user information for a player account.
 * 
 * @author Tim Clancy
 * @version 6.5.14
 * 
 */
public class Account {
	private String name;

	public Account(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}
}
