package nl.shelfiesupport.shelfie.faye;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import com.saulpower.fayeclient.FayeClient;
import nl.shelfiesupport.shelfie.MainActivity;
import nl.shelfiesupport.shelfie.Remoting;
import nl.shelfiesupport.shelfie.Tag;
import org.json.JSONObject;
import java.net.URI;

public class WebSocketService extends IntentService implements FayeClient.FayeListener {

    public final String TAG = Tag.SHELFIE_NET;
    private static boolean isUp = false;
    private String mChannel = null;
    private static FayeClient mClient;
    private static boolean connecting = false;

    private static final class WebSocketHandler extends Handler {

    }
    private static final WebSocketHandler mHandler = new WebSocketHandler();

    public WebSocketService() {
        super("WebSocketService");

    }

    @Override
    protected void onHandleIntent(Intent intent) {

        boolean disconnect = intent.getBooleanExtra("disconnect", false);
        if (disconnect) {
            if(mClient != null) {
                mClient.disconnectFromServer();
            }
            return;
        }
        if(isUp || connecting) {
            Log.i(TAG, "service is already up or connecting");
            return;
        }
        connecting = true;

        mChannel = intent.getStringExtra("channel");
        if(mChannel == null) {
            return;
        }
        Log.i(TAG, "Starting Web Socket for channel: " + mChannel);

        String baseUrl = Remoting.SERVICE_URL + "/chans";

        URI uri = URI.create(baseUrl);

        mClient = new FayeClient(mHandler, uri, mChannel);
        mClient.setFayeListener(this);
        mClient.connectToServer(new JSONObject());

    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void connectedToServer() {
        Log.i(TAG, "Connected to Server");
        isUp = true;
        connecting = false;
        Intent intent = new Intent();
        intent.setAction(MainActivity.GROCERY_LIST_ACTION);
        intent.putExtra("CONNECTED", 1);
        sendBroadcast(intent);
    }

    @Override
    public void disconnectedFromServer() {
        Log.i(TAG, "Disonnected from Server");
        isUp = false;
        connecting = false;
        Intent intent = new Intent();
        intent.setAction(MainActivity.GROCERY_LIST_ACTION);
        intent.putExtra("CONNECTED", 0);
        sendBroadcast(intent);
    }

    @Override
    public void subscribedToChannel(String subscription) {
        Log.i(TAG, String.format("Subscribed to channel %s on Faye", subscription));

    }

    @Override
    public void subscriptionFailedWithError(String error) {
        Log.i(TAG, String.format("Subscription failed with error: %s", error));
        isUp = false;
        connecting = false;
        Intent intent = new Intent();
        intent.setAction(MainActivity.GROCERY_LIST_ACTION);
        intent.putExtra("CONNECTED", 0);
        sendBroadcast(intent);

    }

    @Override
    public void messageReceived(JSONObject json) {

        Log.i(TAG, String.format("Received message %s", json.toString()));
        Intent intent = new Intent();
        intent.setAction(MainActivity.GROCERY_LIST_ACTION);
        intent.putExtra("MSG", json.toString());
        sendBroadcast(intent);
    }
}