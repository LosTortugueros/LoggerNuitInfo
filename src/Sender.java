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
    private boolean run;

    public Sender(Fenetre fenetre){
        this.fenetre = fenetre;
        keypress = new ArrayList<Integer>();
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
            }
        } catch (InterruptedException e)
        {

        }
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

        String url = "http://etud.insa-toulouse.fr/~livet/logger.php?user=" + id;

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(s);
        wr.flush();
        wr.close();

        System.out.println("retour : " + con.getResponseCode());

    }

}
