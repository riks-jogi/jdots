import java.io.*;
import java.security.MessageDigest;
import java.util.Objects;

/**
 * DotFile signaleerib ühte jälgitavat .file-i ehk dot file-i
 * Talletab nime ja viidet kus fail arvutis on ja kus fail gitiga jälgitavas repos os
 * Nimi on failinimi ilma pathita ja path ei sisalda lõpus failinime
 * (nt ssh configi puhul nimi="config" path="/home/user/.ssh/")
 * muudatus võib omada 3me väärtust 1- muutus arvutis; 2- mutuus repos; 3- muutus mõlemas
 * Kuna diff tööriista sisse ehitamine polnud selle projekti jaoks mõistlik, on 3ndal juhul
 * võimalik ainult üks või teine fail täielikult üle kirjutada.
 */
public class DotFile {
    private final String nimi;
    private final String path;
    private String checksum;
    private int muudatus = 0;

    // Kasutatud jälgitavate failide lisamisel käsitsi
    public DotFile(String nimi, String path) throws Exception {
        this.nimi = nimi;
        this.path = path;
        this.checksum = genereeriChecksum(this.path + this.nimi);
    }

    // Kasutatud jälgitavate failide lugemisel fileindex failist
    public DotFile(String nimi, String path, String checksum) {
        this.nimi = nimi;
        this.path = path;
        this.checksum = checksum;
    }

    // Kontrollib kas fileindexist loetud/kirjutatav checksum teine kui praegu arvutist loetud faili oma
    private boolean kontrolliMuudatusiKohalik() throws Exception {
        String kontrollChecksum = this.genereeriChecksum(this.path + this.nimi);
        return !Objects.equals(this.checksum, kontrollChecksum);
    }

    // Kontrollib kas fileindexist loetud/kirjutatav checksum teine kui praegu gitist pullitud faili oma
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

    // Need kaks funktsiooni loodud, et oleks lihtsalt aru saada kuhu mida kopeeritakse
    public void uuendaKohalikku(String gitPath) throws IOException {
        kirjutaÜle(gitPath + this.nimi, this.path + this.nimi);
        this.muudatus = 0;
    }

    public void uuendaGiti(String gitPath) throws IOException {
        kirjutaÜle(this.path + this.nimi, gitPath + this.nimi);
        this.muudatus = 0;
    }

    // Kopeerib baithaaval kas gitist pullitud faili arvutisse või vastupidi
    // Äkki saab teha mingi ühe reaga seda tegelikult? Leidsin kuskilt ja töötab. Las olla.
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

    // Baseerub tugevalt sellel artiklil: https://www.geeksforgeeks.org/how-to-generate-md5-checksum-for-files-in-java/
    // Tagastab MessageDigesti kasutades mingi faili sisu järgi loodud MD5 checksumi. Iga sisu on erineva sumiga (teoreetiliselt)
    private String genereeriChecksum(String pathJaNimi) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("MD5");

        // Võta faili input stream, et lugada sisu baitides
        FileInputStream fis = new FileInputStream(pathJaNimi);

        // digest(byte[] input) laskes ka korraga faili sisu checksumiks teha aga
        // suurtemate failidega võib mälu otsa saada, seega teeme tükkideks
        byte[] byteArray = new byte[1024];
        int bytesCount;

        // faili baidid tükkidena MessageDigesti
        while ((bytesCount = fis.read(byteArray)) != -1) digest.update(byteArray, 0, bytesCount);

        fis.close();

        // digest.digest annab decimal formaadis checksumi. Tahame seda hoopis heksadecimalis
        // Kasutame muteeritavat stringi, et tükk haaval teisendada süsteeme
        StringBuilder sb = new StringBuilder();
        for (byte aByte : digest.digest()) sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));

        return sb.toString();
    }

    // Kasutatakse faili trackimisse lisades.
    // parameeter checksum on mingi suvaline väärtus
    // Nii saab kasutaja otsustada, kas õige versioon on gitis või selles arvutis kust lisati
    // sest ei lokaalse ega giti faili checksum klapi nn "õige versiooni" checksumiga
    // sest "õige versiooni" checksum määratakse siin suvaliselt
    public void uuendaChecksum(String checksum) {
        this.checksum = checksum;
    }

    // Kasutatakse, kui arvutis olev fail kirjutatakse üle gitist tulevaga. Vajalik, et järgmine kord
    // fileindex sisse lugedes oleks seal õige sum, mitte eelmise arvutis oleva versiooni sum
    public void uuendaChecksum() throws Exception {
        this.checksum = genereeriChecksum(this.path + this.nimi);
    }

    public int getMuudatus() {
        return this.muudatus;
    }

    public String getNimi() {
        return this.nimi;
    }

    @Override
    public String toString() {
        return this.nimi + "\t" + this.path + this.nimi;
    }

    // Formaat, milles lõpuks fileindex'isse salvestan
    public String toDataString() {
        return this.nimi + ';' + this.path + ';' + this.checksum + "\n";
    }
}
