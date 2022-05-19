public class ServerThread implements Runnable{
    private GitWrangler repo;
    private FileWrangler files;

    private volatile boolean running = true;

    public ServerThread(GitWrangler repo, FileWrangler files) {
        this.repo = repo;
        this.files = files;
    }

    public void terminate(){
        API.stopserver();
    }

    public void run() {
        API.start(repo, files);
    }
}
