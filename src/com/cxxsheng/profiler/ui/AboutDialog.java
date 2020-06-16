package com.cxxsheng.profiler.ui;

import com.cxxsheng.profiler.core.Settings;
import com.cxxsheng.profiler.utils.NLS;
import com.cxxsheng.profiler.utils.UiUtils;
import javax.swing.*;
import java.awt.*;

class AboutDialog extends JDialog {
  private static final long serialVersionUID = 5763493590584039096L;

  public AboutDialog() {
    initUI();
  }

  public final void initUI() {
    Font font = new Font("Serif", Font.BOLD, 13);




    JLabel desc = new JLabel("Tool to Trace Java Method");
    desc.setFont(font);
    desc.setAlignmentX(0.5f);

    JLabel name = new JLabel("Author: cxxsheng@gmail.com", SwingConstants.CENTER);
    name.setFont(font);
    name.setAlignmentX(0.5f);

    JLabel version = new JLabel("profiler version: " + Settings.getVersion());
    version.setFont(font);
    version.setAlignmentX(0.5f);

    String javaVm = System.getProperty("java.vm.name");
    String javaVer = System.getProperty("java.version");

    javaVm = javaVm == null ? "" : javaVm;

    JLabel javaVmLabel = new JLabel("Java VM: " + javaVm);
    javaVmLabel.setFont(font);
    javaVmLabel.setAlignmentX(0.5f);

    javaVer = javaVer == null ? "" : javaVer;
    JLabel javaVerLabel = new JLabel("Java version: " + javaVer);
    javaVerLabel.setFont(font);
    javaVerLabel.setAlignmentX(0.5f);

    JPanel textPane = new JPanel();
    textPane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    textPane.setLayout(new BoxLayout(textPane, BoxLayout.PAGE_AXIS));
    textPane.add(Box.createRigidArea(new Dimension(0, 10)));
    textPane.add(desc);
    textPane.add(Box.createRigidArea(new Dimension(0, 10)));
    textPane.add(name);
    textPane.add(Box.createRigidArea(new Dimension(0, 10)));
    textPane.add(version);
    textPane.add(Box.createRigidArea(new Dimension(0, 20)));
    textPane.add(javaVmLabel);
    textPane.add(javaVerLabel);
    textPane.add(Box.createRigidArea(new Dimension(0, 20)));

    JButton close = new JButton(NLS.str("tabs.close"));
    close.addActionListener(event -> dispose());
    close.setAlignmentX(0.5f);

    Container contentPane = getContentPane();
    contentPane.add(textPane, BorderLayout.CENTER);
    contentPane.add(close, BorderLayout.PAGE_END);

    UiUtils.setWindowIcons(this);

    setModalityType(ModalityType.APPLICATION_MODAL);

    setTitle("About Profiler");
    pack();
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);
  }
}
