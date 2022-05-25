import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.IOException;

/**
 * Klass, mille abil liidestub programm Gitiga.
 * Selle klassi isend iseloomustab ühte repositooriumi ja pakub abifunktsioonie ka autentimiseks.
 * Loomisel on vaja anda viit repositooriumi kaustale.
 */
public class GitWrangler {
    Repository localRepo;
    Git git;
    private String username;
    private String secret;


    GitWrangler(String localPath) throws IOException {
        localRepo = new FileRepository(localPath + "/.git");
        git = new Git(localRepo);
    }

    /**
     * Meetod kasutajaandmete Keskkonnamuutujatest lugemiseks.
     */
    public void auth() {
        this.username = System.getenv("GIT_USER");
        this.secret = System.getenv("GIT_SECRET");
    }

    /**
     * Meetod, mis sätib kasutajaandmed sisendist.
     */
    public void auth(String user, String pass) {
        this.username = user;
        this.secret = pass;
    }

    /**
     * Meetod kasutajatunnuste õigsuse kontrolliks.
     * Meetod kasutab `git remote-ls` käsku, sest see on võrdlemisi kergekaaluline ja ei alusta ühtegi failioperatsiooni.
     */
    public boolean testAuth() {
        LsRemoteCommand test = git.lsRemote();
        try {
            test.setCredentialsProvider(new UsernamePasswordCredentialsProvider(this.username, this.secret));
            test.call();
            return true;
        } catch (NullPointerException |
                 GitAPIException e) { // Kui username ja secret pole defineeritud või kui git ise katki
            return false;
        }
    }

    /**
     * Meetod, mis tõmbab alla repositooriumi uuenduse.
     * Ekvivalentne käsuga `git pull`
     * Meetod kuvab ka stdouti käsu progressi.
     *
     * @return boolean, kas operatsioon õnnestus
     */
    public boolean pullRemote() throws GitAPIException {
        PullCommand result = git.pull();
        result.setProgressMonitor(new SimpleMonitor());
        result.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, secret));
        PullResult success = result.call();
        return success.isSuccessful();
    }

    /**
     * Meetod failide repositooriumi pushimiseks.
     * Ekvivalentne käsuga:
     * `git add * && git commit -m message && git push`
     *
     * @param message Commiti sõnum.
     */
    public void addCommitPush(String message) throws GitAPIException {
        git.add().addFilepattern(".").call();
        git.commit().setMessage(message).call();
        PushCommand pushCommand = git.push();
        pushCommand.setProgressMonitor(new SimpleMonitor());
        pushCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, secret));
        pushCommand.call();
    }
}