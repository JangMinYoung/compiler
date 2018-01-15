import org.antlr.v4.runtime.tree.ParseTreeProperty;

public class MiniCPrintVisitor extends MiniCBaseVisitor<String> {
	int dot = 0;

	// 아래는 보조 메소드이다.
	@Override
	public String visitProgram(MiniCParser.ProgramContext ctx) {
		String input = "";
		int i = 0;
		while (ctx.decl(i) != null) {
			input += visit(ctx.decl(i)) + "\n";
			i++;
		}
		System.out.println(input);
		return input;
	}

	@Override
	public String visitDecl(MiniCParser.DeclContext ctx) {
		String input = "";
		if (ctx.fun_decl() != null) {
			return visit(ctx.fun_decl());
		} else if (ctx.var_decl() != null) {
			return visit(ctx.var_decl());
		}
		return null;

	}

	@Override
	public String visitVar_decl(MiniCParser.Var_declContext ctx) {
		String type = visit(ctx.type_spec());
		String name = ctx.getChild(1).getText();

		if ((ctx.getChild(2).getText()).equals("["))
			return point(dot) + type + " " + name + "[];";

		else {
			if ((ctx.getChild(2).getText()).equals("=")) {
				return point(dot) + type + " " + name + " = " + ctx.getChild(3).getText() + ";";
			} else {
				return point(dot) + type + " " + name + ";";
			}
		}
	}

	@Override
	public String visitType_spec(MiniCParser.Type_specContext ctx) {
		String input = "";
		if (ctx.VOID() != null)
			input = ctx.VOID().getText();
		else if (ctx.INT() != null)
			input = ctx.INT().getText();
		
		return input;
	}

	@Override
	public String visitFun_decl(MiniCParser.Fun_declContext ctx) {
		String type = visit(ctx.type_spec());
		String name = ctx.IDENT().getText();
		String params = visit(ctx.params());
		String contents = visit(ctx.compound_stmt());

		return type + " " + name + " (" + params + ")\n" + point(dot) + "{" + contents + "\n" + point(dot) + "}\n";
	}

	@Override
	public String visitParams(MiniCParser.ParamsContext ctx) {
		if (ctx.VOID() != null) {
			return ctx.VOID().getText();
		} else {
			String input = "";

			for (int i = 0; i < ctx.param().size(); i++) {
				if (i >= 1)
					input += ", ";
				input += visit(ctx.param(i));
			}
			return input;
		}
	}
	

	@Override
	public String visitParam(MiniCParser.ParamContext ctx) {
		String type = visit(ctx.type_spec());
		String name = ctx.IDENT().toString();

		if (ctx.getChild(2) != null)
			return type + " " + name + "[]";
		else
			return type + " " + name;
	}

	@Override
	public String visitStmt(MiniCParser.StmtContext ctx) {
		if (ctx.expr_stmt() != null) {
			dot++;
			return visit(ctx.expr_stmt());

		} else if (ctx.compound_stmt() != null) {
			dot--;

			return visit(ctx.compound_stmt());

		} else if (ctx.if_stmt() != null) {
			dot++;

			return visit(ctx.if_stmt());

		} else if (ctx.while_stmt() != null) {
			dot++;

			return visit(ctx.while_stmt());

		} else if (ctx.return_stmt() != null) {
			dot++;
			return visit(ctx.return_stmt());

		}
		return null;

	}

	@Override
	public String visitExpr_stmt(MiniCParser.Expr_stmtContext ctx) {
		if (ctx.getChildCount() != 3 && ctx.getChild(0).getChild(1).toString().equals("(")) {
			return "\n" + point(dot--) + visit(ctx.expr()) + ";";
		} else
			return point(dot--) + visit(ctx.expr()) + ";";

	}

	@Override
	public String visitCompound_stmt(MiniCParser.Compound_stmtContext ctx) {
		String input = "";
		dot++;

		if (ctx.local_decl().size() != 0) {
			for (int i = 0; i < ctx.local_decl().size(); i++) {
				input += "\n" + visit(ctx.local_decl(i));
			}
		}

		if (ctx.stmt().size() != 0) {
			for (int i = 0; i < ctx.stmt().size(); i++) {
				input += "\n" + visit(ctx.stmt(i));
			}
		}

		return input;
	}

	@Override
	public String visitIf_stmt(MiniCParser.If_stmtContext ctx) {
		String condition = visit(ctx.expr());
		String ifContents = visit(ctx.stmt(0));

		if (ctx.ELSE() != null) {
			String elseContents = visit(ctx.stmt(1));
			return point(dot) + "if (" + condition + ")\n" + point(dot) + "{" + ifContents + "\n"
					+ point(dot) + "}" + "\n" + point(dot) +  "else\n" + point(dot) + "{" + elseContents
					+ "\n" + point(dot--) + "}";
		} else {

			return point(dot) +"if (" + condition + ")\n" + point(dot) + "{" + ifContents + "\n"
					+ point(dot--) + "}";

		}
	}

	@Override
	public String visitWhile_stmt(MiniCParser.While_stmtContext ctx) {
		String whileString = ctx.WHILE().getText();
		String condition = visit(ctx.expr());
		String contents = visit(ctx.stmt());

		return point(dot) + whileString + " (" + condition + ")\n" + point(dot) + "{" + contents + "\n"
				+ point(dot--) + "}";
	}

	@Override
	public String visitReturn_stmt(MiniCParser.Return_stmtContext ctx) {
		if (ctx.expr() != null) {
			return point(dot--) + ctx.RETURN() + " " + visit(ctx.expr()) + ";";

		} else {
			return point(dot--) + ctx.RETURN() + " ;";

		}

	}

	@Override
	public String visitLocal_decl(MiniCParser.Local_declContext ctx) {
		dot++;
		String type = visit(ctx.type_spec());
		String name = ctx.getChild(1).getText();

		if ((ctx.getChild(2).getText()).equals("[")) {
			String length = ctx.getChild(3).getText();
			return point(dot--) + type + " " + name + "[" + length + "];";
		} else if ((ctx.getChild(2).getText()).equals("=")) {
			return point(dot--) + type + " " + name + " = " + ctx.getChild(3).getText() + ";";
		} else {
			return point(dot--) + type + " " + name + ";";
		}
	}

	@Override
	public String visitExpr(MiniCParser.ExprContext ctx) {
		String s1 = null, s2 = null, op = null;

		if (isBinaryOperation(ctx)) {
			if (ctx.IDENT() != null) {
				s1 = ctx.IDENT().getText();
				s2 = visit(ctx.expr(0));
				op = ctx.getChild(1).getText();
				return s1 + " " + op + " " + s2;
			} else {
				s1 = visit(ctx.expr(0));
				s2 = visit(ctx.expr(1));
				op = ctx.getChild(1).getText();
				return s1 + " " + op + " " + s2;
			}

		} else if (isOnlyLiteral(ctx)) {
			s1 = ctx.LITERAL().getText();
			return s1;

		} else if (isOnlyIdent(ctx)) {
			s1 = ctx.IDENT().getText();
			return s1;
		}

		else if (ctx.getChildCount() == 2) {
			s1 = ctx.getChild(1).getText();
			op = ctx.getChild(0).getText();
			return op + s1;
		}

		else if (ctx.getChildCount() == 3) {
			return "(" + visit(ctx.expr(0)) + ")";
		}

		else if (isUseParentheses(ctx)) {
			return ctx.IDENT().getText() + "(" + visit(ctx.args()) + ")";
		}

		else if (isUseSquareBrackets(ctx)) {
			if (ctx.getChildCount() < 5) {
				return ctx.IDENT().getText() + "[" + visit(ctx.expr(0)) + "]";
			} else {
				return ctx.IDENT().getText() + "[" + visit(ctx.expr(0)) + "] = " + visit(ctx.expr(1));
			}
		}
		return null;
	}

	boolean isBinaryOperation(MiniCParser.ExprContext ctx) {
		return ctx.getChildCount() == 3 && ctx.getChild(1) != ctx.expr();
		// 자식 3개짜리 expr 중 ‘(‘ expr ’)’를 배제
	}

	public boolean isOnlyLiteral(MiniCParser.ExprContext ctx) {
		return ctx.getChildCount() == 1 && ctx.LITERAL() != null;
	}

	public boolean isOnlyIdent(MiniCParser.ExprContext ctx) {
		return ctx.getChildCount() == 1 && ctx.IDENT() != null;
	}

	public boolean isUseParentheses(MiniCParser.ExprContext ctx) {
		return ctx.getChildCount() != 3 && ctx.getChild(1).toString().equals("(");
	}

	public boolean isUseSquareBrackets(MiniCParser.ExprContext ctx) {
		return ctx.getChildCount() != 3 && ctx.getChild(1).toString().equals("[");
	}
	
	@Override
	public String visitArgs(MiniCParser.ArgsContext ctx) {
		String input = "";
		for (int i = 0; i < ctx.expr().size(); i++) {
			if (i >= 1)
				input += ", ";
			input += visit(ctx.expr(i));
		}
		return input;
	}
	
	public String point(int dot2) {
		String dots = "";
		for (int i = 1; i < dot2; i++) {
			dots += "....";
		}
		return dots;
	}
}