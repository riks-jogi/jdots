import org.eclipse.jgit.api.errors.GitAPIException;

import java.util.List;
import java.util.Objects;
import java.util.Scanner;

/**
 * Peamine klass kasutajaliidese elementide jaoks.
 */
public class App {
    /**
     * Kasutajaliides Git autentimise jaoks.
     * Kasutajal on võimalus täpsustada kasutajanimi ja parool keskkonnamuutujatega, et vältida nende trükkimist.
     * Muutujate puudumisel küsib programm neid interaktiivselt
     *
     * @param repo Kasutatava repositooriumi GitWrangleri isend
     */
    private static void gitAuth(GitWrangler repo) {
        // Tegeleb git autentimisega
        // TODO: SSH authentication

        Scanner scan = new Scanner(System.in);

        if (!Objects.equals(System.getenv("GIT_USER"), null)) {
            System.out.println("Using ENV credentials for: " + System.getenv("GIT_USER"));
            repo.auth();
        } else {
            System.out.println("Git username: ");
            String user = scan.nextLine();
            System.out.println("Git password: ");
            String pass = scan.nextLine();
            repo.auth(user, pass);
        }
    }

    /**
     * Kasutajaliides Repositooriumi "remote" pulli jaoks.
     * Haldab peamiselt autentimise kontrolli ja kasutaja soovil uuesti proovimist.
     *
     * @param repo GitWrangleri isend
     */
    private static void gitPull(GitWrangler repo){
        Scanner scan = new Scanner(System.in);

        gitAuth(repo);
        while (!repo.testAuth()) {
            System.out.println("Authentication failed!");
            System.out.println("Retry? Y/n  ");
            String r = scan.nextLine();
            if (Objects.equals(r, "y") || Objects.equals(r, "Y") || Objects.equals(r, "")) {
                gitAuth(repo);
            } else {
                return;
            }
        }
        try {
            repo.pullRemote();
        } catch (GitAPIException e) {
            System.out.println("Remote pull failed");
        }
    }

    public static void gitPush(GitWrangler repo, String message) throws GitAPIException {
        gitPull(repo);

        if (repo.testAuth()){
            repo.addCommitPush(message);
        }
    }

    /**
     * Kasutades DotFile klassi meetodeid, uurib kas vaadetava faili checksum on erinev kui teadaolev
     * Olenevalt kus fail erinev on (muutusekoht) pakub kasutajale vastavaid variante failide ümber kopeerimiseks
     * @param failisüsteem main funktsioonis defineeritud FileWrangler isend
     */
    private static void fileSyncDialog(FileWrangler failisüsteem, Scanner scan, DotFile fail) throws Exception {
        System.out.println("File\tPath\t\t\tChanged in");

        int muutuseKoht = fail.getMuudatus();
        switch (muutuseKoht) {
            case 1:
                System.out.printf("%s\tThis Device\n", fail);
                System.out.println("Copy the file from this device to Git? (Y/n)");
                String answer = scan.nextLine();

                if (Objects.equals(answer, "y") || Objects.equals(answer, "Y") || Objects.equals(answer, "")) {
                    failisüsteem.uuendaDotFailGit(fail);
                    System.out.println("File updated");
                } else System.out.println("File not updated");
                break;
            case 2:
                System.out.printf("%s\tGit repo\n", fail);
                System.out.println("Copy the file from Git to this device? (Y/n)");
                String fileCopyAnswer = scan.nextLine();

                if (Objects.equals(fileCopyAnswer, "y") || Objects.equals(fileCopyAnswer, "Y") || Objects.equals(fileCopyAnswer, "")) {
                    failisüsteem.uuendaDotFailKoahlik(fail);
                    System.out.println("File updated");
                } else System.out.println("File not updated");
                break;
            case 3:
                System.out.printf("%s\tLocal Device and Git repo\n", fail);
                System.out.println("Fixing this is out of our programs scope. You have to fix it DIY :))))");
                break;
        }
    }

    /**
     * Lisab või eemaldab mingi faili jälgimise
     * Repost ja arvutist faili ei kustuta, kustutab FileWranger.dotfiles Listist ja seega fileindexist
     * @param failisüsteem main funktsioonis defineeritud FileWrangler isend
     * @param task "add" või "remove", olenevalt mida kasutaja valis
     */
    private static boolean addOrRemoveFile(FileWrangler failisüsteem, String task) {
        Scanner scan = new Scanner(System.in);

        System.out.println("Type file path and name: (ex. /home/username/.ssh/config)");
        String dotFailPath = scan.nextLine();
        try {
            if (Objects.equals(task, "add")) failisüsteem.lisaDotfail(dotFailPath);
            else if (Objects.equals(task, "remove")) failisüsteem.eemaldaDotfail(dotFailPath);

            System.out.printf("'%s' was %s\n", dotFailPath, Objects.equals(task, "add") ? "added" : "removed");
            return true;
        } catch (Exception e) {
            System.out.printf("'%s' not found\n", dotFailPath);
            return false;
        }
    }

    /**
     * Peamine kasutajaliides, mis küsib pidevalt kasutajalt soovitud tegevuse.
     * Kutsub välja spetsiifilisemad funktsioonid, kuni kasutaja väljub.
     *
     * Tegevused on järgnevad:
     * 1. Kuva kõik failid, mille seisu jälgitakse.
     * 2. Tõmba repositooriumist alla muudatused.
     * 3. Loe failide indeks ja soorita edasisi käske
     * 4. Lisa fail indeksisse
     * 5. Eemalda fail indeksist
     */
    public static void main(String[] args) throws Exception {
        Scanner scan = new Scanner(System.in);
        String gitPath = "./.data/";
        String databasePath = "./fileindex";

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
            System.out.println("\nPossible actions:\n1. List tracked files\n2. Pull remote\n3. Sync files\n4. Add file to tracking\n5. Remove file from tracking\nE(xit)");
            String action = scan.nextLine();
            switch (action) {
                case "1":
                    System.out.println("Name:\t Path:");
                    for (DotFile file : failisüsteem.getDotfailid()) {
                        System.out.println(file);
                    }
                    break;
                case "2":
                    gitPull(repo);
                    break;
                case "3":
                    List<DotFile> uuendatudFailid = failisüsteem.leiaUuendused();
                    for (DotFile fail: uuendatudFailid) {
                        fileSyncDialog(failisüsteem, scan, fail);
                    }

                    failisüsteem.salvestaNimedJaChecksumid();
                    gitPush(repo, "Synced files");

                    break;
                case "4":
                    // Kui sai lisatud, uuenda fileindex sisu ja pushi giti
                    if (addOrRemoveFile(failisüsteem, "add")) {
                        failisüsteem.salvestaNimedJaChecksumid();
                        gitPush(repo, "Added file");
                    }
                    break;
                case "5":
                    // Kui sai eemaldatud, uuenda fileindex sisu ja ÄRA pushi giti
                    // Ei pushi sest eemaldamine tähendab siin arvutis mitte syncimist
                    // Mingis muus arvutis võibolla tahan seda faili synciga. Repos võiks ikka olla
                    if (addOrRemoveFile(failisüsteem, "remove")) {
                        failisüsteem.salvestaNimedJaChecksumid();
                    }
                    break;
                case "e":
                case "E":
                    System.out.println("Exiting..");
                    break ui;
                default:
                    System.out.printf("Command '%s' not found", action);
            }
        }
    }
}
