package com.cxxsheng.profiler.core;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.Client;
import com.android.ddmlib.ClientData;
import com.android.ddmlib.IDevice;
import com.cxxsheng.profiler.utils.exceptions.ProfilerRuntimeException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

public class AdbService {

  private static final Logger LOG = LoggerFactory.getLogger(AdbService.class);
  //fixme in settings
  private static String adbPath = "/usr/local/bin/adb";
  private static Thread zthis;
  volatile private static Map<String, IDevice> active_devices = new HashMap<>();
  public synchronized static void init(@NotNull AdbServiceInitializedListener listener){
    if (zthis==null)
      zthis = new Thread(()->{
        AndroidDebugBridge.initIfNeeded(true);
        AndroidDebugBridge bridge = AndroidDebugBridge.createBridge(adbPath,false);

        if (bridge == null)
          throw new ProfilerRuntimeException("bridge is null");
        while (!bridge.isConnected()){
          try {
            Thread.sleep(200);
          }
          catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        listener.onConnected();
      });
    if (zthis.getState() == Thread.State.TERMINATED || zthis.getState() == Thread.State.NEW){
      zthis.start();
    }
  }


  public interface AdbServiceInitializedListener{
    public void onConnected();
  }
  public static AndroidDebugBridge getBridge() {
    return AndroidDebugBridge.getBridge();
  }


  synchronized public static void terminate(){
    AndroidDebugBridge.disconnectBridge();
    AndroidDebugBridge.terminate();
  }





  private AdbService(){
  }

  public static synchronized void updateDevices(@Nullable IDevice[] devices){
    if (devices == null)
      return;
    active_devices.clear();
    for (IDevice device : devices)
        active_devices.put(device.toString(), device);

  }

  @Nullable
  public static IDevice getActiveDeviceByName(@NotNull String name){
    return active_devices.get(name);
  }



}
