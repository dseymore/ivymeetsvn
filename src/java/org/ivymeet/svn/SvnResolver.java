package org.ivymeet.svn;

import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;

import fr.jayasoft.ivy.resolver.RepositoryResolver;

/**
 * 
 * @author dseymore d.seymore@gmail.com
 */
public class SvnResolver extends RepositoryResolver {

	private String username;
	private String password;

	public SvnResolver() {
		// get the dav stuff ready to go
		DAVRepositoryFactory.setup();
	}

	@Override
	public SvnRepository getRepository() {
		setRepository(new SvnRepository(getUsername(), getPassword()));
		return (SvnRepository) super.getRepository();
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}


}
