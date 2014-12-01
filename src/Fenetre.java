import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by tlk on 01/12/14.
 */
public class Fenetre extends JFrame implements WindowListener {

    private JFormattedTextField field_id;
    private JTextField field_num;
    private Sender sender;

    public Fenetre()
    {
        GlobalScreen.getInstance().setEventDispatcher(new SwingExecutorService());

        GridLayout layout = new GridLayout(2,2);
        this.setLayout(layout);
        setTitle("Nuit de l'info - calcul clavier & souris");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(this);
        setVisible(true);

        this.add(new JLabel("Ton identifiant pour les logs :"));
        this.field_id = new JFormattedTextField(new DecimalFormat("###"));
        this.add(this.field_id);

        this.add(new JLabel("Nombre de touches tapées :"));
        this.field_num = new JTextField("0");
        this.field_num.setEditable(false);
        this.add(this.field_num);

        pack();
    }

    public int getIdUser()
    {
        return Integer.parseInt(this.field_id.getText());
    }

    public void addNTouches(int n)
    {
        this.field_num.setText("" + (n + Integer.parseInt(this.field_num.getText())));
    }

    public static void main(String[] str)
    {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Fenetre();
            }
        });
    }

    @Override
    public void windowOpened(WindowEvent windowEvent) {
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            System.err.println("Impossible d'initialiser la lib ...");
            e.printStackTrace();
            System.exit(1);
        }

        sender = new Sender(this);
        sender.start();

        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        GlobalScreen.getInstance().addNativeKeyListener(new GlobalKeyListener(sender));
    }

    @Override
    public void windowClosing(WindowEvent windowEvent) {

    }

    @Override
    public void windowClosed(WindowEvent windowEvent) {
        this.sender.setRun(false);
        GlobalScreen.unregisterNativeHook();
        System.runFinalization();
        System.exit(0);
    }

    @Override
    public void windowIconified(WindowEvent windowEvent) {

    }

    @Override
    public void windowDeiconified(WindowEvent windowEvent) {

    }

    @Override
    public void windowActivated(WindowEvent windowEvent) {

    }

    @Override
    public void windowDeactivated(WindowEvent windowEvent) {

    }
}
