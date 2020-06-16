package com.cxxsheng.profiler.ui.treemodel.profiler;

import com.android.ddmlib.Client;
import com.android.ddmlib.IDevice;
import com.cxxsheng.profiler.utils.UiUtils;
import com.cxxsheng.profiler.utils.exceptions.AdbException;
import org.jetbrains.annotations.NotNull;
import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JProcess extends JNode {
  private static final long serialVersionUID = -138400723825715352L;

  @Override
  //fixme
  public String toString() {
    return  description + (status ? " [Tracing]": "");
  }

  @Override
  public Icon getIcon() {
    return UiUtils.openIconTest("process");
  }


  public JProcess(@NotNull String processName, boolean status){
    this.description = processName;
    this.status = status;
  }


  public static JProcess parse(Map map){
    String description = (String)map.get("description");
    boolean status = (boolean)map.get("status");
    JProcess process = new JProcess(description, status);
    List<Map> children = (List<Map>)map.get("children");

    for (Map jTraceMap: children){
      process.add(JTrace.parse(jTraceMap));
    }
    return process;
  }

  public IDevice getDevice()  throws AdbException {
    JDevice deviceNode = (JDevice)parent;
    return deviceNode.getDevice();
  }

  public Client getClient() throws AdbException {
      Client[] clients = getDevice().getClients();
      for (Client client : clients) {
        if (client.getClientData().getClientDescription().equals(description))
          return client;
      }
      throw new AdbException("cannot find process " + description);
    }

}


