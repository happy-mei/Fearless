package lsp;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FearlessTextDocumentService implements TextDocumentService {
  @Override public CompletableFuture<DocumentDiagnosticReport> diagnostic(DocumentDiagnosticParams params) {
    var d = new Diagnostic();
    d.setMessage("bad bad bad");
    d.setSeverity(DiagnosticSeverity.Error);
    var report = new DocumentDiagnosticReport(new RelatedFullDocumentDiagnosticReport(List.of(d)));
    return CompletableFuture.completedFuture(report);
  }

  @Override public void didOpen(DidOpenTextDocumentParams params) {}
  @Override public void didChange(DidChangeTextDocumentParams params) {}
  @Override public void didClose(DidCloseTextDocumentParams params) {}
  @Override public void didSave(DidSaveTextDocumentParams params) {}
}
