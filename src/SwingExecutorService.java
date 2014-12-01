import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by tlk on 01/12/14.
 */
public class SwingExecutorService extends AbstractExecutorService {

    private EventQueue queue;

    public SwingExecutorService()
    {
        queue = Toolkit.getDefaultToolkit().getSystemEventQueue();
    }

    @Override
    public void shutdown() {
        queue = null;

    }

    @Override
    public List<Runnable> shutdownNow() {
        return new ArrayList<Runnable>();
    }

    @Override
    public boolean isShutdown() {
        return queue == null;
    }

    @Override
    public boolean isTerminated() {
        return queue == null;
    }

    @Override
    public boolean awaitTermination(long l, TimeUnit timeUnit) throws InterruptedException {
        return true;
    }

    @Override
    public void execute(Runnable r) {
        EventQueue.invokeLater(r);
    }
}
