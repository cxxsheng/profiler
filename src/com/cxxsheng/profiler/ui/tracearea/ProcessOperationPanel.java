package com.cxxsheng.profiler.ui.tracearea;

import com.android.ddmlib.Client;
import com.android.ddmlib.ClientData;
import com.android.ddmlib.IDevice;
import com.cxxsheng.profiler.core.ProfilerClient;
import com.cxxsheng.profiler.ui.ContentPanel;
import com.cxxsheng.profiler.ui.TabbedPane;
import com.cxxsheng.profiler.ui.treemodel.profiler.JNode;
import com.cxxsheng.profiler.ui.treemodel.profiler.JProcess;
import com.cxxsheng.profiler.utils.exceptions.AdbException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ProcessOperationPanel extends ContentPanel {
  private final JButton tracingBtn;
  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  volatile boolean isTracing = false;

  private void setBtnText(){
    if (isTracing)
      tracingBtn.setText("Stop Tracing");
    else
      tracingBtn.setText("Start Tracing");
  }

  public ProcessOperationPanel(TabbedPane tabbedPane, JNode node) {
    super(tabbedPane, node);
    isTracing = node.getStatus();
    tracingBtn = new JButton();
    tracingBtn.setSize(400,200);
    setBtnText();
    setLayout(new GridBagLayout());
    tracingBtn.addActionListener(e -> {
      tracingBtn.setEnabled(false);
      JProcess process = (JProcess)node;
      try {

        if (!isTracing)
        {
          startProfiling(process.getDevice(), process.getClient());
          isTracing = true;
        }else
        {
          stopProfiling(process.getDevice(), process.getClient());
          isTracing = false;
        }
      }catch (AdbException | IOException exception){
        JOptionPane.showMessageDialog(
          this,
          exception.getMessage(),
          "Warning",
          JOptionPane.WARNING_MESSAGE
        );
      }finally {
        process.setStatus(isTracing);
        setBtnText();
        tracingBtn.setEnabled(true);
      }
    });
    add(tracingBtn, new GridBagConstraints());
  }



  @Override
  public void loadSettings() {
    //label.setFont(getLabelFont());
  }


  //fixme
  private void startProfiling(@NotNull IDevice device, @NotNull Client client) throws IOException {
    if (!client.isValid())
      return;
      if (client.getClientData().getMethodProfilingStatus() == ClientData.MethodProfilingStatus.OFF)
      {
        LOG.info("start tracing "+ ProfilerClient.Client2String(client));
        client.startMethodTracer();
      }else {
        LOG.error("startProfiling "+"client status is "+ client.getClientData().getMethodProfilingStatus()+", expected is OFF!");
        client.stopMethodTracer();
      }
  }

  private void stopProfiling(@NotNull IDevice device, @NotNull Client client) throws IOException {
    if (!client.isValid())
      return;
    if (client.getClientData().getMethodProfilingStatus() == ClientData.MethodProfilingStatus.TRACER_ON){
        LOG.info("stop tracing " + ProfilerClient.Client2String(client));
        client.stopMethodTracer();
    }else {
        LOG.error("stopProfiling:" +"client status is "+client.getClientData().getMethodProfilingStatus() +", expected is TRACER_ON!");
    }


  }
}
