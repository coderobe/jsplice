package net.coderobe.java.jsplice;

import net.coderobe.java.jsplice.*;
import javassist.*;
import com.beust.jcommander.JCommander;
import com.google.gson.Gson;
import java.io.IOException;
import com.google.gson.JsonSyntaxException;
import java.util.stream.Stream;
import java.util.List;
import java.util.ArrayList;

public class App {
  private static void implementInterfaces(ClassPool pool, CtClass target_class, String[] ifaces){
    Stream.of(ifaces).forEach(iface -> {
      Util.log(String.format("Looking for '%s'\n", iface));
      CtClass iface_class;
      try {
        iface_class = pool.get(iface);
      } catch(NotFoundException e) {
        System.err.println(String.format("Interface '%s' not found, skipping implementation", iface));
        return;
      }

      Util.log(String.format("Adding interface %s\n", iface));
      target_class.addInterface(iface_class);
    });
  }
  public static void main(String[] argv) {
    Util.log("jsplice by coderobe\n");

    Args args = new Args();
    JCommander.newBuilder().addObject(args).build().parse(argv);

    Util.log(String.format("Import: %s\n", args.importpath));
    Util.log(String.format("Patch: %s\n", args.patch));
    Util.log(String.format("Outdir: %s\n\n", args.outdir));

    if(!Util.stringValid(args.patch)){
      System.err.println("Invalid patch path");
      return;
    }

    Util.log("Reading patchfile; ");
    String patchdata = Util.readFile(args.patch);

    Util.log("Parsing patches; ");
    Gson gson = new Gson();
    Patch[] patches;
    try {
      patches = gson.fromJson(patchdata, Patch[].class);
    } catch(JsonSyntaxException e) {
      System.err.println(String.format("Malformed patch input: %s", e.getCause()));
      return;
    }

    Util.log(String.format("Found %d\n\n", patches.length));

    Util.log("Preparing environment\n");
    Util.log("Initializing ClassPool\n");
    ClassPool pool = ClassPool.getDefault();
    args.importpath.forEach(path -> {
      if(Util.stringValid(path)){
        try {
          pool.appendPathList(path);
        } catch(NotFoundException e) {
          System.err.println(String.format("Couldn't import classes: %s", e.getCause()));
          return;
        }
      }
    });

    Util.log("\nApplying patches...\n");
    Stream.of(patches).forEach(patch -> {
      Util.log(String.format("\nLooking for '%s'\n", patch.target));
      CtClass target_class;
      try {
        target_class = pool.get(patch.target);
      } catch(NotFoundException e) {
        System.err.println("Target class not found, skipping patch");
        return;
      }
      if(target_class == null){
        System.err.println("Target class not found, skipping patch");
        return;
      }
      Util.log("Class acquired\n");

      if(patch.interfaces != null && patch.interfaces.length > 0){
        Util.log(String.format("Implementing %d interfaces...\n", patch.interfaces.length));
        implementInterfaces(pool, target_class, patch.interfaces);
      }

      Stream.of(patch.methods).forEach(fun -> {
        Util.log(String.format("\nPatch target: %s.%s\n", patch.target, fun.name));

        CtMethod method;
        String method_name_original = target_class.makeUniqueName(fun.name+"$impl");
        if(fun.type != PatchType.ADD.id){
          Util.log(String.format("Looking for '%s'\n", fun.name));

          try {
            method = target_class.getDeclaredMethod(fun.name);
          } catch(NotFoundException e) {
            System.err.println("Target method not found, skipping patch");
            return;
          }

          if(fun.type == PatchType.APPEND.id){
            Util.log("Operation: Method appending\n");
            Util.log("Preserving original method\n");
            method.setName(method_name_original);
            try {
              method = CtNewMethod.copy(method, fun.name, target_class, null);
            } catch(CannotCompileException e) {
              System.err.println(String.format("Can't compile method: %s", e.getCause()));
              return;
            }
          }else if(fun.type == PatchType.REPLACE.id){
            Util.log("Operation: Method replacement\n");
          }
        }else{
          ArrayList<CtClass> params = new ArrayList<>();
          Stream.of(fun.parameters).forEach(param -> {
            if(Util.stringValid(param)){
              try {
                params.add(pool.get(param));
              } catch(NotFoundException e) {
                System.err.println("Couldn't find argument");
                return;
              }
            }
          });
          CtClass[] params_ary = new CtClass[params.size()];
          params.toArray(params_ary);

          CtClass returns;
          try {
            returns = pool.get(fun.returns);
          } catch(NotFoundException e) {
            System.err.println("Function return value not found, skipping patch");
            return;
          }
          method = new CtMethod(returns, fun.name, params_ary, target_class);
        }

        Util.log("Generating patched method\n");

        StringBuffer body = new StringBuffer();
        body.append("{");

        if(fun.type == PatchType.APPEND.id){
          String type;
          try {
            type = method.getReturnType().getName();
          } catch(NotFoundException e) {
            System.err.println("Target method return type not found, skipping patch");
            return;
          }

          if(!type.equals("void")) {
            Util.log(String.format("Adding var %s jsplice_result to method body\n", type));
            body.append(String.format("%s jsplice_result = ", type));
          }else{
            Util.log("Method is of type void, jsplice_result will be unavailable\n");
          }
          body.append(method_name_original+"($$);");
        }

        Stream.of(fun.body).forEach(line -> {
          if(Util.stringValid(line)){
            body.append(line);
          }
        });

        body.append("}");

        Util.log("Compiling patch...\n");
        try {
          method.setBody(body.toString());
          if(fun.type != PatchType.REPLACE.id){
            target_class.addMethod(method);
          }
        } catch(CannotCompileException e) {
          System.err.println(body.toString());
          System.err.println(String.format("Cannot compile patch: %s", e.getCause()));
          e.printStackTrace();
          return;
        }

        Util.log("Applied cleanly.\n");

        if(Util.stringValid(args.outdir)){
          Util.log("Saving modified class\n");
          try {
            target_class.writeFile(args.outdir);
            target_class.defrost();
          } catch (IOException | CannotCompileException e) {
            System.err.println("Error saving patched class");
            return;
          }
        }
      });
    });
  }
}
