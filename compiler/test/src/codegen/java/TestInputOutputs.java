package codegen.java;

import main.InputOutput;
import utils.IoErr;
import utils.Push;
import utils.ResolveResource;

import java.nio.file.Files;
import java.util.List;


public interface TestInputOutputs {
	static InputOutput programmaticAuto(List<String> files){
		return InputOutput.programmaticAuto(files);
	}
	static InputOutput programmaticImm(List<String> files){
		return programmaticImm(files, List.of());
	}
	static InputOutput programmaticImm(List<String> files, List<String> args){
		var workingDir = ResolveResource.freshTmpPath();
		IoErr.of(()->Files.createDirectories(workingDir));
		args = Push.of("test.Test", args);
		return InputOutput.programmaticImm(
				"test.Test",
				args,
				files,
				workingDir,
				workingDir
		);
	}
}
