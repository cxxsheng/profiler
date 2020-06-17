package com.cxxsheng.profiler.ui.treemodel.profiler;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.android.tools.profilers.cpu.CaptureNode;
import com.android.tools.profilers.cpu.CpuCapture;
import com.android.tools.profilers.cpu.CpuThreadInfo;
import com.android.tools.profilers.cpu.nodemodel.CaptureNodeModel;
import com.android.tools.profilers.cpu.nodemodel.JavaMethodModel;
import com.android.tools.profilers.cpu.nodemodel.SingleNameModel;
import com.cxxsheng.profiler.core.CpuCaptureParser;
import com.cxxsheng.profiler.utils.Base64;
import com.cxxsheng.profiler.utils.UiUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JTrace extends JNode {


  private static final long serialVersionUID = 587026442084236958L;
  private static final Logger LOG = LoggerFactory.getLogger(JTrace.class);

  transient private byte[] bytes;

  transient private List<JThread> threads = new ArrayList<>();

  private boolean isSaved = false;

  public boolean isSaved() {
    return isSaved;
  }

  public boolean saveFile(Path path){
      if (bytes == null || bytes.length == 0)
        return false;
      try{
        FileOutputStream out = new FileOutputStream(path.toFile());
        out.write(bytes);
        isSaved = true;
        return true;
      }
      catch (IOException e) {
        e.printStackTrace();
        return false;
      }
  }



  private JTrace(@NotNull String description, byte[] bytes, List<JThread> threads){
    this.description = description;
    this.bytes = bytes;
    this.threads = threads;
  }



  public JTrace(@NotNull String description, @NotNull CpuCaptureParser parser, @NotNull CpuCapture capture){

    this.description = description;

    Map<CpuThreadInfo, CaptureNode> nodesMaps = parser.getParser().getCaptureTrees();
    for (CpuThreadInfo info : nodesMaps.keySet()){
      JThread jThread = new JThread(info);

      for (JMethod method : parseThread(nodesMaps.get(info))){
        jThread.add(method);
      }
      threads.add(jThread);
    }

    bytes = parser.getBytes();
  }



  private List<JMethod> parseThread(@NotNull CaptureNode root){
    List<JMethod> ret = new ArrayList<>();
    if (root.getData() instanceof SingleNameModel)
    {
       LOG.info(String.format("start parsing thread %s methods", root.getData().getName()));
       for (CaptureNode child : root.getChildren())
         {
           JMethod methodNode = parseThreadMethod(child);
           if (methodNode!=null)
             ret.add(methodNode);
         }

    }
    return ret;

  }

  @Nullable
  private JMethod parseThreadMethod(@NotNull CaptureNode methodRoot){
    CaptureNodeModel method = methodRoot.getData();

    if (method instanceof JavaMethodModel)
    {
      JMethod methodNode = new JMethod((JavaMethodModel)method);
      if (!methodRoot.getChildren().isEmpty())
        for (CaptureNode child : methodRoot.getChildren())
        {
          JMethod childMethod = parseThreadMethod(child);
          if (childMethod!=null)
            methodNode.add(childMethod);
        }
      return methodNode;
    }
    LOG.warn("unhandled type"+method.toString());
    return null;
  }

  @Override
  //fixme
  public String toString() {
    return  description + (status ? " [Tracing]" : "");
  }

  @NotNull
  public List<JThread> getContentNodes(){
    return threads;
  }

  @Override
  public boolean checkSameDescription(@NotNull String description) {
    return this.description.equals(description);
  }


  @Override
  public Icon getIcon() {
    return UiUtils.openIconTest("trace");
  }

  @Override
  public Map getMap() {
    Map map = super.getMap();

    map.put("bytes",bytes);

    List threadsList = new ArrayList();
    for (JThread thread : threads){
      threadsList.add(thread.getMap());
    }
    map.put("threads", threadsList);


    return map;
  }

  public static JTrace parse(@NotNull Map map) {


    String description = (String)map.get("description");
    boolean status = (boolean)map.get("status");
    byte[] bytes = Base64.decodeFast(map.get("bytes").toString());

    List <Map> threadsMap = (List<Map>)map.get("threads");


    List<JThread> threads = new ArrayList<>();

    for (Map threadMap: threadsMap){
      threads.add(JThread.parse(threadMap));
    }
    JTrace trace = new JTrace(description,bytes,threads);
    trace.setStatus(status);
    trace.isSaved = true;

    List<Map> children = (List<Map>)map.get("children");

    for (Map jTraceMap: children){
      trace.add(JThread.parse(jTraceMap));
    }
    return trace;
  }

  static class JThread extends JNode{


    private static final long serialVersionUID = 3391778855177027061L;

    public JThread(@NotNull CpuThreadInfo info){
      description = info.getName();
    }
    @Override
    public boolean checkSameDescription(@NotNull String description) {
      return false;
    }


    private JThread(String description){
      this.description = description;
    }

    @Override
    public Icon getIcon() {
      return UiUtils.openIconTest("thread");
    }


    protected static JThread parse(Map map){
      String description = (String)map.get("description");
      boolean status = (boolean)map.get("status");
      JThread thread = new JThread(description);
      List<Map> children = (List<Map>)map.get("children");

      for (Map jTraceMap: children){
        thread.add(JMethod.parse(jTraceMap));
      }
      return thread;
    }

  }

  static class JMethod extends JNode{

    private static final long serialVersionUID = -3570339289347884862L;

    public JMethod(@NotNull JavaMethodModel model){
      this.description = model.getClassName()+" -> "+model.getName()+"  "+model.getSignature();
    }

    private JMethod(@NotNull String description){
      this.description = description;
    }
    @Override
    public boolean checkSameDescription(@NotNull String description) {
      return description.equals(this.description);
    }


    @Override
    @Nullable
    public Icon getIcon() {
      return UiUtils.openIconTest("function");
    }


    protected static JMethod parse(Map map){
      String description = (String)map.get("description");
      boolean status = (boolean)map.get("status");
      JMethod method = new JMethod(description);
      method.setStatus(status);
      List<Map> children = (List<Map>)map.get("children");

      for (Map jTraceMap: children){
        method.add(JMethod.parse(jTraceMap));
      }
      return method;
    }
  }



}
