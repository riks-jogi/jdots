import java.io.IOException;
import static spark.Spark.*;

public class API {
    public static void start(GitWrangler git, FileWrangler file){
        path("/api", () -> {
            path("/files", () -> {
                get("", (req, res) -> {
                    res.type("application/json");
                    res.header("Access-Control-Allow-Origin", "*");
                    res.status(200);
                    return file.getDotfailid();
                }, JsonConvert.json());

                post("", (req, res) -> {
                    file.lisaDotfail(req.params("path"));
                    res.status(200);
                    return "OK";
                });
                delete("", (req, res) -> {
                    file.eemaldaDotfail(req.params("path"));
                    res.status(200);
                    return "OK";
                });
            });
        });
    }

    public static void main(String[] args) throws IOException {
        GitWrangler repo = new GitWrangler("./.data/");
        FileWrangler failisüsteem = new FileWrangler("./.data/", "./fileindex");

        start(repo, failisüsteem);
    }
}