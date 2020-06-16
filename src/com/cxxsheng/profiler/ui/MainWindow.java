package com.cxxsheng.profiler.ui;

import com.alibaba.fastjson.JSONObject;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.Client;
import com.android.ddmlib.ClientData;
import com.android.ddmlib.IDevice;
import com.android.tools.profilers.cpu.CpuCapture;
import com.cxxsheng.profiler.core.AdbService;
import com.cxxsheng.profiler.core.CpuCaptureParser;
import com.cxxsheng.profiler.core.ProfilerClient;
import com.cxxsheng.profiler.settings.ProfilerSettings;
import com.cxxsheng.profiler.ui.tracearea.TraceContentPanel;
import com.cxxsheng.profiler.ui.treemodel.profiler.JDevice;
import com.cxxsheng.profiler.ui.treemodel.profiler.JNode;
import com.cxxsheng.profiler.ui.treemodel.profiler.JProcess;
import com.cxxsheng.profiler.ui.treemodel.profiler.JRoot;
import com.cxxsheng.profiler.ui.treemodel.profiler.JTrace;
import com.cxxsheng.profiler.utils.NLS;
import com.cxxsheng.profiler.utils.UiUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import static javax.swing.KeyStroke.getKeyStroke;

public class MainWindow extends JFrame {
  private static final long serialVersionUID = 3310426424083909002L;

  private static final Logger LOG = LoggerFactory.getLogger(MainWindow.class);

  private static final String DEFAULT_TITLE = "Profiler";

  private static final double BORDER_RATIO = 0.15;
  private static final double WINDOW_RATIO = 1 - BORDER_RATIO * 2;
  private static final double SPLIT_PANE_RESIZE_WEIGHT = 0.15;

  private static final ImageIcon ICON_OPEN = UiUtils.openIcon("folder");
  private static final ImageIcon ICON_SAVE_ALL = UiUtils.openIcon("disk_multiple");
  private static final ImageIcon ICON_EXPORT = UiUtils.openIcon("database_save");
  private static final ImageIcon ICON_CLOSE = UiUtils.openIcon("cross");
  private static final ImageIcon ICON_SYNC = UiUtils.openIcon("sync");
  private static final ImageIcon ICON_FLAT_PKG = UiUtils.openIcon("empty_logical_package_obj");
  private static final ImageIcon ICON_SEARCH = UiUtils.openIcon("wand");
  private static final ImageIcon ICON_FIND = UiUtils.openIcon("magnifier");
  private static final ImageIcon ICON_BACK = UiUtils.openIcon("icon_back");
  private static final ImageIcon ICON_FORWARD = UiUtils.openIcon("icon_forward");
  private static final ImageIcon ICON_PREF = UiUtils.openIcon("wrench");
  private static final ImageIcon ICON_DEOBF = UiUtils.openIcon("lock_edit");
  private static final ImageIcon ICON_LOG = UiUtils.openIcon("report");
  private static final ImageIcon ICON_JADX = UiUtils.openIcon("jadx-logo");

  private transient JComboBox devicesCombo;
  private transient Action profilingBution;


  private JPanel mainPanel;
  private JSplitPane splitPane;

  private JTree tree;
  private DefaultTreeModel treeModel;
  //private JRoot treeRoot;
  private JRoot testTreeRoot;
  private TabbedPane tabbedPane;
  private transient boolean treeReloading;

  private Thread initAdbThread;
  private void closeWindow() {
    //if (!ensureProjectIsSaved()) {
    //  return;
    //}
    //settings.setTreeWidth(splitPane.getDividerLocation());
    //settings.saveWindowPos(this);
    //settings.setMainWindowExtendedState(getExtendedState());
    //cancelBackgroundJobs();
    dispose();

    //FileUtils.deleteTempRootDir();
    System.exit(0);
  }

  private void initMenuAndToolbar() {
    Action openAction = new AbstractAction(NLS.str("file.open_action"), ICON_OPEN) {
      @Override
      public void actionPerformed(ActionEvent e) {
          try {
            openFileOrProject();
          }
          catch (IOException ex) {
            showErrorDialog(ex.getMessage());
          }
      }
    };
    openAction.putValue(Action.SHORT_DESCRIPTION, NLS.str("file.open_action"));
    openAction.putValue(Action.ACCELERATOR_KEY, getKeyStroke(KeyEvent.VK_O, UiUtils.ctrlButton()));


    //fixme
    Action saveAction = new AbstractAction("Export trace", ICON_EXPORT) {
      @Override
      public void actionPerformed(ActionEvent e) {
        saveRecentTrace();
      }
    };
    //fixme
    saveAction.putValue(Action.SHORT_DESCRIPTION,"Export trace");
    saveAction.putValue(Action.ACCELERATOR_KEY, getKeyStroke(KeyEvent.VK_E, UiUtils.ctrlButton()));


    Action saveAllAction = new AbstractAction(NLS.str("file.save_project"), ICON_SAVE_ALL) {
      @Override
      public void actionPerformed(ActionEvent e) {
       saveAll();
      }
    };
    saveAllAction.putValue(Action.SHORT_DESCRIPTION, NLS.str("file.save_project"));
    saveAllAction.putValue(Action.ACCELERATOR_KEY, getKeyStroke(KeyEvent.VK_S, UiUtils.ctrlButton()));


    Action prefsAction = new AbstractAction(NLS.str("menu.preferences"), ICON_PREF) {
      @Override
      public void actionPerformed(ActionEvent e) {
        new ProfilerSettingsWindow(MainWindow.this, new ProfilerSettings()).setVisible(true);
      }
    };
    prefsAction.putValue(Action.SHORT_DESCRIPTION, NLS.str("menu.preferences"));
    prefsAction.putValue(Action.ACCELERATOR_KEY, getKeyStroke(KeyEvent.VK_P,
                                                              UiUtils.ctrlButton() | KeyEvent.SHIFT_DOWN_MASK));

    Action exitAction = new AbstractAction(NLS.str("file.exit"), ICON_CLOSE) {
      @Override
      public void actionPerformed(ActionEvent e) {
        closeWindow();
      }
    };

    //isFlattenPackage = settings.isFlattenPackage();
    //flatPkgMenuItem = new JCheckBoxMenuItem(NLS.str("menu.flatten"), ICON_FLAT_PKG);
    //flatPkgMenuItem.setState(isFlattenPackage);

    JCheckBoxMenuItem heapUsageBarMenuItem = new JCheckBoxMenuItem(NLS.str("menu.heapUsageBar"));
    //heapUsageBarMenuItem.setState(settings.isShowHeapUsageBar());
    heapUsageBarMenuItem.addActionListener(event -> {
      //settings.setShowHeapUsageBar(!settings.isShowHeapUsageBar());
      //heapUsageBar.setVisible(settings.isShowHeapUsageBar());
    });

    Action syncAction = new AbstractAction(NLS.str("menu.sync"), ICON_SYNC) {
      @Override
      public void actionPerformed(ActionEvent e) {
        //syncWithEditor();
      }
    };
    syncAction.putValue(Action.SHORT_DESCRIPTION, NLS.str("menu.sync"));
    syncAction.putValue(Action.ACCELERATOR_KEY, getKeyStroke(KeyEvent.VK_T, UiUtils.ctrlButton()));

    Action textSearchAction = new AbstractAction(NLS.str("menu.text_search"), ICON_FIND) {
      @Override
      public void actionPerformed(ActionEvent e) {
        searchRecentTrace();
      }
    };
    textSearchAction.putValue(Action.SHORT_DESCRIPTION, NLS.str("menu.text_search"));
    textSearchAction.putValue(Action.ACCELERATOR_KEY, getKeyStroke(KeyEvent.VK_F, UiUtils.ctrlButton()));

    Action logAction = new AbstractAction(NLS.str("menu.log"), ICON_LOG) {
      @Override
      public void actionPerformed(ActionEvent e) {
        //new LogViewer(MainWindow.this).setVisible(true);
      }
    };
    logAction.putValue(Action.SHORT_DESCRIPTION, NLS.str("menu.log"));
    logAction.putValue(Action.ACCELERATOR_KEY, getKeyStroke(KeyEvent.VK_L,
                                                            UiUtils.ctrlButton() | KeyEvent.SHIFT_DOWN_MASK));

    Action aboutAction = new AbstractAction(NLS.str("menu.about")) {
      @Override
      public void actionPerformed(ActionEvent e) {
        new AboutDialog().setVisible(true);
      }
    };

    Action backAction = new AbstractAction(NLS.str("nav.back"), ICON_BACK) {
      @Override
      public void actionPerformed(ActionEvent e) {
        //tabbedPane.navBack();
      }
    };
    backAction.putValue(Action.SHORT_DESCRIPTION, NLS.str("nav.back"));
    backAction.putValue(Action.ACCELERATOR_KEY, getKeyStroke(KeyEvent.VK_LEFT,
                                                             UiUtils.ctrlButton() | KeyEvent.ALT_DOWN_MASK));

    Action forwardAction = new AbstractAction(NLS.str("nav.forward"), ICON_FORWARD) {
      @Override
      public void actionPerformed(ActionEvent e) {
        //tabbedPane.navForward();
      }
    };
    forwardAction.putValue(Action.SHORT_DESCRIPTION, NLS.str("nav.forward"));
    forwardAction.putValue(Action.ACCELERATOR_KEY, getKeyStroke(KeyEvent.VK_RIGHT,
                                                                UiUtils.ctrlButton() | KeyEvent.ALT_DOWN_MASK));

    initDevicesCombo();


    JMenu file = new JMenu(NLS.str("menu.file"));
    file.setMnemonic(KeyEvent.VK_F);
    file.add(openAction);
    file.addSeparator();
    file.add(saveAction);
    file.add(saveAllAction);
    file.addSeparator();
    file.add(prefsAction);
    file.addSeparator();
    file.add(exitAction);

    JMenu view = new JMenu(NLS.str("menu.view"));
    view.setMnemonic(KeyEvent.VK_V);
    //view.add(flatPkgMenuItem);
    view.add(syncAction);
    view.add(heapUsageBarMenuItem);

    JMenu nav = new JMenu(NLS.str("menu.navigation"));
    nav.setMnemonic(KeyEvent.VK_N);
    nav.add(textSearchAction);
    nav.addSeparator();
    nav.add(backAction);
    nav.add(forwardAction);

    JMenu tools = new JMenu(NLS.str("menu.tools"));
    tools.setMnemonic(KeyEvent.VK_T);
    //tools.add(deobfMenuItem);
    tools.add(logAction);

    JMenu help = new JMenu(NLS.str("menu.help"));
    help.setMnemonic(KeyEvent.VK_H);
    help.add(aboutAction);

    JMenuBar menuBar = new JMenuBar();
    menuBar.add(file);
    menuBar.add(view);
    menuBar.add(nav);
    menuBar.add(tools);
    menuBar.add(help);
    setJMenuBar(menuBar);
    //
    //flatPkgButton = new JToggleButton(ICON_FLAT_PKG);
    //flatPkgButton.setSelected(isFlattenPackage);
    //ActionListener flatPkgAction = e -> toggleFlattenPackage();
    //flatPkgMenuItem.addActionListener(flatPkgAction);
    //flatPkgButton.addActionListener(flatPkgAction);
    //flatPkgButton.setToolTipText(NLS.str("menu.flatten"));
    //
    //updateLink = new Link("", JadxUpdate.JADX_RELEASES_URL);
    //updateLink.setVisible(false);

    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);
    toolbar.add(openAction);
    toolbar.add(saveAction);
    toolbar.add(saveAllAction);
    toolbar.addSeparator();
    toolbar.add(syncAction);
    //toolbar.add(flatPkgButton);
    toolbar.addSeparator();
    toolbar.add(textSearchAction);
    toolbar.addSeparator();
    toolbar.add(backAction);
    toolbar.add(forwardAction);
    toolbar.addSeparator();
    //toolbar.add(deobfToggleBtn);
    toolbar.addSeparator();
    toolbar.add(logAction);
    toolbar.addSeparator();
    toolbar.add(prefsAction);
    toolbar.addSeparator();
    toolbar.add(devicesCombo);

    toolbar.add(Box.createHorizontalGlue());
    //toolbar.add(updateLink);

    mainPanel.add(toolbar, BorderLayout.NORTH);
  }

  private void saveAll() {
    JFileChooser fileChooser = new JFileChooser();

    //fixme and checkme
    String[] exts = { "profiler" };
    String description = "supported files: " + Arrays.toString(exts).replace('[', '(').replace(']', ')');
    fileChooser.setFileFilter(new FileNameExtensionFilter(description, exts));
    //fixme
    fileChooser.setToolTipText(NLS.str("file.save_project"));
    int ret = fileChooser.showSaveDialog(mainPanel);
    if (ret == JFileChooser.APPROVE_OPTION){
      Path path = fileChooser.getSelectedFile().toPath();
      if (!path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith("profiler")) {
        path = path.resolveSibling(path.getFileName() + "." + "profiler");
      }
      if (Files.exists(path)) {
        int res = JOptionPane.showConfirmDialog(
          this,
          NLS.str("confirm.save_as_message", path.getFileName()),
          NLS.str("confirm.save_as_title"),
          JOptionPane.YES_NO_OPTION);
        if (res == JOptionPane.NO_OPTION) {
          return;
        }
      }
      try {
        byte[] res = JSONObject.toJSONBytes(testTreeRoot.getMap());
        FileOutputStream fos = new FileOutputStream(path.toFile());
        fos.write(res);
        fos.close();
        //fixme
        showInfoDialog("保存成功");
      } catch (IOException e) {
        showErrorDialog(e.getMessage());
      }
    }

  }


  private void searchRecentTrace(){
    Component current = tabbedPane.getSelectedComponent();
    if (current instanceof  TraceContentPanel){
      //to show search dialog
      ((TraceContentPanel)current).setSearchTextVisible(true);
      SwingUtilities.invokeLater(((TraceContentPanel)current)::updateUI);
    }else if (current == null){
      //fixme
      showInfoDialog("请先打开trace文件");
    }else {
      showInfoDialog("请选择你要搜索的trace窗口");
    }
  }

  private void saveRecentTrace() {
    Component current = tabbedPane.getSelectedComponent();
    if (current instanceof TraceContentPanel){
      JTrace trace = (JTrace)((TraceContentPanel)current).getNode();
      if (trace == null)
        return;
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setAcceptAllFileFilterUsed(true);
      String[] exts = { "trace" };
      String description = "supported files: " + Arrays.toString(exts).replace('[', '(').replace(']', ')');
      fileChooser.setFileFilter(new FileNameExtensionFilter(description, exts));
      //fixme and checkme
      fileChooser.setToolTipText(NLS.str("file.save_project"));

      int ret = fileChooser.showSaveDialog(mainPanel);
      if (ret == JFileChooser.APPROVE_OPTION) {
        Path path = fileChooser.getSelectedFile().toPath();
        if (!path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith("trace")) {
          path = path.resolveSibling(path.getFileName() + "." + "trace");
        }
        if (Files.exists(path)) {
          int res = JOptionPane.showConfirmDialog(
            this,
            NLS.str("confirm.save_as_message", path.getFileName()),
            NLS.str("confirm.save_as_title"),
            JOptionPane.YES_NO_OPTION);
          if (res == JOptionPane.NO_OPTION) {
            return;
          }
        }
        if (trace.saveFile(path))
          showInfoDialog("保存成功");
        else
          showErrorDialog("保存失败");

      }
    }else if (current == null){
      //fixme
      showInfoDialog("请先打开trace文件");
    }else {
      showInfoDialog("请选择你要保存的trace窗口");
    }
  }

  private void openTraceFile(Path path) throws IOException {
    CpuCaptureParser parser = new CpuCaptureParser();
    CpuCapture capture = parser.parse(path);
    if (capture != null)
      //fixme
      updateTracesTree("TraceFile", "unknown", ClientData.MethodProfilingStatus.UNKNOWN, parser.getDescription(), parser, capture);
    else
      showErrorDialog("parse trace file failed!");
  }

  private void openProject(Path path) {
    try{
//    FileInputStream fis = new FileInputStream(path.toFile());
//    byte[] bs  = fis.readAllBytes();
      byte[] bs = Files.readAllBytes(path);
      Map map = (Map)JSONObject.parse(bs);

//      fis.close();
      testTreeRoot = JRoot.parse(map);
      //testTreeRoot = gson.fromJson(new String(bs),testTreeRoot.getClass());
      //fixme 确认dialog没写
      treeModel.setRoot(testTreeRoot);
      SwingUtilities.invokeLater(()-> tree.updateUI());
    }catch (IOException e){
      e.printStackTrace();
      showErrorDialog(e.getMessage());
    }
  }

  private void openFileOrProject() throws IOException {

    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setAcceptAllFileFilterUsed(true);
    String[] exts = { "trace", "profiler"};
    String description = "supported files: " + Arrays.toString(exts).replace('[', '(').replace(']', ')');
    fileChooser.setFileFilter(new FileNameExtensionFilter(description, exts));
    fileChooser.setToolTipText(NLS.str("file.open_action"));

    int ret = fileChooser.showDialog(mainPanel, NLS.str("file.open_title"));
    if (ret == JFileChooser.APPROVE_OPTION) {
       Path path = fileChooser.getSelectedFile().toPath();
       if (path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".profiler")){
          openProject(path);
       }else {
         openTraceFile(path);
       }
    }
  }

  synchronized private void showErrorDialog(String msg){
    JOptionPane.showMessageDialog(
      this,
       msg,
      "Warning",
      JOptionPane.ERROR_MESSAGE
    );
  }

  synchronized private void showInfoDialog(String msg){
    JOptionPane.showMessageDialog(
      this,
      msg,
      "Warning",
      JOptionPane.INFORMATION_MESSAGE
    );
  }

  @Nullable
  private JNode getJNodeUnderMouse(MouseEvent mouseEvent) {
    TreePath path = tree.getPathForLocation(mouseEvent.getX(), mouseEvent.getY());
    if (path != null) {
      Object obj = path.getLastPathComponent();
      if (obj instanceof JNode) {
        return (JNode) obj;
      }
    }
    return null;
  }

  private void nodeClickAction(@Nullable Object obj) {
    try {
      if (obj == null) {
        return;
      }

      if (obj instanceof JProcess){
        tabbedPane.showProcess((JProcess)obj);
      }else if (obj instanceof JTrace){
        tabbedPane.showTrace((JTrace)obj);
      }else if (obj instanceof JRoot)
      {
        if (!((JRoot)obj).getStatus())
          openFileOrProject();
      }
    } catch (Exception e) {
      LOG.error("Content loading error", e);
    }
  }

  //unused
  private void treeRightClickAction(MouseEvent e) {

  }

  private void initUI() {
    setMinimumSize(new Dimension(200, 150));
    mainPanel = new JPanel(new BorderLayout());
    splitPane = new JSplitPane();
    splitPane.setResizeWeight(SPLIT_PANE_RESIZE_WEIGHT);
    mainPanel.add(splitPane);



    //DefaultMutableTreeNode treeRootNode = new DefaultMutableTreeNode(NLS.str("msg.open_file"));
    //fixme
    testTreeRoot= new JRoot("打开trace文件");
    treeModel = new DefaultTreeModel(testTreeRoot);
    tree = new JTree(treeModel);
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
          nodeClickAction(getJNodeUnderMouse(e));
        } else if (SwingUtilities.isRightMouseButton(e)) {
          treeRightClickAction(e);
        }
      }
    });
    tree.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          //nodeClickAction(tree.getLastSelectedPathComponent());
        }
      }
    });
    tree.setCellRenderer(new DefaultTreeCellRenderer() {
      @Override
      public Component getTreeCellRendererComponent(JTree tree,
                                                    Object value, boolean selected, boolean expanded,
                                                    boolean isLeaf, int row, boolean focused) {
        Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, isLeaf, row, focused);
        if (value instanceof JNode) {
          setIcon(((JNode) value).getIcon());
        }else {
          setIcon(UiUtils.openIcon("package"));
        }
        return c;
      }
    });
    tree.addTreeWillExpandListener(new TreeWillExpandListener() {
      @Override
      public void treeWillExpand(TreeExpansionEvent event) {
      }

      @Override
      public void treeWillCollapse(TreeExpansionEvent event) {
        if (!treeReloading) {
          //project.removeTreeExpansion(getPathExpansion(event.getPath()));
          //update();
        }
      }
    });

   // progressPane = new ProgressPanel(this, true);

    JPanel leftPane = new JPanel(new BorderLayout());
    JScrollPane treeScrollPane = new JScrollPane(tree);
    treeScrollPane.setMinimumSize(new Dimension(100, 150));

    leftPane.add(treeScrollPane, BorderLayout.CENTER);
    //leftPane.add(progressPane, BorderLayout.PAGE_END);
    splitPane.setLeftComponent(leftPane);

    tabbedPane = new TabbedPane(this);
    tabbedPane.setMinimumSize(new Dimension(150, 150));
    splitPane.setRightComponent(tabbedPane);

    //new DropTarget(this, DnDConstants.ACTION_COPY, new MainDropTarget(this));


    setContentPane(mainPanel);
    setTitle(DEFAULT_TITLE);
  }


  public MainWindow(){
    initUI();
    initMenuAndToolbar();
    loadSettings();
  }

  public void loadSettings() {
    //Font font = settings.getFont();
    //Font largerFont = font.deriveFont(font.getSize() + 2.f);
    //
    //setFont(largerFont);
    //setEditorTheme(settings.getEditorThemePath());
    //tree.setFont(largerFont);
    //tree.setRowHeight(-1);
    //
    //tabbedPane.loadSettings();
  }
  public void init(){
    pack();
    //setLocationAndPosition();
    //splitPane.setDividerLocation(settings.getTreeWidth());
    //heapUsageBar.setVisible(settings.isShowHeapUsageBar());
    setVisible(true);
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        closeWindow();
      }
    });

    AdbService.init(() -> {
        //updateDevices();
        AndroidDebugBridge.addDeviceChangeListener(new AndroidDebugBridge.IDeviceChangeListener() {
          @Override
          public void deviceConnected(IDevice iDevice) {
            updateDevices();
          }

          @Override
          public void deviceDisconnected(IDevice iDevice) {
            updateDevices();
          }

          @Override
          public void deviceChanged(IDevice iDevice, int i) {
            updateDevices();
          }
        });

        ClientData.setMethodProfilingHandler(new ClientData.IMethodProfilingHandler() {
            @Override
            public void onSuccess(String s, Client client) {
                LOG.error("setMethodProfilingHandler", "unhandled data: "+s);
            }

            @Override
            public void onSuccess(byte[] bytes, Client client) {
              new Thread(() -> {
                LOG.info("getTraceInfo onSuccess \n" +
                         "*********************\n"+new String(bytes)+"\n*********************");
                CpuCaptureParser parser = new CpuCaptureParser();
                String tmpName = System.currentTimeMillis()+"";
                try {
                  CpuCapture cpuCapture = parser.parse(bytes, tmpName);
                  if (cpuCapture != null)
                    updateTracesTree(client.getDevice().toString(), client.getClientData().getClientDescription(),
                                     client.getClientData().getMethodProfilingStatus(), tmpName, parser, cpuCapture);
                  else
                    showErrorDialog("parse trace failed");
                }catch (IOException e){
                  showErrorDialog("parse trace failed "+ e.getMessage());
                }
              }).start();
            }

            @Override
            public void onStartFailure(Client client, String s) {
              LOG.error("setMethodProfilingHandler", "onStartFailure: "+s);
            }

            @Override
            public void onEndFailure(Client client, String s) {
              LOG.error("setMethodProfilingHandler","onEndFailure: "+s);
            }
         });
        });

    //processCommandLineArgs();
  }

  synchronized private void updateDevices() {
    clearDevicesCombo();
    AndroidDebugBridge bridge = AdbService.getBridge();
    if (bridge!=null){
        IDevice[] devices = bridge.getDevices();
        AdbService.updateDevices(devices);
        for (IDevice device:devices){
              devicesCombo.addItem(device);
              devicesCombo.setMaximumSize(new Dimension(10,30));
        }
        if (devicesCombo.getItemCount() > 1 ){
          //fixme
          devicesCombo.insertItemAt("请选择",0);
          devicesCombo.removeItemAt(1);
        }
    }
  }

  synchronized private void initDevicesCombo(){
    if (devicesCombo == null)
    {
      devicesCombo = new JComboBox();
      //Use ActionListener instead of ItemListener to pop up dialog when click selected device button
      devicesCombo.addActionListener(e -> {
        Object o = devicesCombo.getSelectedItem();
        if (o instanceof IDevice)
        {
          IDevice device = (IDevice)o;
          deviceSelectedAction(device);
        }
      });

      devicesCombo.setMaximumSize(new Dimension(10,30));
    }
    if (devicesCombo.getItemCount()==0)
      //fixme
        devicesCombo.addItem("等待设备连接");
  }

  synchronized private void clearDevicesCombo(){
    devicesCombo.removeAllItems();
    //fixme
    devicesCombo.addItem("等待设备连接");
    System.out.println(devicesCombo.getSize().getWidth());
  }

  private void deviceSelectedAction(@NotNull IDevice device){
      Client[] clients = device.getClients();
      if (clients.length <= 0)
        return;
      ProfilerClient[] profilerClients = ProfilerClient.clients2ProfilerClients(clients);
      ProfilerClient selectedClient  =
        //fixme
        (ProfilerClient)JOptionPane.showInputDialog(mainPanel, "选择进程", "进程列表", JOptionPane.PLAIN_MESSAGE, null, profilerClients, profilerClients[0]);

      if (selectedClient!=null)
      {
        LOG.info("selected "+ selectedClient.toString());
        addProcessesTree(device.toString(), selectedClient.getClient().getClientData().getClientDescription(), selectedClient.getClient().getClientData().getMethodProfilingStatus());
      }
      else {
        LOG.info("user cancelled dialog");
      }
  }

  @NotNull
  private JDevice updateDeviceNode(@NotNull String deviceName){
    JNode deviceNode =  testTreeRoot.getChildByDescription(deviceName);
    if (deviceNode == null)
    {
      deviceNode = new JDevice(deviceName);
      if (!testTreeRoot.getStatus())
        //fixme
        testTreeRoot.setDescription("设备列表", true);
      testTreeRoot.add(deviceNode);
    }
    return (JDevice)deviceNode;
  }

  @NotNull
  private JProcess addProcessNode(@NotNull String deviceName, @NotNull String clientDescription, ClientData.MethodProfilingStatus clientStatus){
    JNode deviceNode =  updateDeviceNode(deviceName);

    JNode processNode = deviceNode.getChildByDescription(clientDescription);
    if (processNode == null) {
      boolean status = clientStatus == ClientData.MethodProfilingStatus.TRACER_ON;
      processNode = new JProcess(clientDescription, status);
      deviceNode.add(processNode);
    }
    return (JProcess)processNode;
  }


  private void addProcessesTree(@NotNull String deviceName,@NotNull String clientDescription, ClientData.MethodProfilingStatus clientStatus){

    JNode processNode = addProcessNode(deviceName, clientDescription, clientStatus);
      //select and click this node
    tree.setSelectionPath(new TreePath(processNode.getPath()));
    SwingUtilities.invokeLater(() -> tree.updateUI());
    nodeClickAction(processNode);
  }


   synchronized private void updateTracesTree(@NotNull String deviceName, @NotNull String clientDescription, ClientData.MethodProfilingStatus clientStatus, @NotNull String traceDesc, @NotNull CpuCaptureParser parser, @NotNull CpuCapture capture){

     JNode processNode = addProcessNode(deviceName, clientDescription, clientStatus);
     JTrace jTrace = new JTrace(traceDesc, parser, capture);
     processNode.add(jTrace);
     tree.setSelectionPath(new TreePath(jTrace.getPath()));
     SwingUtilities.invokeLater(() -> tree.updateUI());
     nodeClickAction(jTrace);
   }



}
