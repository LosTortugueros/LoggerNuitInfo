import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

/**
 * Created by tlk on 01/12/14.
 */
public class GlobalKeyListener implements NativeKeyListener {

    private Sender sender;

    public GlobalKeyListener(Sender sender)
    {
        this.sender = sender;
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {
        this.sender.addKeypress();
    }

}
