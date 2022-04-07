/**
 * Põhiline autor: Mihkel Martin Kasterpalu
 * <p>
 * Checksum koodi baseerub sellel: https://www.geeksforgeeks.org/how-to-generate-md5-checksum-for-files-in-java/
 */

import java.io.*;
import java.util.*;

public class FileWrangler {
    private List<DotFile> dotfailid = new ArrayList<>();
    private final String gitPath;
    private final String databasePath;

    public void lisaDotfail(String pathJaNimi) throws Exception {
        File dotfailiInfo = new File(pathJaNimi);
        this.dotfailid.add(new DotFile(dotfailiInfo.getName(), dotfailiInfo.getParent() + "/"));
    }

    public void eemaldaDotfail(String pathJaNimi) {
        File dotfailiInfo = new File(pathJaNimi);
        this.dotfailid.removeIf(file -> Objects.equals(file.getNimi(), dotfailiInfo.getName()));
    }

    public FileWrangler(String gitPath, String databasePath) throws Exception {
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
        } catch (FileNotFoundException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<DotFile> leiaUuendused() throws Exception {
        List<DotFile> uuendusegaFailid = new ArrayList<>();
        for (DotFile file : this.dotfailid) {
            if (file.kontrolliMuudatusi(this.gitPath)) uuendusegaFailid.add(file);
        }

        return uuendusegaFailid;
    }

    public void salvestaNimedJaChecksumid() throws IOException {
        FileWriter fileWriter = new FileWriter(this.databasePath);
        PrintWriter printWriter = new PrintWriter(fileWriter);

        for (DotFile file : this.dotfailid) {
            System.out.println(file);
            printWriter.print(file.toDataString());
        }
        printWriter.close();
    }

    public void uuendaDotFailGit(DotFile dotfile) throws IOException {
        dotfile.uuendaGiti(this.gitPath);
    }

    public void uuendaDotFailKoahlik(DotFile dotfile) throws Exception {
        dotfile.uuendaKohalikku(this.gitPath);
        dotfile.uuendaChecksum();
    }

    public List<DotFile> getDotfailid() {
        return dotfailid;
    }
}
