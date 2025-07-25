package id.go.fimo;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import id.go.fimo.util.NotificationUtil;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class FcmService extends FirebaseMessagingService {

    private static final String TAG = "MyFcmListenerService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String from = remoteMessage.getFrom();
        Map bundle = remoteMessage.getData();

        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + remoteMessage);

        String title = (String) bundle.get("title");
        Boolean isBackground = Boolean.valueOf((String) bundle.get("is_background"));
        String flag = (String) bundle.get("flag");
        String data = (String) bundle.get("data");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "title: " + title);
        Log.d(TAG, "isBackground: " + isBackground);
        Log.d(TAG, "flag: " + flag);
        Log.d(TAG, "data: " + data);

        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }

        try {
            JSONObject datObj = new JSONObject(data);

            String message = datObj.getString("message");
            String createdAt = datObj.getString("created_at");

            Intent resultIntent = new Intent(getApplicationContext(), RecordActivity.class);
                if (!App.getInstance().getPreferenceManager().getLoginFlag(getApplicationContext())) {
                    resultIntent = new Intent(getApplicationContext(), WelcomeActivity.class);
                }
                showNotificationMessage(getApplicationContext(), title, message, createdAt, resultIntent);

        } catch (JSONException e) {
            Log.e(TAG, "json parsing error: " + e.getMessage());
            Toast.makeText(getApplicationContext(), "Json parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Processing chat room push message
     * this message will be broadcasts to all the activities registered
     */
    private void showNotification(String title, boolean isBackground, String message, String createdAt, Class clazz) {
        if (!isBackground) {
            Intent resultIntent = new Intent(getApplicationContext(), clazz);
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                resultIntent = new Intent(getApplicationContext(), WelcomeActivity.class);
            }
            showNotificationMessage(getApplicationContext(), title, message, createdAt, resultIntent);
        } else {
            // the push notification is silent, may be other operations needed
            // like inserting it in to SQLite
        }
        Intent intent = new Intent(INTENT_FILTER);
        sendBroadcast(intent);
    }


    public static final String INTENT_FILTER = "INTENT_FILTER";

    private NotificationUtil notificationUtil;

    /**
     * Showing notification with text only
     */
    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
        notificationUtil = new NotificationUtil(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtil.showNotificationMessage(title, message, timeStamp, intent);
    }
}
