import java.io.IOException;
import java.util.List;

import static spark.Spark.*;

public class API {
    public static void start(GitWrangler git, FileWrangler file){
        path("/api", () -> {
            path("/files", () -> {
                get("", (req, res) -> {
                    res.type("application/json");
                    return file.getDotfailid();
                }, JsonConvert.json());

                post("", (req, res) -> "Hello Worldpost");
                delete("", (req, res) -> "Hello Worlddeleet");
            });
        });
    }

    public static void main(String[] args) throws IOException {
        GitWrangler repo = new GitWrangler("./.data/");
        FileWrangler failisüsteem = new FileWrangler("./.data/", "./fileindex");

        start(repo, failisüsteem);
    }
}