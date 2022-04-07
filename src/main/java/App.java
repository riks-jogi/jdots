import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.Objects;
import java.util.Scanner;

public class App {

    private static void gitAuth(GitWrangler repo){
        // Tegeleb git autentimisega
        // TODO: SSH authentication

        Scanner scan = new Scanner(System.in);

        if (!Objects.equals(System.getenv("GIT_USER"), null)){
            System.out.println(System.getenv("GIT_USER"));
            repo.auth();
        }
        else{
            System.out.println("Git username: ");
            String user = scan.nextLine();
            System.out.println("Git password: ");
            String pass = scan.nextLine();
            repo.auth(user, pass);
        }


    }

    public static void main(String[] args) throws IOException, GitAPIException {
        Scanner scan = new Scanner(System.in);

        // Banner and version info
        String banner = "     _     _       _       \r\n    | | __| | ___ | |_ ___ \r\n _  | |/ _` |/ _ \\| __/ __|\r\n| |_| | (_| | (_) | |_\\__ \\\r\n \\___/ \\__,_|\\___/ \\__|___/\n";
        String version = "0.1.0";
        System.out.println(banner);
        System.out.println("By MMK & rjogi");
        System.out.printf("Version: %s\n", version);

        System.out.println("Connecting to repo.");
        GitWrangler repo = new GitWrangler("./.data");

        // User interactions
        System.out.println("Pull remote? Y/n  ");
        String pull = scan.nextLine();
        if (Objects.equals(pull, "") || Objects.equals(pull, "y") || Objects.equals(pull, "Y")){
            gitAuth(repo);
            while (!repo.testAuth()){
                System.out.println("Authentication failed:");
                System.out.println("Retry? Y/n  ");
                String r = scan.nextLine();
                if (Objects.equals(r, "n") || Objects.equals(r, "N")) {
                    break;
                }else {
                    gitAuth(repo);
                }
            }
            repo.pullRemote();
        }

        // Scan registry and lookup diffs


    }
}
