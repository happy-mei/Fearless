module FearlessCompiler {
  requires org.antlr.antlr4.runtime;
  requires org.junit.jupiter.api;
  requires org.opentest4j;
  requires net.jqwik.api;
  requires java.compiler;
//  requires commons.cli;
  requires cmdline.app;
  requires java.logging;
  requires org.apache.commons.text;
  requires org.apache.commons.lang3;
  requires commons.cli;
//  exports tour;
//  opens typing to net.jqwik.engine, org.junit.platform.commons;
  opens program.typesystem to net.jqwik.engine, org.junit.platform.commons;
}