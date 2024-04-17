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
			tree.forEach(ThrowingConsumer.of(p->{
				var dest = workingDir.resolve(Path.of("rt", "libnative")).resolve(p.getFileName());
				Files.createDirectories(dest);
				Files.copy(p, dest, StandardCopyOption.REPLACE_EXISTING);
			}));
		}
	}
}
