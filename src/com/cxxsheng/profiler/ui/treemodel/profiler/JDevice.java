package com.cxxsheng.profiler.ui.treemodel.profiler;

import com.android.ddmlib.IDevice;
import com.cxxsheng.profiler.core.AdbService;
import com.cxxsheng.profiler.utils.UiUtils;
import com.cxxsheng.profiler.utils.exceptions.AdbException;
import org.jetbrains.annotations.NotNull;
import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JDevice extends JNode {


  private static final long serialVersionUID = 7098556283089485729L;




  public JDevice(@NotNull String deviceName){
    this.description = deviceName;
  };

  public static JDevice parse(@NotNull Map map){

    String description = (String)map.get("description");
    boolean status = (boolean)map.get("status");
    JDevice device = new JDevice(description);
    device.setStatus(status);
    List<Map> children = (List<Map>)map.get("children");

    for (Map jProcessMap: children){
      device.add(JProcess.parse(jProcessMap));
    }
    return device;
  }

  @Override
  public Icon getIcon() {
    return UiUtils.openIconTest("device");
  }


  public IDevice getDevice() throws AdbException {
    IDevice device =  AdbService.getActiveDeviceByName(description);
    if (device == null)
      throw new AdbException("no such device " + description);
    return device;
  }
}
