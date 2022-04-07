/**
 * Põhiline autor: Mihkel Martin Kasterpalu
 * <p>
 * Checksum koodi baseerub sellel: https://www.geeksforgeeks.org/how-to-generate-md5-checksum-for-files-in-java/
 */

import java.io.*;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class FileWrangler {
    /*
    public static void salvestaNimedJaChecksumid(MessageDigest digest, String checksumFail, String[] dotFailid) throws Exception {
        salvestaNimedJaChecksumid(digest, avaKirjutaja(checksumFail), Arrays.stream(dotFailid).map(File::new).toArray(File[]::new));
    }

    public static void salvestaNimedJaChecksumid(MessageDigest digest, FileWriter checksumFail, File[] dotFailid) throws Exception {
        for (File file : dotFailid) checksumFail.write(file + ";" + genereeriChecksum(digest, file) + "\n");
        sulgeKirjtaja(checksumFail);
        System.out.println("donerooski");
    }
*/

    public static void main(String[] args) throws Exception {
        DotFile one = new DotFile(".one", ".data/");
        System.out.println(one);
    }
}
