import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class App {

    private static void gitAuth(GitWrangler repo) {
        // Tegeleb git autentimisega
        // TODO: SSH authentication

        Scanner scan = new Scanner(System.in);

        if (!Objects.equals(System.getenv("GIT_USER"), null)) {
            System.out.println(System.getenv("GIT_USER"));
            repo.auth();
        } else {
            System.out.println("Git username: ");
            String user = scan.nextLine();
            System.out.println("Git password: ");
            String pass = scan.nextLine();
            repo.auth(user, pass);
        }
    }

    private static void uuendaFaile(FileWrangler failisüsteem) throws Exception {
        List<DotFile> uuendustegaFailid = failisüsteem.leiaUuendused();
        for (DotFile fail : uuendustegaFailid) {
            System.out.println(fail);
            System.out.println(fail.getMuudatus());
        }
    }

    private static void gitPull(GitWrangler repo) throws GitAPIException {
        Scanner scan = new Scanner(System.in);

        gitAuth(repo);
        while (!repo.testAuth()) {
            System.out.println("Authentication failed!");
            System.out.println("Retry? Y/n  ");
            String r = scan.nextLine();
            if (Objects.equals(r, "n") || Objects.equals(r, "N")) {
                return;
            } else {
                gitAuth(repo);
            }
        }
        repo.pullRemote();
    }

    public static void main(String[] args) throws Exception {
        Scanner scan = new Scanner(System.in);
        String gitPath = "./.data/";
        String databasePath = "./data";

        // Banner and version info
        String banner = "     _     _       _       \r\n    | | __| | ___ | |_ ___ \r\n _  | |/ _` |/ _ \\| __/ __|\r\n| |_| | (_| | (_) | |_\\__ \\\r\n \\___/ \\__,_|\\___/ \\__|___/\n";
        String version = "0.1.0";
        System.out.println(banner);
        System.out.println("By MMK & rjogi");
        System.out.printf("Version: %s\n", version);

        System.out.println("Connecting to repo.");
        GitWrangler repo = new GitWrangler(gitPath);
        FileWrangler failisüsteem = new FileWrangler(gitPath, databasePath);

        ui:
        while (true) {
            System.out.println("Possible actions:\n1. Pull remote\n2. Sync files\n3. Add file to tracking\n4. Remove file from tracking\nE(xit)");
            String action = scan.nextLine();
            switch (action) {
                case "1":
                    try {
                        gitPull(repo);
                    } catch (GitAPIException e) {
                        System.out.println("Remote pull failed!");
                    }
                    break;
                case "2":
                    //sync
                    break;
                case "3":
                    //add
                    break;
                case "4":
                    //remove
                    break;
                default:
                    System.out.println("Exiting..");
                    break ui;
            }
        }
    }
}
