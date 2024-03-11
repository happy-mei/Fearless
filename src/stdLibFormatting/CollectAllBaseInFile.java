package stdLibFormatting;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
  import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.List;

public class CollectAllBaseInFile {
  public static List<Path> allFiles(Path startDir) throws IOException{
    try (var files = Files.walk(startDir)) {
      return files.filter(Files::isRegularFile)
        .filter(path->path.toString().endsWith(".fear"))
        .toList();
    }
  }
  public static void main(String[] args) throws IOException, URISyntaxException {
    Path startDir= Paths.get("resources/base");
    //IntelliJ is opinionated in the wrong way
    //Path outputFile= Paths.get(Objects.requireNonNull(CollectAllBaseInFile.class.getResource("allBase.txt")).toURI());
    Path outputFile= Paths.get("resources/allBase/allBase.txt");

    try (var writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
        for (Path file : allFiles(startDir)) {
          writer.write("\n//---File " + startDir.relativize(file)+"\n");
          for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            if(line.trim().startsWith("//")){ continue; }
            writer.write(line);
            writer.newLine();
          }
        }
      }
  }
}
