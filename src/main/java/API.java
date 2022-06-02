import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.path;
import static spark.Spark.post;
import static spark.Spark.staticFiles;
import static spark.Spark.stop;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import com.google.gson.Gson;

public class API {
    public static void start(GitWrangler git, FileWrangler file) {
        staticFiles.location("/public");
        Gson gson = new Gson();
        path("/api", () -> {
            path("/files", () -> {
                get("", (req, res) -> {
                    try {
                        res.type("application/json");
                        res.header("Access-Control-Allow-Origin", "*");
                        git.pullRemote();
                        file.leiaUuendused();

                        res.status(200);
                        return file.getDotfailid();
                    } catch (Exception e) {
                        res.status(400);
                        return e.toString();
                    }
                }, JsonConvert.json());

                post("", (req, res) -> {
                    try {
                        String json = req.body();
                        Map params = gson.fromJson(json, Map.class);
                        file.lisaDotfail(String.valueOf(params.get("path")));
                        file.salvestaNimedJaChecksumid();
                        git.addCommitPush("Added " + params.get("path"));

                        res.status(200);
                        return "OK";
                    } catch (Exception e) {
                        res.status(400);
                        return e.toString();
                    }
                }, JsonConvert.json());
                delete("", (req, res) -> {
                    try {
                        String json = req.body();
                        Map params = gson.fromJson(json, Map.class);
                        file.eemaldaDotfail(String.valueOf(params.get("path")));
                        file.salvestaNimedJaChecksumid();

                        res.status(200);
                        return "OK";
                    } catch (Exception e) {
                        res.status(400);
                        return e.toString();
                    }
                }, JsonConvert.json());
            });
            path("/sync", () -> post("", (req, res) -> {
                try {
                    String json = req.body();
                    Map params = gson.fromJson(json, Map.class);
                    DotFile fileToSync = file.getDotfailid().get(Integer.parseInt((String) params.get("id")));

                    if (Objects.equals(params.get("loc"), "git")) {
                        file.uuendaDotFailGit(fileToSync);
                        file.salvestaNimedJaChecksumid();
                        git.addCommitPush("Synced " + fileToSync.getNimi());

                        res.status(200);
                        return "OK";
                    } else if (Objects.equals(params.get("loc"), "local")) {
                        file.uuendaDotFailKoahlik(file.getDotfailid().get(Integer.parseInt((String) params.get("id"))));
                        file.salvestaNimedJaChecksumid();

                        res.status(200);
                        return "OK";
                    }
                } catch (Exception e) {
                    res.status(400);
                    return e.toString();
                }

                res.status(400);
                return "Bad request";
            }, JsonConvert.json()));
            path("/auth", () -> {
                post("", (req, res) -> {
                    try {
                        res.type("application/json");
                        res.header("Access-Control-Allow-Origin", "*");
                        String json = req.body();
                        Map params = gson.fromJson(json, Map.class);
                        git.auth(params.get("user").toString(), params.get("pass").toString());

                        return git.testAuth();
                    } catch (Exception e) {
                        res.status(400);
                        return e.toString();
                    }
                }, JsonConvert.json());

                get("", (req, res) -> {
                    try {
                        res.type("application/json");
                        res.header("Access-Control-Allow-Origin", "*");
                        res.status(200);

                        if (git.testAuth()) return true;

                        git.auth(); // Proovib sisse logid env muutujatega
                        return git.testAuth(); // Tagastab kas env muutujate login töötas
                    } catch (Exception e) {
                        res.status(400);
                        return e.toString();
                    }
                }, JsonConvert.json());
            });
        });
    }

    public static void stopserver() {
        stop();
    }

    public static void main(String[] args) throws IOException {
        GitWrangler repo = new GitWrangler("./.data/");
        FileWrangler failisüsteem = new FileWrangler("./.data/", "./fileindex");

        start(repo, failisüsteem);
    }
}