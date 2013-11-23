/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package storybook.pro.guardian;

import storybook.StorybookApp;

/**
 *
 * @author favdb
 */
public class Guardian {
	private static Guardian instance;

	public static Guardian getInstance() {
		if (instance == null) {
			instance = new Guardian();
		}
		return instance;
	}
	public void init() {

	}
	public void unlock() {

	}

	public boolean isLicenseValid() {
		return false;
	}

	public void setEmail(String text) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public void setPassword(String pw) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public String getEmail() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public String getPassword() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public boolean check() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public String getErrorMessage() {
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
