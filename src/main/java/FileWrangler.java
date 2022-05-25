import java.io.*;
import java.util.*;

/**
 * Klass, mis loob, talletab ja lõpuks salvestab jälgitud dot failid fileindex'isse
 * fileindexis on talletatud faili nimi, path süsteemis ja sisu MD5 checksum
 * Fileindex'is on iga rida DotFile klassi loomiseks vajalik info. FileWrangler alguses loetakse
 * fileindex'ist kõik read DotFile'ideks ja lisatakse dotfailid listi.
 * Kõik failid, mis on dotfailid listis salvestatakse lõpuks fileindex faili.
 */
public class FileWrangler {
    private final List<DotFile> dotfailid = new ArrayList<>();
    private final String gitPath;
    private final String databasePath;

    public void lisaDotfail(String pathJaNimi) throws Exception {
        // Kontrollib kas juba jälgitakse sama pathi ja nimega faili
        for (DotFile fail : this.dotfailid) {
            String[] filedata = fail.toDataString().split(";");
            if (Objects.equals(filedata[1] + filedata[0], pathJaNimi)) {
                throw new Exception("The file is already being tracked.");
            }
        }

        File dotfailiInfo = new File(pathJaNimi);
        DotFile uusFail = new DotFile(dotfailiInfo.getName(), dotfailiInfo.getParent() + "/");

        // Kontrollib kas githubis juba selline fail. Kui pole siis lisab
        // Vajalik, sest dotfile peab olema ka gitis et programm töötaks
        // Aga alati git kausta kirjutamine kirjutaks üle githubis jälgitud dot faili
        // iga kord kui uues arvutis seda jälgima hakatase
        File gitFail = new File(gitPath + dotfailiInfo.getName());

        if (!gitFail.exists()) uuendaDotFailGit(uusFail);

        uusFail.uuendaChecksum("000000000");
        this.dotfailid.add(uusFail);
    }

    public void eemaldaDotfail(String pathJaNimi) {
        File dotfailiInfo = new File(pathJaNimi);
        this.dotfailid.removeIf(file -> Objects.equals(file.getNimi(), dotfailiInfo.getName()));
    }

    public FileWrangler(String gitPath, String databasePath) {
        this.gitPath = gitPath;
        this.databasePath = databasePath;
        this.loeDatabaasistFailid(databasePath);
    }

    private void loeDatabaasistFailid(String databasePath) {
        try {
            Scanner dotfailiRead = new Scanner(new File(databasePath));

            while (dotfailiRead.hasNextLine()) {
                String[] failiInfo = dotfailiRead.nextLine().split(";");
                this.dotfailid.add(new DotFile(failiInfo[0], failiInfo[1], failiInfo[2]));
            }
            dotfailiRead.close();

        } catch (FileNotFoundException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Kasutab DotFile sees kirjutatud meetodied, mis võrdlevad
    // Trackitud faili viimast teadaolevad checksumi masinas oleva faili ja gitis oleva haili checksumidega
    // Tagastab kõik DotFile'id, kus mingi võrdlus pole samaväärne (ehk kuskil failis muudatus)
    public List<DotFile> leiaUuendused() throws Exception {
        List<DotFile> uuendusegaFailid = new ArrayList<>();
        for (DotFile file : this.dotfailid) {
            if (file.kontrolliMuudatusi(this.gitPath)) {
                uuendusegaFailid.add(file);
            }
        }

        return uuendusegaFailid;
    }

    // Salvestab fileindex faili järgmiseks korraks kõikide jälgitud DotFile'ide nime, pathi ja checksumi
    public void salvestaNimedJaChecksumid() throws IOException {
        FileWriter fileWriter = new FileWriter(this.databasePath);
        PrintWriter printWriter = new PrintWriter(fileWriter);

        for (DotFile file : this.dotfailid) {
            printWriter.print(file.toDataString());
        }
        printWriter.close();
    }

    // Nende funktsioonide mõte on lihtsamalt main'is aru saada, mis fail kus muudetakse
    public void uuendaDotFailGit(DotFile dotfile) throws Exception {
        dotfile.uuendaGiti(this.gitPath);
        dotfile.uuendaChecksum();
    }

    public void uuendaDotFailKoahlik(DotFile dotfile) throws Exception {
        dotfile.uuendaKohalikku(this.gitPath);
        dotfile.uuendaChecksum();
    }

    public List<DotFile> getDotfailid() {
        return this.dotfailid;
    }
}
