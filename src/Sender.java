import java.awt.*;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by tlk on 01/12/14.
 */
public class Sender extends Thread {

    private Fenetre fenetre;
    private ArrayList<Integer> keypress;
    private ArrayList<Integer[]> coords;
    private boolean run;

    public Sender(Fenetre fenetre){
        this.fenetre = fenetre;
        keypress = new ArrayList<Integer>();
        coords = new ArrayList<Integer[]>();
        this.run = true;
    }

    public void run()
    {
        try {
            while (this.run) {
                this.sleep(5000);
                synchronized (keypress) {
                    if(keypress.size() != 0)
                    {
                        this.sendKeypress();
                    }
                }

                synchronized (coords)
                {
                    if(coords.size() != 0)
                    {
                        this.sendParcour();
                    }
                }

            }
        } catch (InterruptedException e)
        {

        }
    }

    private synchronized void sendParcour() {
        float taille = this.calcTaille();
        long timestamp = new Date().getTime()/1000;
        String json = "{\"source\": \"javalog\", \"time\":"+ timestamp +", \"distance\":"+taille+"}";

        try {
            this.sendHttpRequest(json);
            this.coords.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private synchronized float calcTaille() {
        float ret = 0;
        for(int i=0; i<this.coords.size()-1; i++)
        {
            Integer x1 = this.coords.get(i)[0];
            Integer x2 = this.coords.get(i+1)[0];
            Integer y1 = this.coords.get(i)[1];
            Integer y2 = this.coords.get(i+1)[1];
            ret += Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
        }
        ret /= Toolkit.getDefaultToolkit().getScreenResolution();
        ret *= 0.0254;
        return ret;
    }

    private synchronized void sendKeypress()
    {
        String json = this.getJsonKeypress();

        try {
            this.sendHttpRequest(json);
            this.fenetre.addNTouches(this.keypress.size());
            this.keypress.clear();
            System.out.println(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized String getJsonKeypress()
    {
        String ret = "{\"source\":\"javalog\", \"keys\": [";
        for(Integer i : this.keypress)
        {
            ret += i.toString() +",";
        }
        ret = ret.substring(0, ret.length()-1) + "]}";
        return ret;
    }

    public synchronized void addKeypress()
    {
        long timestamp = new Date().getTime()/1000;
        this.keypress.add((int) timestamp);
    }

    public synchronized void addCoords(Integer[] c)
    {
        this.coords.add(c);
    }

    public synchronized void setRun(boolean a)
    {
        this.run = a;
    }

    private synchronized void sendHttpRequest(String s) throws Exception
    {

        int id = this.fenetre.getIdUser();
        if(id == 0)
        {
            throw new Exception("bad id !");
        }

        System.out.println(s);

        String url = "http://etud.insa-toulouse.fr/~livet/logger.php?user=" + id;

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        con.setConnectTimeout(3000);
        con.setReadTimeout(3000);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(s);
        wr.flush();
        wr.close();

        System.out.println("retour : " + con.getResponseCode());

    }

}
