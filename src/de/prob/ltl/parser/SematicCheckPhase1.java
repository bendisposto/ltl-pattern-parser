package de.prob.ltl.parser;

import org.antlr.v4.runtime.tree.TerminalNode;

import de.prob.ltl.parser.LtlParser.AfterScopeDefContext;
import de.prob.ltl.parser.LtlParser.AfterUntilScopeDefContext;
import de.prob.ltl.parser.LtlParser.BeforeScopeDefContext;
import de.prob.ltl.parser.LtlParser.BetweenScopeDefContext;
import de.prob.ltl.parser.LtlParser.GlobalScopeDefContext;
import de.prob.ltl.parser.LtlParser.Pattern_defContext;
import de.prob.ltl.parser.LtlParser.Pattern_def_paramContext;
import de.prob.ltl.parser.LtlParser.VarCallAtomContext;
import de.prob.ltl.parser.LtlParser.Var_assignContext;
import de.prob.ltl.parser.LtlParser.Var_defContext;
import de.prob.ltl.parser.symboltable.Pattern;
import de.prob.ltl.parser.symboltable.Pattern.PatternScopes;
import de.prob.ltl.parser.symboltable.SymbolTable;
import de.prob.ltl.parser.symboltable.Variable;
import de.prob.ltl.parser.symboltable.Variable.VariableTypes;

public class SematicCheckPhase1 extends LtlBaseListener {

	private SymbolTable symbolTable;
	private Pattern currentPattern;

	public SematicCheckPhase1(SymbolTable symbolTable) {
		this.symbolTable = symbolTable;
	}

	@Override
	public void exitVar_def(Var_defContext ctx) {
		TerminalNode nameNode = ctx.ID();
		String name = nameNode.getText();
		VariableTypes type = VariableTypes.var;

		symbolTable.define(new Variable(name, type));
	}

	@Override
	public void enterVar_assign(Var_assignContext ctx) {
		TerminalNode nameNode = ctx.ID();
		String name = nameNode.getText();

		if (!symbolTable.isDefined(name)) {
			throw new RuntimeException(String.format("Assignment to undefined variable '%s'.", name));
		}
	}

	@Override
	public void enterVarCallAtom(VarCallAtomContext ctx) {
		TerminalNode nameNode = ctx.ID();
		String name = nameNode.getText();

		if (!symbolTable.isDefined(name)) {
			throw new RuntimeException(String.format("Variable '%s' cannot be resolved.", name));
		}
	}

	@Override
	public void enterPattern_def(Pattern_defContext ctx) {
		if (ctx.exception != null) {
			return;
		}
		TerminalNode nameNode = ctx.ID();
		String name = nameNode.getText();

		currentPattern = new Pattern(symbolTable.getCurrentScope(), name);
		symbolTable.pushScope(currentPattern);
	}

	@Override
	public void exitPattern_def(Pattern_defContext ctx) {
		symbolTable.popScope();
		symbolTable.define(currentPattern);
		currentPattern = null;
	}

	@Override
	public void enterPattern_def_param(Pattern_def_paramContext ctx) {
		TerminalNode nameNode = ctx.ID();
		String name = nameNode.getText();
		VariableTypes type = (ctx.NUM_VAR() != null ? VariableTypes.num : VariableTypes.var);

		Variable parameter = new Variable(name, type);
		symbolTable.define(parameter);
		currentPattern.addParameter(parameter);
	}

	@Override
	public void enterGlobalScopeDef(GlobalScopeDefContext ctx) {
		currentPattern.setScope(PatternScopes.global);
	}

	@Override
	public void enterBeforeScopeDef(BeforeScopeDefContext ctx) {
		currentPattern.setScope(PatternScopes.before);
		TerminalNode nameNode = ctx.ID();
		String name = nameNode.getText();
		Variable scopeParameter = new Variable(name, VariableTypes.var);
		symbolTable.define(scopeParameter);
	}

	@Override
	public void enterAfterScopeDef(AfterScopeDefContext ctx) {
		currentPattern.setScope(PatternScopes.after);
		TerminalNode nameNode = ctx.ID();
		String name = nameNode.getText();
		Variable scopeParameter = new Variable(name, VariableTypes.var);
		symbolTable.define(scopeParameter);
	}

	@Override
	public void enterBetweenScopeDef(BetweenScopeDefContext ctx) {
		currentPattern.setScope(PatternScopes.between);
		for (TerminalNode nameNode : ctx.ID()) {
			String name = nameNode.getText();
			Variable scopeParameter = new Variable(name, VariableTypes.var);
			symbolTable.define(scopeParameter);
		}
	}

	@Override
	public void enterAfterUntilScopeDef(AfterUntilScopeDefContext ctx) {
		currentPattern.setScope(PatternScopes.after_until);
		for (TerminalNode nameNode : ctx.ID()) {
			String name = nameNode.getText();
			Variable scopeParameter = new Variable(name, VariableTypes.var);
			symbolTable.define(scopeParameter);
		}
	}

}
