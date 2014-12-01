import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;
import org.jnativehook.mouse.NativeMouseListener;

/**
 * Created by rusty on 01/12/2014.
 */
public class GlobalMouseListener implements NativeMouseInputListener {

    private Sender sender;

    public GlobalMouseListener(Sender sender) {
        this.sender = sender;
    }

    @Override
    public void nativeMouseClicked(NativeMouseEvent nativeMouseEvent) {
        System.out.println("clicked");
    }

    @Override
    public void nativeMousePressed(NativeMouseEvent nativeMouseEvent) {

    }

    @Override
    public void nativeMouseReleased(NativeMouseEvent nativeMouseEvent) {

    }

    @Override
    public void nativeMouseMoved(NativeMouseEvent nativeMouseEvent) {

        Integer x = 0;
        Integer y = 0;
        if(nativeMouseEvent.getX() > 60000)
        {
            x = nativeMouseEvent.getX() - 65535;
        } else {
            x = nativeMouseEvent.getX();
        }

        if(nativeMouseEvent.getY() > 60000)
        {
            y = nativeMouseEvent.getY() - 65535;
        } else {
            y = nativeMouseEvent.getY();
        }

        Integer[] c = {x, y};
        this.sender.addCoords(c);
    }

    @Override
    public void nativeMouseDragged(NativeMouseEvent nativeMouseEvent) {

    }
}
