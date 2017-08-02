// Generated from JsonPath2.g4 by ANTLR 4.5.3
package gov.nasa.jpl.kservices.sysml2k;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link JsonPath2Parser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface JsonPath2Visitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link JsonPath2Parser#path}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPath(JsonPath2Parser.PathContext ctx);
	/**
	 * Visit a parse tree produced by {@link JsonPath2Parser#element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElement(JsonPath2Parser.ElementContext ctx);
	/**
	 * Visit a parse tree produced by {@link JsonPath2Parser#tagelement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTagelement(JsonPath2Parser.TagelementContext ctx);
	/**
	 * Visit a parse tree produced by {@link JsonPath2Parser#tag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTag(JsonPath2Parser.TagContext ctx);
	/**
	 * Visit a parse tree produced by {@link JsonPath2Parser#indexelement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexelement(JsonPath2Parser.IndexelementContext ctx);
	/**
	 * Visit a parse tree produced by {@link JsonPath2Parser#index}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndex(JsonPath2Parser.IndexContext ctx);
	/**
	 * Visit a parse tree produced by {@link JsonPath2Parser#attributefilterelement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttributefilterelement(JsonPath2Parser.AttributefilterelementContext ctx);
	/**
	 * Visit a parse tree produced by {@link JsonPath2Parser#attributevalue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttributevalue(JsonPath2Parser.AttributevalueContext ctx);
	/**
	 * Visit a parse tree produced by {@link JsonPath2Parser#doubleattributefilterelement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDoubleattributefilterelement(JsonPath2Parser.DoubleattributefilterelementContext ctx);
	/**
	 * Visit a parse tree produced by {@link JsonPath2Parser#negationfilterelement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNegationfilterelement(JsonPath2Parser.NegationfilterelementContext ctx);
	/**
	 * Visit a parse tree produced by {@link JsonPath2Parser#alternationelement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlternationelement(JsonPath2Parser.AlternationelementContext ctx);
	/**
	 * Visit a parse tree produced by {@link JsonPath2Parser#branches}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBranches(JsonPath2Parser.BranchesContext ctx);
}