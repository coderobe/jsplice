package net.coderobe.java.jsplice;

import java.util.List;
import java.util.ArrayList;
import com.beust.jcommander.Parameter;

public class Args {
  @Parameter
  public List<String> parameters = new ArrayList<>();
  
  @Parameter(
    names = "-import",
    description = "Classpath to import, can be specified multiple times"
  )
  public List<String> importpath;

  @Parameter(
    names = "-patch",
    description = "Path to the jsplice patch file"
  )
  public String patch;

  @Parameter(
    names = "-outdir",
    description = "Output path for the patched classes"
  )
  public String outdir;
}