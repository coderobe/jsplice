package net.coderobe.java.jsplice;

import net.coderobe.java.jsplice.*;

public enum PatchType {
  REPLACE(0),
  APPEND(1),
  ADD(2);

  public final int id;
  private PatchType(int id) {
    this.id = id;
  }
}