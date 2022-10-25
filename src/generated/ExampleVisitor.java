// Generated from java-escape by ANTLR 4.11.1
package generated;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link ExampleParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface ExampleVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link ExampleParser#e}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitE(ExampleParser.EContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExampleParser#mCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMCall(ExampleParser.MCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExampleParser#x}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitX(ExampleParser.XContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExampleParser#lambda}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLambda(ExampleParser.LambdaContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExampleParser#nudeE}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNudeE(ExampleParser.NudeEContext ctx);
}