import org.eclipse.jgit.lib.ProgressMonitor;

public class SimpleMonitor implements ProgressMonitor {
    @Override
    public void start(int totalTasks) {
    }

    @Override
    public void beginTask(String title, int totalWork) {
        System.out.println(title + ".");
    }

    @Override
    public void update(int completed) {
    }

    @Override
    public void endTask() {
    }

    @Override
    public boolean isCancelled() {
        return false;
    }
}
