import java.util.ArrayList;

import org.antlr.v4.runtime.misc.NotNull;

import Tabla_Simbolos.Firma;
import Tabla_Simbolos.TablaSimbolos;
import Tabla_Simbolos.Tipo;
import Tabla_Simbolos.VarDec;


public class EvalVisitor extends DECAFBaseVisitor<Tipo>{
	TablaSimbolos tablaSimbolos = new TablaSimbolos();
	private Firma firmaA;
	private Tipo tipoActual=null;
	private boolean isReturn=false;
	private boolean isMain=false;
	private String iCode="";
	private int position=0;
	private int labelCount;
	private ArrayList<Integer> temps=new ArrayList<Integer>();
	//*********************************************************************************
	public TablaSimbolos getTablaSimbolos() {
		return tablaSimbolos;
	}

	public void setTablaSimbolos(TablaSimbolos tablaSimbolos) {
		this.tablaSimbolos = tablaSimbolos;
	}
	//******************************************************************************
	public Tipo visitProgram(DECAFParser.ProgramContext ctx){
		tablaSimbolos.enter(position);
		Tipo res=super.visitProgram(ctx);
		if(!isMain){
			tablaSimbolos.addError("Missing main method (Line: "+ctx.stop.getCharPositionInLine()+")");
			return tablaSimbolos.incorrect();
		}
		return res;
	}
	//Ingresa a tipo e ingresa sus metodos
	public Tipo visitFactorInput(DECAFParser.FactorInputContext ctx) { 
		//generacion de codigo
		String temp=getTemp();
		addToCode(input(temp));
		Tipo res=tablaSimbolos.intType();
		res.setTemp(temp);;
		return res;
	}
	public Tipo visitPrintStatement(DECAFParser.PrintStatementContext ctx) { 
		Tipo input=visit(ctx.expression());
		if(input.getNombre().equals("char")){
			addToCode(printChar(input.getTemp()));
			freeTemp(input.getTemp());
			return tablaSimbolos.correct();
		}
		else if (input.getNombre().equals("int")){
			addToCode(printNum(input.getTemp()));
			freeTemp(input.getTemp());
			return tablaSimbolos.correct();
		}
		else{
			tablaSimbolos.addError("Print value has to be int or char (Line: "+ctx.stop.getCharPositionInLine()+")");
			return tablaSimbolos.incorrect();
		}
		
	}
	public  Tipo visitMethodDeclaration(DECAFParser.MethodDeclarationContext ctx){
		isReturn = false;
		tablaSimbolos.enter(position);
		String nombre =ctx.ID().getText();
		String tipo=ctx.methodType().getText();
		firmaA=new Firma(nombre,tablaSimbolos.searchTipo(tipo));
		addLabel(methodLabel(firmaA));
		for(int i=0;i<ctx.parameter().size();i++){
			visit(ctx.parameter(i));
		}
		ArrayList<VarDec> parametros=firmaA.getParametros();
		addComment("Loading parameters");
		for(int i=parametros.size()-1;i>=0;i--){
			String temp=getTemp();
			addToCode(pop(temp));
			addToCode(store(parametros.get(i).getPosition()+"",temp));
			freeTemp(temp);
		}
		addToCode("returndir");
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
		position=tablaSimbolos.exit();
		if(ctx.children.size()==0){
			tablaSimbolos.addError("Missing instructions inside { }");
			return tablaSimbolos.incorrect();
		}
		return res;
	}
	public Tipo visitIfStatement(DECAFParser.IfStatementContext ctx){
		String lfalse=newLabel();
		addComment("Empezando if");
		Tipo cond=visit(ctx.expression());
		freeTemp(cond.getTemp());
		addToCode(goToFalse(lfalse,cond.getTemp()));
		if(cond.getNombre().equals("boolean")){
			tablaSimbolos.enter(position);
			Tipo res=visit(ctx.block(0));
			if(ctx.block(1)!=null){
				addLabel(lfalse);
				tablaSimbolos.enter(position);
				res=visit(ctx.block(1));
			}
			else{
				addLabel(lfalse);
			}
			addComment("Terminando if");
			return res;
		}
		else{
			tablaSimbolos.addError("Incorrect type ("+cond.getNombre()+") condition must be boolean type (Line: "+ctx.start.getLine()+")");
			return tablaSimbolos.incorrect();
		}
	}
	
	public Tipo visitWhileStatement(DECAFParser.WhileStatementContext ctx){
		String lcond=newLabel();
		String lend=newLabel();
		addComment("Empeando while");
		addLabel(lcond);
		Tipo cond = visit(ctx.expression());
		freeTemp(cond.getTemp());
		addToCode(goToFalse(lend,cond.getTemp()));
		if(cond.getNombre().equals("boolean")){
			tablaSimbolos.enter(position);
			Tipo res= visit(ctx.block());
			addToCode(goTo(lcond));
			addLabel(lend);
			addComment("Terminando while");
			return res;
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
				String temp=getTemp();
				addToCode(pop(temp));
				addToCode(returnG(temp));
				freeTemp(temp);
				return tablaSimbolos.correct();
			}
			tablaSimbolos.addError("Method type void does not require return value (Line: "+ctx.start.getLine()+")");
			return tablaSimbolos.incorrect();
		}
		else{
			addComment("preparando valor de retorno");
			Tipo returnType=visit(ctx.expression());
			String temp=getTemp();
			addToCode(pop(temp));
			addToCode(push(returnType.getTemp()));
			addToCode(returnG(temp));
			freeTemp(temp);
			freeTemp(returnType.getTemp());
			if(returnType.equals(firmaA.getTipo())){
				if(ctx.getParent().getChild(ctx.parent.getChildCount()-2).getText().startsWith("return")){
					isReturn=true;
				}
				//System.out.println("******"+firmaA.getNombre()+" "+ctx.getParent().getChild(ctx.parent.getChildCount()-1).getText());
				return tablaSimbolos.correct();
			}
			tablaSimbolos.addError("Method type ("+firmaA.getTipo().getNombre()+") and Return type ("+returnType.getNombre()+") are not the same for method: "+firmaA.getNombre()+" (Line: "+ctx.start.getLine()+")");
			return tablaSimbolos.incorrect();
		}
	}
	
	public Tipo visitMethodCallStatement(DECAFParser.MethodCallStatementContext ctx){
		addComment("guardando estado");
		Tipo res=visit(ctx.methodCall());
		freeTemp(res.getTemp());
		return res;
	}
	
	public Tipo visitMethodCall(DECAFParser.MethodCallContext ctx){
		addComment("llamada a metodo");
		addToCode(saveState());
		ArrayList<VarDec> args=new ArrayList<VarDec>();
		String params="";
		addComment("preparando argumentos");
		for(int i=0;i<ctx.expression().size();i++){
			Tipo actual=visit(ctx.expression(i));
			addToCode(push(actual.getTemp()));
			freeTemp(actual.getTemp());
			args.add(new VarDec("temp",actual,0,0));
			params+=" "+actual.getNombre();
			if(actual.getNombre().equals("error")){
				tablaSimbolos.addError("Invalid argument (Line: "+ctx.start.getLine()+")");
				return tablaSimbolos.incorrect();
			}
		}
		
		Firma res =tablaSimbolos.obtenerFirma(ctx.ID().getText(), args);
		addToCode(goToM(res.getLabel()));
		if(res!=null){
			String temp="";
			if(!res.getTipo().getNombre().equals("void")){
				temp=getTemp();
				addToCode(pop(temp));
				addToCode(loadState());
			}
			Tipo resultado=res.getTipo().clone();
			resultado.setTemp(temp);
			return resultado;
		}
		tablaSimbolos.addError("Method "+ctx.ID().getText()+" with parameter types: "+params+" does not exist (Line: "+ctx.start.getLine()+")");
		return tablaSimbolos.incorrect();
	}
	public Tipo visitAssignStatement(DECAFParser.AssignStatementContext ctx){
		addComment("calculando: "+ctx.expression().getText());
		Tipo expression=visit(ctx.expression());
		addComment("obteniendo posicion"+ctx.location().getText());
		Tipo location=visit(ctx.location());
		//codigo de asignacion
		addComment("guardando resultado");
		addToCode(store(location.getTemp(),expression.getTemp()));
		//liberar temporales
		freeTemp(location.getTemp());
		freeTemp(expression.getTemp());
		if(location.getNombre().equals("error")){
			tablaSimbolos.addError("Invalid value location for assignment (Line: "+ctx.start.getLine()+")");
			return tablaSimbolos.incorrect();
		}
		if(location.equals(expression)){
			return tablaSimbolos.correct();
		}
		tablaSimbolos.addError("Location variable and assign value must be the same type (Line: "+ctx.start.getLine()+")");
		return tablaSimbolos.incorrect();
			
	}
	public Tipo visitLocation(DECAFParser.LocationContext ctx){
		VarDec var;
		String currentTemp="";
		if(tipoActual!=null){
			currentTemp=tipoActual.getTemp();
		}
		if(tipoActual!=null){
			var = tipoActual.searchAtribute(ctx.ID().getText());
			if(var==null){
				tablaSimbolos.addError("Struct "+tipoActual.getNombre()+" does not have atribute "+ctx.ID().getText()+" (Line: "+ctx.start.getLine()+")");
				tipoActual=null;
				return tablaSimbolos.incorrect();
			}
			addToCode(binaryOP(currentTemp,"+",currentTemp,var.getPosition()+""));
		}
		else{
			var=tablaSimbolos.searchVar(ctx.ID().getText());
			if(var==null){
				tablaSimbolos.addError("Variable "+ctx.ID().getText()+" not declared (Line: "+ctx.start.getLine()+")");
				return tablaSimbolos.incorrect();
			}
			
			currentTemp=getTemp();
			addToCode(assign(currentTemp,var.getPosition()+""));
			
		}
		if(ctx.expression()==null&&ctx.location()==null){
			tipoActual=null;
			if(var.isList()){
				tablaSimbolos.addError("Cannot operate list (Line: "+ctx.start.getLine()+")");
				return tablaSimbolos.incorrect();
			}
			Tipo res=var.getTipo().clone();
			res.setTemp(currentTemp);
			return res;
		}
		else if(ctx.location()==null){
			tipoActual=null;
			Tipo resultado=visit(ctx.expression());
			if(resultado.getNombre().equals("int")){
				if(var.isList()){
					String offtemp=getTemp();
					addToCode(binaryOP(offtemp,"*",resultado.getTemp(),var.getTipo().getByteSize()+""));
					addToCode(binaryOP(currentTemp,"+",currentTemp,offtemp));
					freeTemp(offtemp);
					Tipo res=var.getTipo().clone();
					res.setTemp(currentTemp);
					return res;
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
					System.out.println("a:"+('a'+0));
					tipoActual.setTemp(currentTemp);
					//addToCode(binaryOP(currentTemp,"+",currentTemp,var.getPosition()+""));
					return visit(ctx.location());
				}
				else{
					Tipo indice=visit(ctx.expression());
					if(indice.getNombre().equals("int")){
						tipoActual=var.getTipo();
						String offset=getTemp();
						addToCode(binaryOP(offset,"*",indice.getTemp(),tipoActual.getByteSize()+""));
						addToCode(binaryOP(currentTemp,"+",offset,currentTemp));
						freeTemp(offset);
						tipoActual.setTemp(currentTemp);
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
		VarDec var =new VarDec(ctx.ID().getText(),tablaSimbolos.searchTipo(ctx.parameterType().getText()),0,position);
		firmaA.addParam(var);
		addComment("paramname: "+var.getNombre()+"-position: "+position);
		position+=var.getByteSize();
		return tablaSimbolos.correct();
	}

	//para declaracion de variables simples
	public Tipo visitDeclSimple(DECAFParser.DeclSimpleContext ctx){
		String nameType=ctx.varType().getText();
		addComment("varname: "+ctx.ID().getText()+"-position: "+position);
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
				if(tablaSimbolos.entry(new VarDec(ctx.ID().getText(),found,0,position)).getNombre().equals("error")){
					return tablaSimbolos.incorrect();
				}
				//actualizar la posicion
				position+=found.getByteSize();
				
				return tablaSimbolos.correct();
			}
			else{
				Tipo found=tablaSimbolos.searchTipo(nameType);
				if(found==null){
					tablaSimbolos.addError("Type"+nameType+" non existent (Line: "+ctx.start.getLine()+")");
					return tablaSimbolos.incorrect();
				}
				if(tipoActual.addAtrib(new VarDec(ctx.ID().getText(),found,0,position))){
					//actualizar la posicion
					position+=found.getByteSize();
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
		addComment("varname: "+ctx.ID().getText()+"-position: "+position);
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
				if(tablaSimbolos.entry(new VarDec(ctx.ID().getText(),found,Integer.parseInt(ctx.NUM().getText()),position)).getNombre().equals("error")){
					return tablaSimbolos.incorrect();
				}
				//actualizar posicion
				position+=found.getByteSize()*Integer.parseInt(ctx.NUM().getText());
				return tablaSimbolos.correct();
			}
			else{
				Tipo found=tablaSimbolos.searchTipo(nameType);
				if(found==null){
					tablaSimbolos.addError("Type: "+nameType+" non existent (Line: "+ctx.start.getLine()+")");
					return tablaSimbolos.incorrect();
				}
				if(tipoActual.addAtrib(new VarDec(ctx.ID().getText(),found,Integer.parseInt(ctx.NUM().getText()),position))){
					//actualizar posicion
					position+=found.getByteSize()*Integer.parseInt(ctx.NUM().getText());
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
		Tipo res=tablaSimbolos.intType();
		String temp=getTemp();
		res.setTemp(temp);
		addToCode(assign(temp,ctx.getText()));
		return res;
	}
	
	public Tipo visitChar_literal(DECAFParser.Char_literalContext ctx){
		Tipo res=tablaSimbolos.charType();
		String temp=getTemp();
		res.setTemp(temp);
		
		addToCode(assign(temp,(ctx.getText().charAt(1)+0)+""));
		return res;
	}
	public Tipo visitBool_literal(DECAFParser.Bool_literalContext ctx){
		Tipo res=tablaSimbolos.boolType();
		String temp=getTemp();
		res.setTemp(temp);
		String value="";
		if(ctx.getText().equals("true")){
			value="0";
		}
		else{
			value="1";
		}
		addToCode(assign(temp,value));
		return res;
	}
	
	public Tipo visitExpression1(DECAFParser.Expression1Context ctx){
		String ltrue=newLabel();
		String lfalse=newLabel();
		String lend=newLabel();
		Tipo tipo1 = visit(ctx.expression());
		addToCode(goToTrue(ltrue,tipo1.getTemp()));
		Tipo tipo2 = visit(ctx.expr1());
		addToCode(goToTrue(ltrue,tipo1.getTemp()));
		addToCode(assign(tipo1.getTemp(),"0"));
		goTo(lend);
		addLabel(ltrue);
		addToCode(assign(tipo1.getTemp(),"1"));
		addLabel(lend);
		freeTemp(tipo2.getTemp());
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
		String lfalse=newLabel();
		String lend=newLabel();
		Tipo tipo1 = visit(ctx.expr1());
		addToCode(goToFalse(lfalse,tipo1.getTemp()));
		Tipo tipo2 = visit(ctx.expr2());
		addToCode(goToFalse(lfalse,tipo2.getTemp()));
		addToCode(assign(tipo1.getTemp(),"1"));
		goTo(lend);
		addLabel(lfalse);
		addToCode(assign(tipo1.getTemp(),"0"));
		addLabel(lend);
		freeTemp(tipo2.getTemp());
		if(tipo1.getNombre().equals("boolean")&&tipo2.getNombre().equals("boolean")){
			Tipo res=tablaSimbolos.boolType();
			res.setTemp(tipo1.getTemp());
			return res;
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
		freeTemp(tipo2.getTemp());
		if(tipo1.equals(tipo2)){
			Tipo res=tablaSimbolos.boolType();
			addToCode(binaryOP(tipo1.getTemp(),ctx.eq_op().getText(),tipo1.getTemp(),tipo2.getTemp()));
			res.setTemp(tipo1.getTemp());
			return res;
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
		freeTemp(tipo2.getTemp());
		if(tipo1.getNombre().equals("int")&&tipo2.getNombre().equals("int")){
			Tipo res=tablaSimbolos.boolType();
			addToCode(binaryOP(tipo1.getTemp(),ctx.rel_op().getText(),tipo1.getTemp(),tipo2.getTemp()));
			res.setTemp(tipo1.getTemp());
			return res;
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
		freeTemp(tipo2.getTemp());
		if(tipo1.getNombre().equals("int")&&tipo2.getNombre().equals("int")){
			Tipo res=tablaSimbolos.intType();
			addToCode(binaryOP(tipo1.getTemp(),ctx.arith_menor().getText(),tipo1.getTemp(),tipo2.getTemp()));
			res.setTemp(tipo1.getTemp());
			return res;
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
		freeTemp(tipo2.getTemp());
		if(tipo1.getNombre().equals("int")&&tipo2.getNombre().equals("int")){
			Tipo res=tablaSimbolos.intType();
			addToCode(binaryOP(tipo1.getTemp(),ctx.arith_mayor().getText(),tipo1.getTemp(),tipo2.getTemp()));
			res.setTemp(tipo1.getTemp());
			return res;
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
				Tipo res=tablaSimbolos.intType();
				res.setTemp(tipo.getTemp());
				return res;
			}
			else{
				Tipo res=tablaSimbolos.charType();
				res.setTemp(tipo.getTemp());
				return res;
			}
		}
		tablaSimbolos.addError("Cast is only for int and char values (Line: "+ctx.start.getLine()+")");
		return tablaSimbolos.incorrect();
	}
	
	public Tipo visitUniFactorNeg(DECAFParser.UniFactorNegContext ctx){
		Tipo tipo = visit(ctx.factor());
		if(tipo.getNombre().equals("int")){
			Tipo res=tablaSimbolos.intType();
			addToCode(unaryOP(res.getTemp(),"-",res.getTemp()));
			res.setTemp(tipo.getTemp());
			return res;
		}
		tablaSimbolos.addError("- is only for int values (Line: "+ctx.start.getLine()+")");
		return tablaSimbolos.incorrect();
	}
	
	public Tipo visitUniFactorCom(DECAFParser.UniFactorComContext ctx){
		Tipo tipo = visit(ctx.factor());
		if(tipo.getNombre().equals("boolean")){
			addToCode(unaryOP(tipo.getTemp(),"!",tipo.getTemp()));
			Tipo res=tablaSimbolos.boolType();
			res.setTemp(tipo.getTemp());
			return res;
		}
		tablaSimbolos.addError("! is only for boolean values (Line: "+ctx.start.getLine()+")");
		return tablaSimbolos.incorrect();
	}
	
	public Tipo visitUniFactorF(DECAFParser.UniFactorFContext ctx){
		return visit(ctx.factor());
	}
	
	public Tipo visitFactorLocation(DECAFParser.FactorLocationContext ctx){
		Tipo res=visit(ctx.location());
		addToCode(load(res.getTemp(),res.getTemp()));
		return res;
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
	//metodos para codigo intermedio
	public String getIntermidiateCode(){
		return iCode;
	}
	//produccion de codigo intermedio
	public void addToCode(String code){
		iCode+=getTabs()+code+"\n";
	}
	public void addLabel(String label){
		addToCode(label+":");
	}
	public void addComment(String comment){
		addToCode("--"+comment);
	}
	//generaccion de instrucciones
	public String store(String dir,String value){
		String res="st_"+dir+"_"+value;
		return res;
	}
	public String load(String dir,String location){
		String res="ld_"+dir+"_"+location;
		return res;
	}
	public String assign(String dir,String val){
		String res="mv_"+dir+"_"+val;
		return res;
	}
	
	public String binaryOP(String dir,String op,String val1,String val2){
		if(op.equals("+")){
			op="add";
		}
		else if(op.equals("-")){
			op="sub";
		}
		else if(op.equals("*")){
			op="mul";
		}
		else if(op.equals("/")){
			op="div";
		}
		else if(op.equals("%")){
			op="mod";
		}
		else if(op.equals("<")){
			op="lt";
		}
		else if(op.equals(">")){
			op="gt";
		}
		else if(op.equals("<=")){
			op="lte";
		}
		else if(op.equals(">=")){
			op="gte";
		}
		else if(op.equals("||")){
			op="or";
		}
		else if(op.equals("&&")){
			op="and";
		}
		else if(op.equals("==")){
			op="eq";
		}
		else if(op.equals("!=")){
			op="neq";
		}
		String res=op+"_"+dir+"_"+val1+"_"+val2;
		return res;
	}
	public String unaryOP(String dir, String op,String val){
		if(op.equals("!")){
			op="not";
		}
		else if(op.equals("-")){
			op="neg";
		}
		return op+"_"+dir+"_"+val;
	}
	public String goToFalse(String label,String val){
		String res="GoToF_"+label+"_"+val;
		return res;
	}
	public String goToTrue(String label,String val){
		String res="GoToT_"+label+"_"+val;
		return res;
	}
	public String goTo(String label){
		String res="GoTo_"+label;
		return res;
	}
	public String goToM(String method){
		String res="GoToM_"+method;
		return res;
	}
	public String push(String temp){
		String res="push_"+temp;
		return res;
	}
	public String pop(String temp){
		String res="pop_"+temp;
		return res;
	}
	public String returnG(String dir){
		String res="returnG_"+dir;
		return res;
	}
	public String getTabs(){
		String res="";
		int tabs=tablaSimbolos.getAmbitos().size()-2;
		for(int i=0;i<tabs;i++){
			res+="\t";
		}
		return res;
	}
	public String input(String input){
		String res="input_"+input;
		return res;
	}
	public String printChar(String temp){
		String res="printc_"+temp;
		return res;
	}
	public String printNum(String temp){
		String res="printn_"+temp;
		return res;
	}
	//labels
	public String newLabel(){
		labelCount++;
		String res="label"+labelCount;
		return res;
	}
	public String methodLabel(Firma a){
		return tablaSimbolos.newMethodLabel(a);
	}
	//asignaccion de temporales
	public String getTemp(){
		int count=0;
		boolean found=true;
		while (found){
			found=false;
			for(int i=0;i<temps.size();i++){
				if(count==temps.get(i)){
					found=true;
					count++;
					break;
				}
			}
			
		}
		temps.add(count);
		return "t"+count;
	}
	public void freeTemp(String temp){
		int value=Integer.parseInt(temp.substring(1));
		for(int i=0;i<temps.size();i++){
			if(value==temps.get(i)){
				temps.remove(i);
				return;
			}
		}
	}
	//instrucciones para guardar en la pila los valores
	public String saveVar(VarDec var,int currentP){
		String res="";
		if(var.isList()){
			for(int i=0;i<var.getLongitud();i++){
				VarDec newvar=var.clone();
				newvar.setLongitud(0);
				res+=saveVar(newvar,currentP);
				currentP+=newvar.getByteSize();
			}
		}
		else if(var.isStruct()){
			ArrayList<VarDec> atributos=var.getTipo().getAtributos();
			for(int i=0;i<atributos.size();i++){
				res+=saveVar(atributos.get(i),currentP);
				currentP+=atributos.get(i).getByteSize();
			}
		}
		else{
			String temp=getTemp();
			res+=getTabs()+load(currentP+"",temp)+"\n";
			res+=getTabs()+push(temp)+"\n";
			freeTemp(temp);
		}
		return res;
	}
	public String saveState(){
		String res="";
		int count=tablaSimbolos.endGlobal();
		ArrayList<VarDec>state=tablaSimbolos.getState();
		for(int i=0;i<state.size();i++){
			res+=saveVar(state.get(i),count);
			count+=state.get(i).getByteSize();
		}
		return res;
	}
	//instruccionse para cargar de la pila los valores
	public String loadVar(VarDec var,int currentP){
		String res="";
		if(var.isList()){
			for(int i=var.getLongitud()-1;i>=0;i--){
				VarDec newvar=var.clone();
				newvar.setLongitud(0);
				res+=saveVar(newvar,currentP);
				currentP-=newvar.getByteSize();
			}
		}
		else if(var.isStruct()){
			ArrayList<VarDec> atributos=var.getTipo().getAtributos();
			for(int i=atributos.size()-1;i>=0;i--){
				res+=saveVar(atributos.get(i),currentP);
				currentP-=atributos.get(i).getByteSize();
			}
		}
		else{
			String temp=getTemp();
			res+=getTabs()+pop(temp)+"\n";
			res+=getTabs()+store(currentP+"",temp)+"\n";
			freeTemp(temp);
		}
		return res;
	}
	public String loadState(){
		String res="";
		int count=tablaSimbolos.lastDir();
		ArrayList<VarDec>state=tablaSimbolos.getState();
		for(int i=state.size()-1;i>=0;i--){
			res+=loadVar(state.get(i),count);
			count-=state.get(i).getByteSize();
		}
		return res;
	}
	public int getMax(){
		return tablaSimbolos.getMax();
	}
}
