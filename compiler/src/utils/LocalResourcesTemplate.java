package utils;

import java.nio.file.Path;

public class LocalResourcesTemplate {
  //example for windows
  static public final Path compilerPath= Path.of("C:\\")
    .resolve("Users","<userNameHere>","OneDrive","Documents","GitHub","Fearless","compiler");
  static public final String javaVersion= "23";
  }
  //example for linux
  //example for mac
