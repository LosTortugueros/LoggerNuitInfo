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
    private ArrayList<Integer> clicks;
    private ArrayList<Integer> nexts;
    private boolean run;

    public Sender(Fenetre fenetre){
        this.fenetre = fenetre;
        keypress = new ArrayList<Integer>();
        coords = new ArrayList<Integer[]>();
        clicks = new ArrayList<Integer>();
        nexts = new ArrayList<Integer>();
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

                synchronized (clicks)
                {
                    if(clicks.size() != 0)
                    {
                        this.sendClicks();
                    }
                }
                synchronized (nexts)
                {
                    if(nexts.size() !=0)
                    {
                        this.sendNexts();
                    }
                }

            }
        } catch (InterruptedException e)
        {

        }
    }

    private void sendNexts() {
        String json = this.getJsonNexts();
        try {
            this.sendHttpRequest(json);
            this.fenetre.addNNext(this.nexts.size());
            this.nexts.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private synchronized void sendClicks() {
        String json = this.getJsonClicks();

        try {
            this.sendHttpRequest(json);
            this.fenetre.addNClicks(this.clicks.size());
            this.clicks.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void sendParcour() {
        float taille = this.calcTaille();
        long timestamp = new Date().getTime()/1000;
        String json = "{\"source\": \"javalog\", \"time\":"+ timestamp +", \"distance\":"+taille+"}";

        try {
            this.sendHttpRequest(json);
            this.fenetre.addNParcours(taille);
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

    public String getJsonClicks() {
        String ret = "{\"source\":\"javalog\", \"clicks\": [";
        for(Integer i : this.clicks)
        {
            ret += i.toString() +",";
        }
        ret = ret.substring(0, ret.length()-1) + "]}";
        return ret;
    }

    public synchronized void addNext(){
        long timestamp = new Date().getTime()/1000;
        this.nexts.add((int) timestamp);
    }
    public synchronized void addKeypress()
    {
        long timestamp = new Date().getTime()/1000;
        this.keypress.add((int) timestamp);
    }

    public synchronized void addClick()
    {
        long timestamp = new Date().getTime()/1000;
        this.clicks.add((int) timestamp);
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

        String id = this.fenetre.getIdUser();
        if(id == null)
        {
            throw new Exception("bad nom - selectionne le batard !");
        }

        System.out.println("send : " + s);

        String url = "http://etud.insa-toulouse.fr/~livet/ServerLogger/logger.php?user=" + id;

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

    /*private synchronized void sendNextSpotify() throws Exception
    {

        String json = "{\"jsonrpc\": \"2.0\", \"id\": 1, \"method\": \"core.playback.get_state\"}";
        String id = this.fenetre.getIdUser();
        if(id == null)
        {
            throw new Exception("bad nom - selectionne le batard !");
        }

        System.out.println("send : " + s);

        String url = "http://etud.insa-toulouse.fr/~livet/ServerLogger/logger.php?user=" + id;

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
*/

    public String getJsonNexts() {
        String ret = "{\"source\":\"javalog\", \"next\": [";
        for(Integer i : this.nexts)
        {
            ret += i.toString() +",";
        }
        ret = ret.substring(0, ret.length()-1) + "]}";
        return ret;
    }
}
