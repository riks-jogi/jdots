import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.IOException;

public class GitWrangler {
    Repository localRepo;
    Git git;
    private String username;
    private String secret;


    GitWrangler(String localPath) throws IOException {
        localRepo = new FileRepository(localPath + "/.git");
        git = new Git(localRepo);
    }

    public void auth(){
        this.username = System.getenv("GIT_USER");
        this.secret = System.getenv("GIT_SECRET");
    }

    public void auth(String user, String pass){
        this.username = user;
        this.secret = pass;
    }

    public boolean testAuth(){
        LsRemoteCommand test = git.lsRemote();
        test.setCredentialsProvider(new UsernamePasswordCredentialsProvider(this.username, this.secret));
        try {
            test.call();
            return true;
        } catch (GitAPIException e) {
            return false;
        }
    }

    public boolean pullRemote() throws GitAPIException {
        PullResult result = git.pull().setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, secret)).call();
        return result.isSuccessful();
    }

    public void addCommitPush(String message) throws GitAPIException {
        git.add().addFilepattern(".").call();
        git.commit().setMessage(message).call();
        PushCommand pushCommand = git.push();
        pushCommand.setProgressMonitor(new SimpleMonitor());
        pushCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, secret));
        pushCommand.call();
    }
}

