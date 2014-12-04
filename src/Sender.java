import java.awt.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.sun.management.OperatingSystemMXBean;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tlk on 01/12/14.
 */
public class Sender extends Thread {

    private Fenetre fenetre;
    private ArrayList<Integer> keypress;
    private ArrayList<Integer[]> coords;
    private ArrayList<Integer> clicks;
    private HashMap<Integer, String> nexts;
    private String currentMusic;
    private ArrayList<Long> ram;
    private boolean run;

    public Sender(Fenetre fenetre){
        this.fenetre = fenetre;
        keypress = new ArrayList<Integer>();
        coords = new ArrayList<Integer[]>();
        clicks = new ArrayList<Integer>();
        nexts = new HashMap<Integer, String>();
        currentMusic = "toto";
        ram = new ArrayList<Long>();
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
                    addMusic(getCurrentTrack());
                    fenetre.setMusic(getCurrentTrack());
                    if(nexts.size() !=0)
                    {
                        this.sendNexts();
                    }
                }

                synchronized (ram)
                {
                    Long ramAviable = ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getFreePhysicalMemorySize();
                    ram.add(ramAviable);
                    if(ram.size() != 0)
                    {
                        this.sendRam();
                    }
                }

            }
        } catch (InterruptedException e)
        {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void addMusic(String currentTrack) {
        if(!currentMusic.equals(currentTrack) && !currentTrack.equals("Toto")){

            currentMusic = currentTrack;
            fenetre.setMusic(currentTrack);

        }
    }

    private synchronized void sendNexts() {
        String json = this.getJsonNexts();
        try {
            this.sendHttpRequest(json);
            this.nexts.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void sendRam(){
        long memorySize = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getFreePhysicalMemorySize();
        String json = this.getJsonRam();
        try {
            this.sendHttpRequest(json);
            this.fenetre.addRam(memorySize);
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

    private synchronized String getJsonRam()
    {
        long timestamp = new Date().getTime()/1000;
        long memorySize = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getFreePhysicalMemorySize();
        String ret = "{\"source\":\"javalog\", \"ram\": "+memorySize+ ", \"time\": "+timestamp+"}";
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
        try {
            this.nexts.put((int)timestamp,getCurrentTrack());
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public synchronized void sendNextSpotify() throws Exception
    {
        String id = this.fenetre.getIdUser();
        if(id == null)
        {
            throw new Exception("bad nom - selectionne le batard !");
        }
        System.out.println("send next");

        String url = "http://10.32.3.190:6680/mopidy/rpc";

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        con.setConnectTimeout(3000);
        con.setReadTimeout(3000);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes("{\"jsonrpc\": \"2.0\", \"id\": 1, \"method\": \"core.playback.next\"}");
        wr.flush();
        wr.close();

        System.out.println("retour : " + con.getResponseCode());

    }


    public synchronized String getCurrentTrack() throws Exception{
        String id = this.fenetre.getIdUser();
        if(id == null)
        {
            throw new Exception("bad nom - selectionne le batard !");
        }
        System.out.println("send next");

        String url = "http://10.32.3.190:6680/mopidy/rpc";

        String name ="a";
        String artist = "b";
        try {
            URL obj = new URL(url);
            HttpURLConnection con = null;
            con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        con.setConnectTimeout(3000);
        con.setReadTimeout(3000);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes("{\"jsonrpc\": \"2.0\", \"id\": 1, \"method\": \"core.playback.get_current_track\"}");
        wr.flush();
        wr.close();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        JSONObject toto = new JSONObject(response.toString());

        System.out.println(toto.toString());
        name = toto.getJSONObject("result").getString("name");
        artist = ((JSONObject)toto.getJSONObject("result").getJSONArray("artists").get(0)).getString("name");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e){
            e.printStackTrace();
        }
        return (artist+"-"+name);
    }


    public String getJsonNexts() {
        String ret = "{\"source\":\"javalog\", \"next\": [";
        String[] prout;
        for(Map.Entry<Integer, String> entry : this.nexts.entrySet()) {
            String name = entry.getValue();
            if(name.contains("-")){
                Integer timestamp = entry.getKey();
                System.out.println(name);
                prout = name.split("-");
                ret += "{\"artist\":\"";
                ret +=prout[0]+"\",\"name\":\"";
                ret += prout[1]+"\",\"timestamp\":\"";
                ret += timestamp+"\"},";
            }


            // do what you have to do here
            // In your case, an other loop.
        }
        ret = ret.substring(0, ret.length()-1) + "]}";
        return ret;
    }
}
