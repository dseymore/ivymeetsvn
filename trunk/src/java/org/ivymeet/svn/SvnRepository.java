package org.ivymeet.svn;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;

import fr.jayasoft.ivy.Artifact;
import fr.jayasoft.ivy.repository.AbstractRepository;
import fr.jayasoft.ivy.repository.BasicResource;
import fr.jayasoft.ivy.repository.Resource;
import fr.jayasoft.ivy.repository.TransferEvent;
import fr.jayasoft.ivy.util.CopyProgressEvent;
import fr.jayasoft.ivy.util.CopyProgressListener;
import fr.jayasoft.ivy.util.FileUtil;
import fr.jayasoft.ivy.util.Message;

public class SvnRepository extends AbstractRepository {

	private ISVNOptions options;
	private ISVNAuthenticationManager authManager;
	private SVNClientManager clientManager;

	public SvnRepository(String username, String password) {
		// get our connection/svn client ready
		options = new DefaultSVNOptions();
		authManager = new BasicAuthenticationManager(username, password);
		clientManager = SVNClientManager.newInstance(options, authManager);
	}

	@Override
	public String getName() {
		return "IvyMeetSvn Subversion Repository";
	}

	/**
	 * Installs the artifact into SVN. yes. fucking awesome. I know. 
	 */
	@Override
	public void put(Artifact artifact, File source, String destination, boolean overwrite) throws IOException {
		//More than likely, we've been invoked without a client yet. 
		if (clientManager == null){
			throw new IllegalArgumentException("Null client.. thats a bit wrong..");
		}
		//next trick, is that we dont have anything other than the destination to work with.. so, we need to walk backwards directory by directory until
		//we can find something to checkout.
		String[] parts = destination.split("/");
		boolean checkedOut = false;
		int position = 0;
		File tempArea = new File(System.getProperty("java.io.tmpdir") + "/ivycommit-" + new Date().getTime());
		for (int i = parts.length; i > 0 && !checkedOut; i--){
			position = i; 
			//build the string
			StringBuffer buf = new StringBuffer();
			for (int j = 0; j < i; j++){
				buf.append(parts[j]).append("/");
			}
			Message.verbose("Trying : " + buf);
			try{
				SVNURL baseUrl = SVNURL.parseURIDecoded(buf.toString());
				//recursively download.. so we can check if we are overwriting.
				//check it out.
				clientManager.getUpdateClient().doCheckout(baseUrl, tempArea, SVNRevision.HEAD, SVNRevision.HEAD, true);
				checkedOut = true;
			}catch(Exception e){
				Message.debug("Unable to checkout: " + buf.toString() + " this is ok if this is a new org/module/revision");
			}
		}
		if (checkedOut = true){
			//what level are we at? 
			//how do we take the destination and build up the directories & files we need?
			StringBuffer dirpath = new StringBuffer(tempArea.getAbsolutePath());
			for (int i = position; i + 1 < parts.length; i++){
				dirpath.append("/").append(parts[i]);
				File f = new File(dirpath.toString());
				if (!f.exists()){
					try{
						Message.verbose("Making directory: " + f.getAbsolutePath());
						f.mkdir();
						clientManager.getWCClient().doAdd(f, true, true, true, true);
					}catch(Exception e){
						Message.error("Unable to create directory!" + f.getAbsolutePath());
						e.printStackTrace();
						throw new IOException("Unable to create directory.");
					}
				}
			}
			//the last one is the artifact
			File theArtifact = new File(dirpath.toString() + "/" + parts[parts.length -1]);
			Message.verbose("Touching artifact file: " + theArtifact.getAbsolutePath());
			//faking a resource to use the fileutil to copy. 
			BasicResource reSource = new BasicResource(source.getAbsolutePath(), true, 1, new Date().getTime(), true);
			fireTransferInitiated(reSource,  TransferEvent.REQUEST_PUT);
			try {
				FileUtil.copy(source, theArtifact, new CopyProgressListener() {
					public void start(CopyProgressEvent evt) {
						fireTransferStarted();
					}

					public void progress(CopyProgressEvent evt) {
						fireTransferProgress(evt.getReadBytes());
					}

					public void end(CopyProgressEvent evt) {
						fireTransferCompleted(evt.getReadBytes());
					}
				});
			} catch (IOException ex) {
				fireTransferError(ex);
				throw ex;
			} catch (RuntimeException ex) {
				fireTransferError(ex);
				throw ex;
			}

			try{
				Message.verbose("Adding File: " + theArtifact.getAbsolutePath());
				clientManager.getWCClient().doAdd(theArtifact, true, false, true, true);
				//now check'er in.
				clientManager.getCommitClient().doCommit(new File[]{tempArea}, false, "Automated Artifact Commit", true, true);
			}catch(Exception e){
				Message.error("unable to add file: " + theArtifact.getAbsolutePath());
				throw new IOException("Unable to add file to svn repository");
			}
		}
		tempArea.delete();
	}

	@Override
	public void get(String source, File destination) throws IOException {
		Message.info("Getting resource " + source);
		SvnResource resource = (SvnResource)getResource(source);
		fireTransferInitiated(resource, TransferEvent.REQUEST_GET);
		try {
			FileUtil.copy(resource.getInputStream(), destination, new CopyProgressListener() {
				public void start(CopyProgressEvent evt) {
					fireTransferStarted();
				}

				public void progress(CopyProgressEvent evt) {
					fireTransferProgress(evt.getReadBytes());
				}

				public void end(CopyProgressEvent evt) {
					fireTransferCompleted(evt.getReadBytes());
				}
			});
		} catch (IOException ex) {
			fireTransferError(ex);
			throw ex;
		} catch (RuntimeException ex) {
			fireTransferError(ex);
			throw ex;
		}
	}

	@Override
	public Resource getResource(String source) throws IOException {
		return new SvnResource(clientManager,source);
	}

	@Override
	public List list(String parent) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
}
