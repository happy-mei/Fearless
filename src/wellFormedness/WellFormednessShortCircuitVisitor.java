package wellFormedness;

import astFull.E;
import astFull.Program;
import astFull.T;
import failure.CompileError;
import failure.Fail;
import files.HasPos;
import id.Id;
import id.Mdf;
import magic.Magic;
import visitors.FullShortCircuitVisitorWithEnv;
import visitors.ShortCircuitVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

// TODO: Sealed and _C/_m restrictions
public class WellFormednessShortCircuitVisitor implements ShortCircuitVisitor<CompileError> {

}
