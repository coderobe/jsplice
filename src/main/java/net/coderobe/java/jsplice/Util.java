package net.coderobe.java.jsplice;

import java.io.File;
import org.apache.commons.io.FileUtils;
import java.io.IOException;

public class Util {
  public static void log(String msg){
    System.out.print(msg);
  }

  public static boolean stringValid(String target){
    return target != null && !target.isEmpty();
  }

  public static String readFile(String path) {
    File file;
    String data;
    try {
      if(!stringValid(path)){
        throw new IOException("garbage file path");
      }
      file = new File(path);
      data = FileUtils.readFileToString(file);
    } catch(IOException e) {
      System.err.println(String.format("Couldn't read file '%s'", path));
      return "";
    }
    return data;
  }
}