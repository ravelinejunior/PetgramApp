package br.com.petgramapp.utils;

import br.com.petgramapp.interfaces.NotificacaoServiceInterface;
import br.com.petgramapp.retrofit.RetrofitMessage;


public class MessageUtils  {

  public static NotificacaoServiceInterface getNotificacao(){
        return RetrofitMessage.getMessageRetrofit().create(NotificacaoServiceInterface.class);
  }
}
