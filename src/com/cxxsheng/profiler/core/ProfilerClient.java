package com.cxxsheng.profiler.core;

import com.android.ddmlib.Client;
import org.jetbrains.annotations.NotNull;

public class ProfilerClient{
  private Client client_;
  private ProfilerClient(@NotNull Client client){
    client_ = client;
  }

  @Override
  @NotNull
  public String toString() {
    return Client2String(client_);
  }

  @NotNull
  public static ProfilerClient[] clients2ProfilerClients(@NotNull Client[] clients){
    ProfilerClient[] profilerClients = new ProfilerClient[clients.length];
    for (int i = 0; i < clients.length; i++){
      profilerClients[i] = new ProfilerClient(clients[i]);
    }
    return profilerClients;
  }

  @NotNull
  public static String Client2String(Client client){
    if (client.isValid()){
      return client.getClientData().getClientDescription()+ " ["+client.getClientData().getPid()+"]";
    }else
      return "";
  }
  public Client getClient() {
    return client_;
  }
}
