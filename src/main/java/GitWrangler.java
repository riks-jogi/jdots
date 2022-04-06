import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

public class GitWrangler {
    Repository localRepo;
    Git git;


    GitWrangler(String localPath) throws IOException {
        localRepo = new FileRepository(localPath + "/.git");
        git = new Git(localRepo);
    }

    public void pullRemote(String user, String pass) throws GitAPIException {
        PullResult result = git.pull().setCredentialsProvider(new UsernamePasswordCredentialsProvider(user, pass)).call();
    }

    public void addCommitPush(String message, String user, String pass) throws GitAPIException {
        git.add().addFilepattern(".").call();
        git.commit().setMessage(message).call();
        PushCommand pushCommand = git.push();
        pushCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(user, pass));
        pushCommand.call();
    }

}

