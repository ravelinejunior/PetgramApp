package br.com.petgramapp.interfaces;

import br.com.petgramapp.model.FirebaseNotification;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface NotificacaoServiceInterface {

    @Headers({
            "Authorization:key=AAAApU3_MIo:APA91bGV0oIq7ZY7LKcSgSILAbGWfmVtyx4iR3rgovvuhfylEDZp_5350d57RRaA3k8SdBSdJmnyyVfMR3KXYaRiFJUNZuhIRn6e_3I8rEKi9f52aFhKDlk7H5l_2cbzwOEpjRz1wIpF",
            "Content-Type:application/json"
    })
    @POST("send")
    Call<FirebaseNotification> salvarNotificacao(@Body FirebaseNotification firebaseNotification);

}
