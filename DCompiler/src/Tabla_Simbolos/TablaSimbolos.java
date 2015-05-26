package Tabla_Simbolos;

import java.util.ArrayList;
import java.util.Stack;

public class TablaSimbolos {
	private Stack<TablaVariables> ambitos;
	private TablaTipos tipos;
	private TablaMetodos metodos;
	private int max=-1;
	String errors="";
	//constructor
	public TablaSimbolos(){
		ambitos=new Stack<TablaVariables>();
		tipos= new TablaTipos();
		metodos=new TablaMetodos();
	}
	//add error
	public void addError(String error){
		errors+="\n"+error;
	}
	public String getErrors(){
		return errors;
	}
	public void setErrors(String errors){
		this.errors=errors;
	}
	//entrada a un ambito
	public void enter(int position){
		ambitos.push(new TablaVariables());
	}
	//salida de un ambito
	public int exit(){
		TablaVariables exit=ambitos.pop();
		calculateMax();
		return exit.getStartPos();
	}
	//declaracion de un metodo
	public Tipo entry(Firma metodo){
		ArrayList<VarDec> list=metodo.getParametros();
		try {
			metodos.entry(metodo);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			addError(e1.getLocalizedMessage());
			return incorrect();
		}
		boolean error=false;
		for(int i=0;i<list.size();i++){
			if(tipos.primitiveSearch(list.get(i).getTipo().getNombre())==null){
				addError("Not permited parameter type");
				error =true;
			}
			else{
				try{
					entry(list.get(i));
				}
				catch(Exception e){
					addError(e.getMessage());
					error=true;
				}
			}
		}
		if(error){
			return incorrect();
		}
		return correct();
	}
	//declaraion de un tipo
	public Tipo entry(Tipo tipo){
		try {
			tipos.entry(tipo);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			addError(e.getMessage());
			return incorrect();
		}
		return correct();
	}
	//declaraion de una variable
	public Tipo entry(VarDec var){
		TablaVariables actual = ambitos.peek();
		try {
			actual.entry(var);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			addError(e.getMessage());
			return incorrect();
		}
		return correct();
	}
	//busqueda de un metodo
	public ArrayList<Firma> searchMetodo(String nombre){
		return metodos.search(nombre);
	}
	//busqueda de un tipo
	public Tipo searchTipo(String nombre){
		return tipos.search(nombre);
	}
	//busqueda de variable
	public VarDec searchVar(String nombre){
		VarDec res;
		for(int x=ambitos.size()-1;x>=0;x--){
			TablaVariables temp=ambitos.get(x);
			res=temp.search(nombre);
			if(res!=null){
				return res; 
			}
		}
		return null;
	}
	//obtencion de metodo
	public Firma obtenerFirma(String nombre,ArrayList<VarDec> parametros){
		Firma temp = new Firma(nombre,null,parametros);
		return metodos.search(temp);
	}
	public String newMethodLabel(Firma a){
		return metodos.newLabel(a);
	}
	public Tipo correct(){
		return tipos.voidType().clone();
	}
	public Tipo incorrect(){
		return tipos.errorType().clone();
	}
	public Tipo intType(){
		return tipos.intType().clone();
	}
	public Tipo charType(){
		return tipos.charType().clone();
	}
	public Tipo boolType(){
		return tipos.boolType().clone();
	}
	public String getFirmas(){
		return metodos+"";
	}
	public String getTipos(){
		return tipos+"";
	}
	//getters y setters
	public Stack<TablaVariables> getAmbitos() {
		return ambitos;
	}
	public void setAmbitos(Stack<TablaVariables> ambitos) {
		this.ambitos = ambitos;
	}
	public TablaMetodos getMetodos() {
		return metodos;
	}
	public void setMetodos(TablaMetodos metodos) {
		this.metodos = metodos;
	}
	public void setTipos(TablaTipos tipos) {
		this.tipos = tipos;
	}
	public ArrayList<VarDec> getState(){
		ArrayList<VarDec> state=new ArrayList<VarDec>();
		for(int i=1;i<ambitos.size();i++){
			TablaVariables actual=ambitos.get(i);
			ArrayList<VarDec> lactual=actual.getTabla();
			for(int j=0;j<lactual.size();j++){
				state.add(lactual.get(j));
			}
		}
		return state;
	}
	public int getByteSize(){
		int count=0;
		for(int i=0;i<ambitos.size();i++){
			TablaVariables actual=ambitos.get(i);
			ArrayList<VarDec> lactual=actual.getTabla();
			for(int j=0;j<lactual.size();j++){
				count+=lactual.get(j).getByteSize();
			}
		}
		return count;
	}
	public void calculateMax(){
		int cant=getByteSize();
		if(cant>max){
			max=cant;
		}
	}
	public int getMax(){
		return max;
	}
	public int endGlobal(){
		return ambitos.get(1).getStartPos();
	}
	public int lastDir(){
		TablaVariables aFinal=ambitos.get(ambitos.size()-1);
		ArrayList<VarDec> lFinal=aFinal.getTabla();
		if(lFinal.size()==0){
			return aFinal.getStartPos();
		}
		return lFinal.get(lFinal.size()-1).getPosition();
	}
}
