package org.ivymeet.svn;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;

import fr.jayasoft.ivy.Artifact;
import fr.jayasoft.ivy.repository.AbstractRepository;
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

	@Override
	public void put(Artifact artifact, File source, String destination, boolean overwrite) throws IOException {
		// TODO Auto-generated method stub
		super.put(artifact, source, destination, overwrite);
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
