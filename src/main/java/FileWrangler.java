import java.io.*;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Scanner;

public class FileWrangler {
    public static File loeFail(String failinimi) {
        return new File(failinimi);
    }
    public static Scanner avaLugeja(File loetav) throws Exception {
        return new Scanner(loetav);
    }

    public static void sulgeLugeja(Scanner lugeja) {
        lugeja.close();
    }

    public static FileWriter avaKirjutaja(String kirjutatavFail) throws Exception {
        return new FileWriter(kirjutatavFail);
    }

    public static void sulgeKirjtaja(FileWriter kirjutaja) throws IOException {
        kirjutaja.close();
    }

    public static void väljastaFailiRead(String failinimi) throws Exception {
        Scanner lugeja = avaLugeja(loeFail(failinimi));
        väljastaFailiRead(lugeja);

        sulgeLugeja(lugeja);
    }

    public static void väljastaFailiRead(Scanner lugeja) {
        while (lugeja.hasNextLine()) {
            System.out.println(lugeja.nextLine());
        }
    }

    public static void salvestaNimedJaChecksumid(MessageDigest digest, String checksumFail, String[] dotFailid) throws Exception {
        salvestaNimedJaChecksumid(digest, avaKirjutaja(checksumFail), Arrays.stream(dotFailid).map(File::new).toArray(File[]::new));
    }

    public static void salvestaNimedJaChecksumid(MessageDigest digest, FileWriter checksumFail, File[] dotFailid) throws Exception {
        for (File file : dotFailid) checksumFail.write(file + ";" + genereeriChecksum(digest, file) + "\n");
        sulgeKirjtaja(checksumFail);
        System.out.println("donerooski");
    }

    private static String genereeriChecksum(File fail) throws Exception {
        MessageDigest mdigest = MessageDigest.getInstance("MD5");
        return genereeriChecksum(mdigest, fail);
    }
    private static String genereeriChecksum(String failinimi) throws Exception {
        MessageDigest mdigest = MessageDigest.getInstance("MD5");
        return genereeriChecksum(mdigest, loeFail(failinimi));
    }
    private static String genereeriChecksum(MessageDigest digest, String failinimi) throws Exception {
        return genereeriChecksum(digest, loeFail(failinimi));
    }

    private static String genereeriChecksum(MessageDigest digest, File fail) throws Exception {
        // Võta faili input stream, et lugada sisu baitides
        FileInputStream fis = new FileInputStream(fail);

        // digest(byte[] input) laskes ka korraga faili sisu checksumiks teha aga
        // suurtemate failidega võib mälu otsa saada, seega teeme tükkideks
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        // faili baidid tükkidena MessageDigesti
        while ((bytesCount = fis.read(byteArray)) != -1) digest.update(byteArray, 0, bytesCount);

        fis.close();

        // digest.digest annab decimal formaadis checksumi. Tahame seda hoopis heksadecimalis
        // Kasutame muteeritavat stringi, et tükk haaval teisendada süsteeme
        StringBuilder sb = new StringBuilder();
        for (byte aByte : digest.digest()) sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));

        return sb.toString();
    }

/*    private static void uuendaFail(String failinimi) throws Exception {
        Scanner checksumScanner = avaLugeja(loeFail("checksums"));

        while (checksumScanner.hasNextLine()) {
            String[] nimiJaChecksum = checksumScanner.nextLine().split(";");
            if (Objects.equals(nimiJaChecksum[0], ".data/" + failinimi)) {
                File tulemasFail = loeFail("git/"+failinimi);
                File olemasFail = loeFail(nimiJaChecksum[0]);

                if (!genereeriChecksum(tulemasFail).equals(nimiJaChecksum[1]) && olemasFail.lastModified() > tulemasFail.lastModified()) {
                    FileInputStream uueStream = new FileInputStream(tulemasFail);
                    FileOutputStream vanaStream = new FileOutputStream(olemasFail);

                    byte[] puffer = new byte[1024];
                    int pikkus;

                    while ((pikkus = uueStream.read(puffer)) > 0) {
                        vanaStream.write(puffer, 0, pikkus);
                    }

                    uueStream.close();
                    vanaStream.close();
                    break;
                }
            }
        }
    }*/

    public static void main(String[] args) throws Exception {
        // File vanaFail = loeFail("uustext.txt");
        // File uusFail = loeFail("text.txt");

        // MessageDigest mdigest = MessageDigest.getInstance("MD5");

        // File uuemFile = vanaFail.lastModified() > uusFail.lastModified() ? vanaFail : uusFail;
        // Scanner uuemFileLugeja = avaLugeja(uuemFile);

        // väljastaFailiRead(uuemFileLugeja);
        // sulgeLugeja(uuemFileLugeja);
        // System.out.println();
        // väljastaFailiRead(".data/.one");
        // väljastaFailiRead(".data/.two");
        // väljastaFailiRead(".data/.three");
        // väljastaFailiRead(".data/checksums");

        // System.out.println(genereeriChecksum(mdigest, loeFail(".data/.one")));

        // salvestaNimedJaChecksumid(mdigest, "checksums", new String[]{".data/.one", ".data/.two", ".data/.three"});

        //uuendaFail(".one");
        //uuendaFail(".data/.twogit");
    }
}
