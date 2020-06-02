package br.com.petgramapp.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

import br.com.petgramapp.R;
import br.com.petgramapp.activities.ChatJamActivity;
import br.com.petgramapp.activities.StartActivity;
import br.com.petgramapp.helper.ConfiguracaoFirebase;
import br.com.petgramapp.model.Usuario;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private FirebaseFirestore firebaseFirestore;
    @Override
    public void onMessageReceived(@NonNull RemoteMessage notificacao) {


     /*   super.onMessageReceived(notificacao);
        Log.i("Notificacao","Notificação recebida");
        showNotification(notificacao.getNotification().getTitle(),notificacao.getNotification().getBody(),notificacao.getNotification().getImageUrl());
*/

    if (notificacao.getNotification() != null){
        String tituloNotificacao = notificacao.getNotification().getTitle();
        String corpoNotificacao = notificacao.getNotification().getBody();

        enviarNotificacao(tituloNotificacao,corpoNotificacao);


    }

    //mensagens e notificações de conversas
        Map<String,String> data = notificacao.getData();
        if (data == null || data.get("sender") == null) return; // finaliza a aplicação caso nao tenha notificação
        Uri uriSom = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
       // final Intent ii = new Intent(this, ChatActivity.class);
        final Intent ii = new Intent(this, ChatJamActivity.class);
        firebaseFirestore = ConfiguracaoFirebase.getFirebaseFirestore();
        firebaseFirestore.collection("Usuarios")
                .document(data.get("sender"))
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Usuario usuarioSender = documentSnapshot.toObject(Usuario.class);
                        ii.putExtra("users",usuarioSender);
                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0,ii,0);
                        Log.i("UsuarioRec","Token:"+usuarioSender.getToken());
                        Log.i("UsuarioRec","id:"+usuarioSender.getId());
                        Log.i("UsuarioRec","nome:"+usuarioSender.getNomePetUsuario());
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                        String ID_CHANNEL = "notification_channel";
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                            NotificationChannel notificationChannel = new NotificationChannel(ID_CHANNEL,"My Notifications",NotificationManager.IMPORTANCE_DEFAULT);
                            notificationChannel.setDescription("Notificações");
                            notificationChannel.enableLights(true);
                            notificationChannel.setVibrationPattern(new long[]{300,400,500,400,300});
                            notificationChannel.setLightColor(Color.RED);

                            notificationManager.createNotificationChannel(notificationChannel);

                        }

                        NotificationCompat.Builder notificationCompat = new NotificationCompat.Builder(getApplicationContext(), ID_CHANNEL);
                        notificationCompat.setAutoCancel(true);
                        notificationCompat.setSound(uriSom);
                        notificationCompat.setSmallIcon(R.mipmap.launcherpet_round);
                        notificationCompat.setContentTitle(data.get("title"));
                        notificationCompat.setContentText(data.get("body"));
                        notificationCompat.setContentIntent(pendingIntent);

                    notificationManager.notify(1,notificationCompat.build());

                    }
                });

    }

    private void showNotification(String title, String body, Uri imageUrl) {

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "br.com.petgramapp";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,"Notificação",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            notificationChannel.setDescription("PetGram notification");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.setVibrationPattern(new long[] {0,1000});
            notificationChannel.enableLights(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.launcherpet_round)
                .setContentTitle(title)
                .setContentText(body)
                .setContentInfo("Info")
        ;
        Log.i("Notificacao","Notificação louca");

        notificationManager.notify(new Random().nextInt(),notificationBuilder.build());

    }

    private void enviarNotificacao(String titulo,String corpoMensagem){

        //CONFIGURAR CHANNEL
        String CHANNEL_ID = getString(R.string.notificacaomensagem);
        Uri uriSom = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        //CRIAR PENDING INTENT
        Intent intent = new Intent(this, StartActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);

        //CRIAR NOTIFICAÇÃO
        NotificationCompat.Builder notificacaoBuilder = new NotificationCompat.Builder(
                this,CHANNEL_ID
        );

        notificacaoBuilder.setContentTitle(titulo);
        notificacaoBuilder.setContentText(corpoMensagem);
        notificacaoBuilder.setSmallIcon(R.mipmap.launcherpet_round);
        notificacaoBuilder.setSound(uriSom);
        notificacaoBuilder.setAutoCancel(true);
        notificacaoBuilder.setContentIntent(pendingIntent);

        //RECUPERAR O NOTIFICATION MANAGER
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //VERIFICAR A VERSAO DO ANDROID A PARTIR DO OREO PARA CONFIGURAR CANAL DE NOTIFICAÇÃO
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,"Notificacao",NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        //Envia a notificação
        notificationManager.notify(new Random().nextInt(100),notificacaoBuilder.build());



    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);

        //token cdunBWO_Z20:APA91bETAqR1jLy3Srd01M1vFaHBzBfUL5GMKrUD5DZzWC03iHQ1OHbE0ROHOOgOYTp6ZWHUUODzi8fG8EEQRlj4JOcDeM2dyJH1ar3faEH_r1Me028y7soTZEoRJ6IG12lOajABtQTN
        Log.i("onNewToken","onNewToken: "+s);
    }
}
