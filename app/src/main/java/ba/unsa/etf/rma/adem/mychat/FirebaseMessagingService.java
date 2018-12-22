package ba.unsa.etf.rma.adem.mychat;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String from_sender_id = remoteMessage.getData().get("from_sender_id");
        String click_action = remoteMessage.getData().get("click_action");
        String notification_title = remoteMessage.getData().get("title");
        String notification_body = remoteMessage.getData().get("body");

        NotificationCompat.Builder mBuilder=
                new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.mychat)
                    .setContentTitle(notification_title)
                    .setContentText(notification_body);


        Intent resultIntent=new Intent(click_action);
        resultIntent.putExtra("visit_user_id",from_sender_id);

        PendingIntent resultPendingIntent=
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);


        int mNotificationId=(int) System.currentTimeMillis();
        NotificationManager mNotifyMgr=
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId,mBuilder.build());
    }
}
