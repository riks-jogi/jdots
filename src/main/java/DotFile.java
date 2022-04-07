import java.io.*;
import java.security.MessageDigest;
import java.util.Objects;
import java.util.Scanner;

public class DotFile {
    private final String nimi;

    private final String path;
    private String checksum;
    private final MessageDigest digest = MessageDigest.getInstance("MD5");
    private int muudatus = 0;

    public String getChecksum() {
        return checksum;
    }

    public void uuendaChecksum() throws Exception {
        this.checksum = genereeriChecksum(this.path + this.nimi);
    }

    public int getMuudatus() {
        return muudatus;
    }

    public String getNimi() {
        return nimi;
    }

    public DotFile(String nimi, String path) throws Exception {
        this.nimi = nimi;
        this.path = path;
        this.checksum = genereeriChecksum(this.path + this.nimi);
    }

    public DotFile(String nimi, String path, String checksum) throws Exception {
        this.nimi = nimi;
        this.path = path;
        this.checksum = checksum;
    }

    private boolean kontrolliMuudatusiKohalik() throws Exception {
        String kontrollChecksum = this.genereeriChecksum(this.path + this.nimi);
        return !Objects.equals(this.checksum, kontrollChecksum);
    }

    private boolean kontrolliMuudatusiGit(String gitPath) throws Exception {
        String kontrollChecksum = this.genereeriChecksum(gitPath + this.nimi);
        return !Objects.equals(this.checksum, kontrollChecksum);
    }

    public boolean kontrolliMuudatusi(String gitPath) throws Exception {
        boolean kohalik = kontrolliMuudatusiKohalik();
        boolean git = kontrolliMuudatusiGit(gitPath);

        // Säti kohalik väärtus, selle kohta kus muudatus on
        if (git && kohalik) this.muudatus = 3;
        else if (git) this.muudatus = 2;
        else if (kohalik) this.muudatus = 1;

        // Tagasta kas fail on kuskil muutunud
        return git || kohalik;
    }

    public void uuendaKohalikku(String gitPath) throws IOException {
        kirjutaÜle(gitPath + this.nimi, this.path + this.nimi);
    }

    public void uuendaGiti(String gitPath) throws IOException {
        kirjutaÜle(this.path + this.nimi, gitPath + this.nimi);
    }

    private void kirjutaÜle(String allikas, String sihtkoht) throws IOException {
        FileInputStream allikasStream = new FileInputStream(allikas);
        FileOutputStream sihtStream = new FileOutputStream(sihtkoht);

        byte[] puffer = new byte[1024];
        int pikkus;

        while ((pikkus = allikasStream.read(puffer)) > 0) {
            sihtStream.write(puffer, 0, pikkus);
        }

        allikasStream.close();
        sihtStream.close();
    }

    private String genereeriChecksum(String pathJaNimi) throws Exception {
        // Võta faili input stream, et lugada sisu baitides
        FileInputStream fis = new FileInputStream(pathJaNimi);

        // digest(byte[] input) laskes ka korraga faili sisu checksumiks teha aga
        // suurtemate failidega võib mälu otsa saada, seega teeme tükkideks
        byte[] byteArray = new byte[1024];
        int bytesCount;

        // faili baidid tükkidena MessageDigesti
        while ((bytesCount = fis.read(byteArray)) != -1) this.digest.update(byteArray, 0, bytesCount);

        fis.close();

        // digest.digest annab decimal formaadis checksumi. Tahame seda hoopis heksadecimalis
        // Kasutame muteeritavat stringi, et tükk haaval teisendada süsteeme
        StringBuilder sb = new StringBuilder();
        for (byte aByte : this.digest.digest()) sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));

        return sb.toString();
    }

    public void väljastaFailiRead() throws FileNotFoundException {
        Scanner lugeja = new Scanner(new File(this.path + this.nimi));
        while (lugeja.hasNextLine()) {
            System.out.println(lugeja.nextLine());
        }
    }

    @Override
    public String toString() {
        return nimi + "\t" + path + nimi;
    }

    public String toDataString() {
        return nimi + ';' + path + ';' + checksum + "\n";
    }
}
