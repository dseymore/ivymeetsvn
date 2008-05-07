package org.ivymeet.svn;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;

import fr.jayasoft.ivy.resolver.RepositoryResolver;
import fr.jayasoft.ivy.util.Message;

/**
 * 
 * @author dseymore d.seymore@gmail.com
 */
public class SvnResolver extends RepositoryResolver {

	private String username;
	private String password;
	private String secure;

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

	public String getSecure() {
		return secure;
	}

	public void setSecure(String secure) {
		this.secure = secure;
		if (secure != null && !"".equalsIgnoreCase(secure)){
			//see if the file exists and has username and password already
			File localSecurity = new File(System.getProperty("user.home") + "/.ivyMeetSvn-" + secure);
			if (!localSecurity.exists()){
				try{
					localSecurity.createNewFile();
				}catch(Exception e){
					Message.error("Unable to write to user's home directory to create a security file.");
					throw new RuntimeException("You have to have the ability to write to your home directory.");
				}
				String username = "";
				String password = "";
				try {
					System.out.println("Please enter your SVN username: (will only happen once)");
					BufferedReader reader = new BufferedReader(new InputStreamReader (System.in));
					username  = reader.readLine();
					reader.close();
					System.out.println("Please enter your SVN password: (will only happen once)");
					reader = new BufferedReader(new InputStreamReader (System.in));
					password  = reader.readLine();
					reader.close();
				} catch (IOException e) {				
					throw new RuntimeException("Cannot ask for input.");
				}
				String alltogether = username + ":" + password;
				
				
				
			}
			
			
		}
	}
	
}
