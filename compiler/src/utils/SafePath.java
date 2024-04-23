package utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.Iterator;

public record SafePath(Path inner) implements Path {
  public static Path of(String first, String... more) {return new SafePath(Path.of(first, more));}
  public static Path of(URI uri) {return new SafePath(Path.of(uri));}


  // Just delegating to inner in all of these

  @Override public FileSystem getFileSystem() {return inner.getFileSystem();}
  @Override public boolean isAbsolute() {return inner.isAbsolute();}
  @Override public Path getRoot() {return inner.getRoot();}
  @Override public Path getFileName() {return inner.getFileName();}
  @Override public Path getParent() {return inner.getParent();}
  @Override public int getNameCount() {return inner.getNameCount();}
  @Override public Path getName(int index) {return inner.getName(index);}
  @Override public Path subpath(int beginIndex, int endIndex) {return inner.subpath(beginIndex, endIndex);}
  @Override public boolean startsWith(Path other) {return inner.startsWith(other);}
  @Override public boolean endsWith(Path other) {return inner.endsWith(other);}
  @Override public Path normalize() {return inner.normalize();}
  @Override public Path resolve(Path other) {return inner.resolve(other);}
  @Override public Path relativize(Path other) {return inner.relativize(other);}
  @Override public URI toUri() {return inner.toUri();}
  @Override public Path toAbsolutePath() {return inner.toAbsolutePath();}
  @Override public Path toRealPath(LinkOption... options) throws IOException {return inner.toRealPath(options);}
  @Override public WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException {return inner.register(watcher, events, modifiers);}
  @Override public int compareTo(Path other) {return inner.compareTo(other);}
  @Override public boolean equals(Object other) {return inner.equals(other);}
  @Override public int hashCode() {return inner.hashCode();}
  @Override public String toString() {return inner.toString();}
}
