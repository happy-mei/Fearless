package codegen.java;

import utils.IoErr;
import utils.ResolveResource;
import utils.ThrowingConsumer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public interface CopyRuntimeLibs {
	static void of(Path workingDir) {
		var resourceLibPath = ResolveResource.artefact("/rt/libnative");
		try(var tree = IoErr.of(()-> Files.walk(resourceLibPath))) {
			tree.filter(Files::isRegularFile).forEach(ThrowingConsumer.of(p->{
				// The toString is important here because it may be from a different FS than the other paths,
				// and if it is, Java will throw a ProviderMismatchException.
				var dest = workingDir.resolve(Path.of("rt", "libnative")).resolve(p.getFileName().toString());
				if (Files.exists(dest)) { return; }
				Files.createDirectories(dest);
				Files.copy(p, dest, StandardCopyOption.REPLACE_EXISTING);
			}));
		}
	}
}
