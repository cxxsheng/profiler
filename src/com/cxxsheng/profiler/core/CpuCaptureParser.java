package com.cxxsheng.profiler.core;

import com.android.annotations.Trace;
import com.android.ddmlib.Log;
import com.android.tools.profilers.cpu.CpuCapture;
import com.android.tools.profilers.cpu.TraceParser;
import com.android.tools.profilers.cpu.art.ArtTraceParser;
import com.android.tools.profilers.cpu.atrace.AtraceParser;
import com.android.tools.profilers.cpu.atrace.AtraceProducer;
import com.android.tools.profilers.cpu.simpleperf.SimpleperfTraceParser;
import com.cxxsheng.profiler.utils.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;

public class CpuCaptureParser {
  private static Logger LOG = LoggerFactory.getLogger(CpuCaptureParser.class);
  static final int MAX_SUPPORTED_TRACE_SIZE = 1024 * 1024 * 100; // 100MB
  static final long IMPORTED_TRACE_ID = 42L;
  private TraceParser mParser;
  private String description;
  private File traceFile = null;
  private byte[] bytes;
  private CpuCapture parse(@NotNull File traceFile) throws IOException {

      if (bytes == null || bytes.length == 0){
        bytes = IOUtils.toByteArray(traceFile);
      }
      long fileLength = traceFile.length();
      if (fileLength > MAX_SUPPORTED_TRACE_SIZE){
        Log.w("parse", "fileLength is too large, len is " + fileLength);
      }
      return tryParsingFileWithDifferentParsers(traceFile);
  }


  @Nullable
  public CpuCapture parse(@NotNull Path filePath) throws IOException {
    traceFile = filePath.toFile();
    if (!traceFile.exists()  && traceFile.isDirectory())
      return null;
    description = traceFile.getAbsolutePath();
    return parse(traceFile);
  }

  public String getDescription() {
    return description;
  }

  @NotNull
  public byte[] getBytes() {
    return bytes;
  }

  @Nullable
  public CpuCapture parse(@NotNull byte[] trace, @NotNull String tempName) throws IOException {
    bytes = trace;
    try {
      traceFile = File.createTempFile("tarce.", "" + tempName);
      description = traceFile.getAbsolutePath();
      FileOutputStream outputStream = new FileOutputStream(traceFile);
      outputStream.write(trace);
      outputStream.close();
    }catch (IOException e){
      e.printStackTrace();
    }
    if (traceFile != null){
     return parse(traceFile);
    }else {
      return null;
    }
  }

  @Nullable
  public File getTraceFile(){
    return traceFile;
  }


  public TraceParser getParser() {
    return mParser;
  }

  private CpuCapture tryParsingFileWithDifferentParsers(@NotNull File traceFile){
    try{
      ArtTraceParser artTraceParser = new ArtTraceParser();
      mParser = artTraceParser;
      return artTraceParser.parse(traceFile, IMPORTED_TRACE_ID);
    }catch (Exception igonred){

    }

    try {
      SimpleperfTraceParser simpleperfTraceParser = new SimpleperfTraceParser();
      mParser = simpleperfTraceParser;
      return simpleperfTraceParser.parse(traceFile, IMPORTED_TRACE_ID);
    }catch (Exception igonred){

    }

    try{
      if (AtraceProducer.verifyFileHasAtraceHeader(traceFile)){
        AtraceParser parser = new AtraceParser(traceFile);
        mParser = parser;
        return parser.parse(traceFile, IMPORTED_TRACE_ID);
      }
    }catch (Exception e){

    }
    LOG.warn("parse" ,String.format("Parsing %s has failed.", traceFile.getPath()));
    return null;
  }
}
