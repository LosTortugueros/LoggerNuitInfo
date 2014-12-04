import com.sun.management.OperatingSystemMXBean;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by tlk on 01/12/14.
 */
public class Fenetre extends JFrame implements WindowListener {

    private JComboBox<String> field_name;
    private JTextField field_num;
    private JTextField field_parcours;
    private JTextField field_clicks;
    private JTextField field_ram;
    private JTextField field_next;
    private Sender sender;

    private String[] usernames = {
            "----",
            "mickael",
            "jeremie",
            "hugo",
            "david",
            "jules",
            "tristan",
            "alfred",
            "sebastien",
            "alexandre",
            "melina",
            "alexis",
            "paul"
    };

    public Fenetre()
    {

        GlobalScreen.getInstance().setEventDispatcher(new SwingExecutorService());

        GridLayout layout = new GridLayout(6,2);
        this.setLayout(layout);
        setTitle("Nuit de l'info - calcul clavier & souris");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(this);
        setVisible(true);

        this.add(new JLabel("Ton identifiant pour les logs :"));
        this.field_name = new JComboBox<String>();
        for(String s : this.usernames)
        {
            this.field_name.addItem(s);
        }
        this.add(this.field_name);

        this.add(new JLabel("Nombre de touches tapées :"));
        this.field_num = new JTextField("0");
        this.field_num.setEditable(false);
        this.add(this.field_num);

        this.add(new JLabel("Nombre de m parcourus :"));
        this.field_parcours = new JTextField("0");
        this.field_parcours.setEditable(false);
        this.add(this.field_parcours);

        this.add(new JLabel("Nombre de clicks :"));
        this.field_clicks = new JTextField("0");
        this.field_clicks.setEditable(false);
        this.add(this.field_clicks);

        this.add(new JLabel("Ram aviable :"));
        this.field_ram = new JTextField("0");
        this.field_ram.setEditable(false);
        this.add(this.field_ram);

        JButton next = new JButton("Next");

        this.add(next);
        this.field_next = new JTextField("0");
        this.field_next.setEditable(false);
        this.add(this.field_next);
        next.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
                logger.setLevel(Level.OFF);
                addNNext(1);
                sender.addNext();
            }
        });
        pack();
    }

    public String getIdUser()
    {
        if(((String) this.field_name.getSelectedItem()).equals(this.usernames[0]))
        {
            return null;
        }
        return (String) this.field_name.getSelectedItem();
    }

    public void addNTouches(int n)
    {
        this.field_num.setText("" + (n + Integer.parseInt(this.field_num.getText())));
    }


    public void addNParcours(float n)
    {
        this.field_parcours.setText("" + (n + Float.parseFloat(this.field_parcours.getText())));
    }

    public void addNClicks(int n)
    {
        this.field_clicks.setText("" + (n + Integer.parseInt(this.field_clicks.getText())));
    }

    public void addRam(Long ram){
        this.field_ram.setText(""+ram.toString());
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
        GlobalScreen.getInstance().addNativeMouseListener(new GlobalMouseListener(sender));
        GlobalScreen.getInstance().addNativeMouseMotionListener(new GlobalMouseListener(sender));
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

    public void addNNext(int size) {
        this.field_next.setText(""+(size+Integer.parseInt(this.field_next.getText())));
    }
}
