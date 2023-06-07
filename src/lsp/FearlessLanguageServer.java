package lsp;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class FearlessLanguageServer implements LanguageServer, LanguageClientAware {
  public static void main(String[] args) {
    LogManager.getLogManager().reset();
    Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.OFF);
    try {
      startServer(System.in, System.out).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }
  public static Future<Void> startServer(InputStream in, OutputStream out) {
    var server = new FearlessLanguageServer();
    var launcher = new LSPLauncher.Builder<LanguageClient>()
      .setLocalService(server)
      .setRemoteInterface(LanguageClient.class)
      .setInput(in)
      .setOutput(out)
      .create();
    server.connect(launcher.getRemoteProxy());
    return launcher.startListening();
  }
//  public static void main(String[] args) {
//    try (var inClient = new PipedInputStream(); var outClient = new PipedOutputStream()) {
//      var inServer = new PipedInputStream();
//      var outServer = new PipedOutputStream();
//
//      inClient.connect(outServer);
//      outClient.connect(inServer);
//      var server = new FearlessLanguageServer();
//      var serverLauncher = LSPLauncher.createServerLauncher(server, inServer, outServer);
//      serverLauncher.startListening().get();
//    } catch (IOException | InterruptedException | ExecutionException e) {
//      throw new RuntimeException(e);
//    }
//  }

  private final TextDocumentService doc = new FearlessTextDocumentService();
  private final WorkspaceService workspace = new FearlessWorkspaceService();
  private LanguageClient client;
  private int errorCode = 1;

  @Override public CompletableFuture<InitializeResult> initialize(InitializeParams initializeParams) {
    var capabilities = new ServerCapabilities();
    capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
    var diagnostics = new DiagnosticRegistrationOptions();
    capabilities.setDiagnosticProvider(diagnostics);
    return CompletableFuture.completedFuture(new InitializeResult(capabilities));
  }

  @Override public CompletableFuture<Object> shutdown() {
    this.errorCode = 0;
    return CompletableFuture.completedFuture(null);
  }

  @Override public void exit() {
    System.exit(this.errorCode);
  }

  @Override public TextDocumentService getTextDocumentService() {
    return this.doc;
  }

  @Override public WorkspaceService getWorkspaceService() {
    return this.workspace;
  }

  @Override public void connect(LanguageClient client) {
    this.client = client;
  }
}
