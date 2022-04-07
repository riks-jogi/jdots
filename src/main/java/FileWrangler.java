/**
 * Põhiline autor: Mihkel Martin Kasterpalu
 * <p>
 * Checksum koodi baseerub sellel: https://www.geeksforgeeks.org/how-to-generate-md5-checksum-for-files-in-java/
 */

import java.io.*;
import java.util.*;

public class FileWrangler {
    private List<DotFile> dotfailid = new ArrayList<>();

    public void lisaDotfail(String pathJaNimi) throws Exception {
        File dotfailiInfo = new File(pathJaNimi);
        this.dotfailid.add(new DotFile(dotfailiInfo.getName(), dotfailiInfo.getParent()));
    }
    public void eemaldaDotfail(String pathJaNimi) {
        File dotfailiInfo = new File(pathJaNimi);
        this.dotfailid.removeIf(file -> Objects.equals(file.getNimi(), dotfailiInfo.getName()));
    }

    public List<DotFile> leiaUuendused(String gitPath) throws Exception {
        List<DotFile> uuendusegaFailid = new ArrayList<>();
        for (DotFile file: this.dotfailid) {
            if (file.kontrolliMuudatusi(gitPath)) uuendusegaFailid.add(file);
        }

        return uuendusegaFailid;
    }

    public void salvestaNimedJaChecksumid(String checksumFail) throws IOException {
        FileWriter fileWriter = new FileWriter(checksumFail);
        PrintWriter printWriter = new PrintWriter(fileWriter);

        for (DotFile file: this.dotfailid) {
            printWriter.print(file.toDataString());
        }
        printWriter.close();
    }

    public void uuendaDotFailGit(DotFile dotfile, String gitPath) throws IOException {
        dotfile.uuendaGiti(gitPath);
    }
    public void uuendaDotFailKoahlik(DotFile dotfile, String gitPath) throws Exception {
        dotfile.uuendaKohalikku(gitPath);
        dotfile.uuendaChecksum();
    }

    public List<DotFile> getDotfailid() {
        return dotfailid;
    }
}
