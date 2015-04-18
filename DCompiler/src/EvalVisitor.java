import java.util.ArrayList;

import Tabla_Simbolos.Firma;
import Tabla_Simbolos.TablaSimbolos;
import Tabla_Simbolos.Tipo;
import Tabla_Simbolos.VarDec;


public class EvalVisitor extends DECAFBaseVisitor<Tipo>{
	TablaSimbolos tablaSimbolos = new TablaSimbolos();
	//getters y setters
	private Firma firmaA;
	private Tipo tipoActual=null;
	boolean isReturn=false;
	boolean isMain=false;
	//*********************************************************************************
	public TablaSimbolos getTablaSimbolos() {
		return tablaSimbolos;
	}

	public void setTablaSimbolos(TablaSimbolos tablaSimbolos) {
		this.tablaSimbolos = tablaSimbolos;
	}
	//******************************************************************************
	public Tipo visitProgram(DECAFParser.ProgramContext ctx){
		tablaSimbolos.enter();
		Tipo res=super.visitProgram(ctx);
		if(!isMain){
			tablaSimbolos.addError("Missing main method (Line: "+ctx.stop.getCharPositionInLine()+")");
			return tablaSimbolos.incorrect();
		}
		return res;
	}
	//Ingresa a tipo e ingresa sus metodos
	
	public  Tipo visitMethodDeclaration(DECAFParser.MethodDeclarationContext ctx){
		isReturn = false;
		tablaSimbolos.enter();
		String nombre =ctx.ID().getText();
		String tipo=ctx.methodType().getText();
		firmaA=new Firma(nombre,tablaSimbolos.searchTipo(tipo));
		
		for(int i=0;i<ctx.parameter().size();i++){
			visit(ctx.parameter(i));
		}
		Tipo resultado = tablaSimbolos.entry(firmaA.clone());
		if(!resultado.getNombre().equals("error")){
			resultado=visit(ctx.block());
			if(!isReturn&&!ctx.methodType().getText().equals("void")){
				tablaSimbolos.addError("Method "+firmaA.getNombre()+" missing return statement at end of method (Line: "+ctx.start.getLine()+")");
				return tablaSimbolos.incorrect();
			}
			if(ctx.ID().getText().equals("main")){
				isMain=true;
			}
			return tablaSimbolos.correct();
		}
		return tablaSimbolos.incorrect();
	}
	//permite visitar el contenido dentro de block
	public Tipo visitBlock(DECAFParser.BlockContext ctx){
		Tipo res=super.visitBlock(ctx);
		tablaSimbolos.exit();
		if(ctx.children.size()==0){
			tablaSimbolos.addError("Missing instructions inside { }");
			return tablaSimbolos.incorrect();
		}
		return res;
	}
	public Tipo visitIfStatement(DECAFParser.IfStatementContext ctx){
		Tipo cond=visit(ctx.expression());
		if(cond.getNombre().equals("boolean")){
			tablaSimbolos.enter();
			Tipo res=visit(ctx.block(0));
			if(ctx.block(1)!=null){
				tablaSimbolos.enter();
				res=visit(ctx.block(1));
			}
			return res;
		}
		else{
			tablaSimbolos.addError("Incorrect type ("+cond.getNombre()+") condition must be boolean type (Line: "+ctx.start.getLine()+")");
			return tablaSimbolos.incorrect();
		}
	}
	
	public Tipo visitWhileStatement(DECAFParser.WhileStatementContext ctx){
		Tipo cond = visit(ctx.expression());
		if(cond.getNombre().equals("boolean")){
			tablaSimbolos.enter();
			return visit(ctx.block());
		}
		else{
			tablaSimbolos.addError("Condition must be boolean type: "+ctx.start.getLine());
			return tablaSimbolos.incorrect();
		}
	}
	
	public Tipo visitReturnStatement(DECAFParser.ReturnStatementContext ctx){
		if(firmaA.getTipo().getNombre().equals("void")){
			if(ctx.expression()==null){
				isReturn=true;
				return tablaSimbolos.correct();
			}
			tablaSimbolos.addError("Method type void does not require return value (Line: "+ctx.start.getLine()+")");
			return tablaSimbolos.incorrect();
		}
		else{
			Tipo returnType=visit(ctx.expression());
			if(returnType.equals(firmaA.getTipo())){
				if(ctx.getParent().getChild(ctx.parent.getChildCount()-2).getText().startsWith("return")){
					isReturn=true;
				}
				System.out.println("********"+ctx.getParent().getClass());
				//System.out.println("******"+firmaA.getNombre()+" "+ctx.getParent().getChild(ctx.parent.getChildCount()-1).getText());
				return tablaSimbolos.correct();
			}
			tablaSimbolos.addError("Method type ("+firmaA.getTipo().getNombre()+") and Return type ("+returnType.getNombre()+") are not the same for method: "+firmaA.getNombre()+" (Line: "+ctx.start.getLine()+")");
			return tablaSimbolos.incorrect();
		}
	}
	
	public Tipo visitMethodCallStatement(DECAFParser.MethodCallStatementContext ctx){
		return visit(ctx.methodCall());
	}
	
	public Tipo visitMethodCall(DECAFParser.MethodCallContext ctx){
		ArrayList<VarDec> args=new ArrayList<VarDec>();
		String params="";
		for(int i=0;i<ctx.expression().size();i++){
			Tipo actual=visit(ctx.expression(i));
			args.add(new VarDec("temp",actual,0));
			params+=" "+actual.getNombre();
			if(actual.getNombre().equals("error")){
				tablaSimbolos.addError("Invalid argument (Line: "+ctx.start.getLine()+")");
				return tablaSimbolos.incorrect();
			}
		}
		Firma res =tablaSimbolos.obtenerFirma(ctx.ID().getText(), args);
		if(res!=null){
			return res.getTipo();
		}
		tablaSimbolos.addError("Method "+ctx.ID().getText()+" with parameter types: "+params+" does not exist (Line: "+ctx.start.getLine()+")");
		return tablaSimbolos.incorrect();
	}
	public Tipo visitAssignStatement(DECAFParser.AssignStatementContext ctx){
		Tipo location=visit(ctx.location());
		if(location.getNombre().equals("error")){
			tablaSimbolos.addError("Invalid value location for assignment (Line: "+ctx.start.getLine()+")");
			return tablaSimbolos.incorrect();
		}
		if(location.equals(visit(ctx.expression()))){
			return tablaSimbolos.correct();
		}
		tablaSimbolos.addError("Location variable and assign value must be the same type (Line: "+ctx.start.getLine()+")");
		return tablaSimbolos.incorrect();
			
	}
	public Tipo visitLocation(DECAFParser.LocationContext ctx){
		VarDec var;
		System.out.println(ctx.getText()+" ");
		if(tipoActual!=null){
			var = tipoActual.searchAtribute(ctx.ID().getText());
			if(var==null){
				tablaSimbolos.addError("Struct "+tipoActual.getNombre()+" does not have atribute "+ctx.ID().getText()+" (Line: "+ctx.start.getLine()+")");
				tipoActual=null;
				return tablaSimbolos.incorrect();
			}
		}
		else{
			var=tablaSimbolos.searchVar(ctx.ID().getText());
			if(var==null){
				tablaSimbolos.addError("Variable "+ctx.ID().getText()+" not declared (Line: "+ctx.start.getLine()+")");
				return tablaSimbolos.incorrect();
			}
		}
		if(ctx.expression()==null&&ctx.location()==null){
			tipoActual=null;
			if(var.isList()){
				tablaSimbolos.addError("Cannot operate list (Line: "+ctx.start.getLine()+")");
				return tablaSimbolos.incorrect();
			}
			System.out.println("var "+var);
			return var.getTipo();
		}
		else if(ctx.location()==null){
			tipoActual=null;
			if(visit(ctx.expression()).getNombre().equals("int")){
				if(var.isList()){
					return var.getTipo();
				}
				else{
					tablaSimbolos.addError("Cannot acces index on a non list variable (Line: "+ctx.start.getLine()+")");
					return tablaSimbolos.incorrect();
				}
			}
			else{
				tablaSimbolos.addError("Index must be int type (Line: "+ctx.start.getLine()+")");
				return tablaSimbolos.incorrect();
			}
		}
		else{
			if(var.getTipo().getAtributos()!=null){
				if(ctx.expression()==null){
					tipoActual=var.getTipo(); //tipoActual
					if(var.isList()){
						tipoActual=null;
						tablaSimbolos.addError("Cannot operate list, must use indexes (Line: "+ctx.start.getLine()+")");
						return tablaSimbolos.incorrect();
					}
					return visit(ctx.location());
				}
				else{
					if(visit(ctx.expression()).getNombre().equals("int")){
						tipoActual=var.getTipo();
						return visit(ctx.location());
					}
					else{
						tipoActual=null;
						tablaSimbolos.addError("Index must be int type (Line: "+ctx.start.getLine()+")");
						return tablaSimbolos.incorrect();
					}
				}
			}
			else{
				tablaSimbolos.addError("Must be struct type (Line: "+ctx.start.getLine());
				return tablaSimbolos.incorrect();
			}
			
		}
	}
	
	//para parametros que no son arrays
	public Tipo visitParameter(DECAFParser.ParameterContext ctx){
		VarDec var =new VarDec(ctx.ID().getText(),tablaSimbolos.searchTipo(ctx.parameterType().getText()),0);
		firmaA.addParam(var);
		return tablaSimbolos.correct();
	}

	//para declaracion de variables simples
	public Tipo visitDeclSimple(DECAFParser.DeclSimpleContext ctx){
		String nameType=ctx.varType().getText();
		if(nameType.startsWith("s")){
			nameType=nameType.replace("struct", "");
			nameType=nameType.replace(" ", "");
		}
		try {
			if(tipoActual==null){
				Tipo found=tablaSimbolos.searchTipo(nameType);
				if(found==null){
					tablaSimbolos.addError("Type: "+nameType+" non existent (Line: "+ctx.start.getLine()+")");
					return tablaSimbolos.incorrect();
				}
				if(tablaSimbolos.entry(new VarDec(ctx.ID().getText(),found,0)).getNombre().equals("error")){
					return tablaSimbolos.incorrect();
				}
				return tablaSimbolos.correct();
			}
			else{
				Tipo found=tablaSimbolos.searchTipo(nameType);
				if(found==null){
					tablaSimbolos.addError("Type"+nameType+" non existent (Line: "+ctx.start.getLine()+")");
					return tablaSimbolos.incorrect();
				}
				if(tipoActual.addAtrib(new VarDec(ctx.ID().getText(),found,0))){
					return tablaSimbolos.correct();
				}
				tablaSimbolos.addError("Atribute "+ctx.ID().getText()+" alredy exits (Line: "+ctx.start.getLine()+")");
				return tablaSimbolos.incorrect();
			}
		} catch (Exception e) {	
			tablaSimbolos.addError(e.getMessage());
			return tablaSimbolos.correct();
		}
	}
	//para declaracion de variables array
	public Tipo visitDeclArray(DECAFParser.DeclArrayContext ctx){
		String nameType=ctx.varType().getText();
		if(nameType.startsWith("s")){
			nameType=nameType.replace("struct", "");
			nameType=nameType.replace(" ", "");
		}
		try {
			if(Integer.parseInt(ctx.NUM().getText())<1){
				tablaSimbolos.addError("Array Length must be higher then 0 (Line: "+ctx.start.getLine()+")");
				return tablaSimbolos.incorrect();
			}
			if(tipoActual==null){
				Tipo found=tablaSimbolos.searchTipo(nameType);
				if(found==null){
					tablaSimbolos.addError("Type: "+nameType+" non existent (Line: "+ctx.start.getLine()+")");
					return tablaSimbolos.incorrect();
				}
				if(tablaSimbolos.entry(new VarDec(ctx.ID().getText(),found,Integer.parseInt(ctx.NUM().getText()))).getNombre().equals("error")){
					return tablaSimbolos.incorrect();
				}
				return tablaSimbolos.correct();
			}
			else{
				Tipo found=tablaSimbolos.searchTipo(nameType);
				if(found==null){
					tablaSimbolos.addError("Type: "+nameType+" non existent (Line: "+ctx.start.getLine()+")");
					return tablaSimbolos.incorrect();
				}
				if(tipoActual.addAtrib(new VarDec(ctx.ID().getText(),found,Integer.parseInt(ctx.NUM().getText())))){
					return tablaSimbolos.correct();
				}
				tablaSimbolos.addError("Atribute "+ctx.ID().getText()+" alredy exits (Line: "+ctx.start.getLine()+")");
				return tablaSimbolos.incorrect();
			}
		} catch (Exception e) {	
			tablaSimbolos.addError(e.getMessage());
			return tablaSimbolos.correct();
		}
	}
	
	public Tipo visitLiteral(DECAFParser.LiteralContext ctx){
		return super.visitLiteral(ctx);
	}
	
	public Tipo visitInt_literal(DECAFParser.Int_literalContext ctx){
		return tablaSimbolos.intType();
	}
	
	public Tipo visitChar_literal(DECAFParser.Char_literalContext ctx){
		return tablaSimbolos.charType();
	}
	public Tipo visitBool_literal(DECAFParser.Bool_literalContext ctx){
		return tablaSimbolos.boolType();
	}
	
	public Tipo visitExpression1(DECAFParser.Expression1Context ctx){
		Tipo tipo1 = visit(ctx.expression());
		Tipo tipo2 = visit(ctx.expr1());
		if(tipo1.getNombre().equals("boolean")&&tipo2.getNombre().equals("boolean")){
			return tablaSimbolos.boolType();
		}
		tablaSimbolos.addError("OR operator is for boolean values (Line: "+ctx.start.getLine()+")");
		return tablaSimbolos.incorrect();
	}
	
	public Tipo visitExpression2(DECAFParser.Expression2Context ctx){
		return visit(ctx.expr1());
	}
	
	public Tipo visitExpr11(DECAFParser.Expr11Context ctx){
		Tipo tipo1 = visit(ctx.expr1());
		Tipo tipo2 = visit(ctx.expr2());
		if(tipo1.getNombre().equals("boolean")&&tipo2.getNombre().equals("boolean")){
			return tablaSimbolos.boolType();
		}
		tablaSimbolos.addError("AND operator is for boolean values (Line: "+ctx.start.getLine()+")");
		return tablaSimbolos.incorrect();
	}
	
	public Tipo visitExpr12(DECAFParser.Expr12Context ctx){
		return visit(ctx.expr2());
	}
	
	public Tipo visitExpr21(DECAFParser.Expr21Context ctx){
		Tipo tipo1 = visit(ctx.expr2());
		Tipo tipo2 = visit(ctx.expr3());
		if(tipo1.equals(tipo2)){
			return tablaSimbolos.boolType();
		}
		tablaSimbolos.addError("Values must be of the same type (Line: "+ctx.start.getLine()+")");
		return tablaSimbolos.incorrect();
	}
	
	public Tipo visitExpr22(DECAFParser.Expr22Context ctx){
		return visit(ctx.expr3());
	}
	
	public Tipo visitExpr31(DECAFParser.Expr31Context ctx){
		Tipo tipo1 = visit(ctx.expr3());
		Tipo tipo2 = visit(ctx.expr4());
		if(tipo1.getNombre().equals("int")&&tipo2.getNombre().equals("int")){
			return tablaSimbolos.boolType();
		}
		tablaSimbolos.addError("Rel operators must be for int values (Line: "+ctx.start.getLine()+")");
		return tablaSimbolos.incorrect();
	}
	public Tipo visitExpr32(DECAFParser.Expr32Context ctx){
		return visit(ctx.expr4());
	}
	
	public Tipo visitExpr41(DECAFParser.Expr41Context ctx){
		Tipo tipo1 = visit(ctx.expr4());
		Tipo tipo2 = visit(ctx.expr5());
		if(tipo1.getNombre().equals("int")&&tipo2.getNombre().equals("int")){
			return tablaSimbolos.intType();
		}
		tablaSimbolos.addError("Arith operators must be for int values (Line: "+ctx.start.getLine()+")");
		return tablaSimbolos.incorrect();
	}
	
	public Tipo visitExpr42(DECAFParser.Expr42Context ctx){
		return visit(ctx.expr5());
	}
	
	public Tipo visitExpr51(DECAFParser.Expr51Context ctx){
		Tipo tipo1 = visit(ctx.expr5());
		Tipo tipo2 = visit(ctx.uniFactor());
		if(tipo1.getNombre().equals("int")&&tipo2.getNombre().equals("int")){
			return tablaSimbolos.intType();
		}
		tablaSimbolos.addError("Arith operators must be for int values (Line: "+ctx.start.getLine()+")");
		return tablaSimbolos.incorrect();
	}
	public Tipo visitExpr52(DECAFParser.Expr52Context ctx){
		return visit(ctx.uniFactor());
	}
	public Tipo visitUniFactorCast(DECAFParser.UniFactorCastContext ctx){
		Tipo tipo = visit(ctx.factor());
		if(tipo.getNombre().equals("int")||tipo.getNombre().equals("char")){
			if(ctx.castingType().getText().equals("int")){
				return tablaSimbolos.intType();
			}
			else{
				return tablaSimbolos.charType();
			}
		}
		tablaSimbolos.addError("Cast is only for int and char values (Line: "+ctx.start.getLine()+")");
		return tablaSimbolos.incorrect();
	}
	
	public Tipo visitUniFactorNeg(DECAFParser.UniFactorNegContext ctx){
		Tipo tipo = visit(ctx.factor());
		if(tipo.getNombre().equals("int")){
			return tablaSimbolos.intType();
		}
		tablaSimbolos.addError("- is only for int values (Line: "+ctx.start.getLine()+")");
		return tablaSimbolos.incorrect();
	}
	
	public Tipo visitUniFactorCom(DECAFParser.UniFactorComContext ctx){
		Tipo tipo = visit(ctx.factor());
		if(tipo.getNombre().equals("boolean")){
			return tablaSimbolos.boolType();
		}
		tablaSimbolos.addError("! is only for boolean values (Line: "+ctx.start.getLine()+")");
		return tablaSimbolos.incorrect();
	}
	
	public Tipo visitUniFactorF(DECAFParser.UniFactorFContext ctx){
		return visit(ctx.factor());
	}
	
	public Tipo visitFactorLocation(DECAFParser.FactorLocationContext ctx){
		return visit(ctx.location());
	}
	
	public Tipo visitMethodCall(DECAFParser.FactorMethodCallContext ctx){
		Tipo tipo=visit(ctx.methodCall());
		if(tipo.getNombre().equals("void")){
			tablaSimbolos.addError("Cannot operate void value (Line: "+ctx.start.getLine()+")");
		}
		return tablaSimbolos.incorrect();
	}
	
	public Tipo visitFactorLiteral(DECAFParser.FactorLiteralContext ctx){
		return visit(ctx.literal());
	}
	
	public Tipo visitFactorExpression(DECAFParser.FactorExpressionContext ctx){
		return visit(ctx.expression());
	}
	
	public Tipo visitStructDeclaration(DECAFParser.StructDeclarationContext ctx){
		tipoActual=new Tipo(ctx.ID().getText());
		for(int x=0;x<ctx.varDeclaration().size();x++){
			visit(ctx.varDeclaration(x));
		}
		Tipo res = tablaSimbolos.entry(tipoActual.clone());
		tipoActual=null;
		return res;
	}
}
