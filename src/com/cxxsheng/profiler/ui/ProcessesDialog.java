package com.cxxsheng.profiler.ui;

import com.android.ddmlib.Client;
import com.android.ddmlib.IDevice;
import javax.swing.*;

public class ProcessesDialog extends JDialog {
  private DefaultListModel<Client> processes = new DefaultListModel<>();
  private IDevice device_;
  public ProcessesDialog(IDevice device){
      this.device_ = device;
      if (device!=null){
        Client[] clients = device.getClients();
        for (Client client : clients){
            processes.addElement(client);
        }
      }
  }
}
