// Generated from java-escape by ANTLR 4.11.1
package generated;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link ExampleParser}.
 */
public interface ExampleListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link ExampleParser#e}.
	 * @param ctx the parse tree
	 */
	void enterE(ExampleParser.EContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExampleParser#e}.
	 * @param ctx the parse tree
	 */
	void exitE(ExampleParser.EContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExampleParser#mCall}.
	 * @param ctx the parse tree
	 */
	void enterMCall(ExampleParser.MCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExampleParser#mCall}.
	 * @param ctx the parse tree
	 */
	void exitMCall(ExampleParser.MCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExampleParser#x}.
	 * @param ctx the parse tree
	 */
	void enterX(ExampleParser.XContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExampleParser#x}.
	 * @param ctx the parse tree
	 */
	void exitX(ExampleParser.XContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExampleParser#lambda}.
	 * @param ctx the parse tree
	 */
	void enterLambda(ExampleParser.LambdaContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExampleParser#lambda}.
	 * @param ctx the parse tree
	 */
	void exitLambda(ExampleParser.LambdaContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExampleParser#nudeE}.
	 * @param ctx the parse tree
	 */
	void enterNudeE(ExampleParser.NudeEContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExampleParser#nudeE}.
	 * @param ctx the parse tree
	 */
	void exitNudeE(ExampleParser.NudeEContext ctx);
}