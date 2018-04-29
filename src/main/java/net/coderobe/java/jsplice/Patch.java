package net.coderobe.java.jsplice;

import net.coderobe.java.jsplice.*;

public class Patch {
  public static class Method {
    public String name;
    public int type;
    public String returns;
    public String[] parameters;
    public String[] body;
  }

  public String target;
  public String[] interfaces;
  public Method[] methods;
}