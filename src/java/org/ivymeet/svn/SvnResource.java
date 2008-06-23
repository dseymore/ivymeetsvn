package org.ivymeet.svn;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.ISVNInfoHandler;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;

import fr.jayasoft.ivy.repository.Resource;
import fr.jayasoft.ivy.util.Message;

/**
 * Modle for the SVN interaction. 
 * @author seymore
 *
 */
public class SvnResource implements Resource {
	private String source;
	private SVNClientManager client;
	private SVNURL svnUrl;
	private SVNInfo info;
	private File f = new File(System.getProperty("java.io.tmpdir") + "/ivy-" + new Date().getTime());

	public SvnResource(SVNClientManager client, String source) {
		this.client = client;
		this.source = source;
	}

	private void init() {
		try{
			svnUrl = SVNURL.parseURIDecoded(source);
			
			client.getWCClient().doInfo(svnUrl,SVNRevision.HEAD, SVNRevision.HEAD, false, new ISVNInfoHandler(){
				 public void handleInfo(SVNInfo info) throws SVNException{
					 setInfo(info);
				 }
			});
			f.createNewFile();
			f.deleteOnExit(); //we wont need it after we close
			FileOutputStream fos = new FileOutputStream(f);
			client.getWCClient().doGetFileContents(svnUrl, SVNRevision.HEAD, SVNRevision.HEAD, true, fos);
		}catch(SVNException e){
			Message.error("You've got a bad svn url: " + source);
			throw new RuntimeException("Bad Bad source url.");
		}catch(Exception e){
			Message.error("Unable to create the temp file:" + f.getAbsolutePath());
		}
	}

	public String getName() {
		return source;
	}

	public long getLastModified() {
		init();
		return info == null ? 0 : info.getCommittedDate().getTime();
	}

	public long getContentLength() {
		init();
		return f.length();
	}

	public boolean exists() {
		boolean exists = false;
		try{
			svnUrl = SVNURL.parseURIDecoded(source);
			client.getWCClient().doInfo(svnUrl,SVNRevision.HEAD, SVNRevision.HEAD, false, new ISVNInfoHandler(){
				 public void handleInfo(SVNInfo info) throws SVNException{
					 //nothing here
				 }
			});
			exists = true;
		}catch(Exception e){
			Message.verbose("Searched for and could not find : " + source);
			exists = false;
		}
		return exists;
	}


	public InputStream getInputStream() throws IOException {
		init();
		Message.verbose("Retrieving file " + source);
		FileInputStream fis = new FileInputStream(this.f);
		return fis;
	}

	@Override
	public Resource clone(String arg0) {
		// arg0 passed in is the sh1 file that MIGHT exist in the repository.
		return new SvnResource(client, arg0);
	}

	@Override
	public boolean isLocal() {
		return false;
	}

	@Override
	public InputStream openStream() throws IOException {
		return getInputStream();
	}

	public SVNInfo getInfo() {
		return info;
	}

	public void setInfo(SVNInfo info) {
		this.info = info;
	}
	
	
}
