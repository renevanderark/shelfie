package nl.shelfiesupport.shelfie;

import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

public class ExportTask extends AsyncTask<String, Integer, String> {
    Responder responder;
    Shelf shelf;

    public ExportTask(Responder responder, Shelf shelf) {
        this.shelf = shelf;
        this.responder = responder;
    }

    private JSONObject buildQuery() throws JSONException {
        JSONObject query = new JSONObject();
        JSONObject data = new JSONObject(shelf.toJSON().toString());
        if(shelf.getExportId() == null) {
            query.put("action", "add");
        } else {
            if(data.has("_id")) { data.remove("_id"); }
            query.put("action", "update");
            query.put("id", shelf.getExportId());
        }
        query.put("data", data);
        return query;
    }

    @Override
    protected String doInBackground(String... params) {
        String response = null;
        HttpURLConnection connection = null;
        InputStream is = null;
        BufferedReader reader = null;


        try {
            URL url = new URL(Remoting.SERVICE_URL);
            String sendData = buildQuery().toString();
            Log.d(Tag.SHELFIE, sendData);
            connection = (HttpURLConnection) url.openConnection();
            Log.d(Tag.SHELFIE_NET, url.toString());
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept-Charset", "utf-8");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setDoOutput(true);
            DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
            dos.write(sendData.getBytes(Charset.forName("UTF-8")));
            dos.flush();
            dos.close();
            is = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String ln;
            while((ln = reader.readLine()) != null) { sb.append(ln); }
            response = sb.toString();
        } catch (IOException e) {
            Log.w(Tag.SHELFIE, "Failed to open service");
        } catch (JSONException e) {
            Log.w(Tag.SHELFIE, "Failed generate JSON");
        } finally {
            if(reader != null) { try { reader.close(); } catch (IOException ignored) { /* ignore */ } }
            if(is != null) { try { is.close(); } catch (IOException ignored) { /* ignore */ } }
            if(connection != null) { connection.disconnect(); }
        }

        return response;
    }

    @Override
    protected void onPostExecute(String response) {
        responder.respondWith(response);
    }
}
