package Tabla_Simbolos;

import java.util.ArrayList;

public class TablaVariables {
	private ArrayList<VarDec> tabla;
	private int startPos=0;
	//**************************************************************
	//constructores
	public TablaVariables(ArrayList<VarDec> tabla){
		this.tabla=tabla;
	}
	public TablaVariables(){
		tabla=new ArrayList<VarDec>();
	}
	//**************************************************************
	//search
	public VarDec search(String nombre){
		for( int i = 0;i<tabla.size();i++){
			VarDec temp=tabla.get(i);
			if(temp.getNombre().equals(nombre)){
				return temp;
			}
		}
		return null;
	}
	//entry
	public void entry(VarDec varDec)throws Exception{
		if(search(varDec.getNombre())==null){
			tabla.add(varDec);
			
		}
		else{
			throw new Exception("Duplicate variable name: "+varDec.getNombre());
		}
	}
	//empty
	public void empty(){
		tabla=new ArrayList<VarDec>();
	}
	//***************************************************
	//Getters y Setters
	public ArrayList<VarDec> getTabla() {
		return tabla;
	}

	public void setTabla(ArrayList<VarDec> tabla) {
		this.tabla = tabla;
	}
	public int getStartPos() {
		return startPos;
	}
	public void setStartPos(int startPos) {
		this.startPos = startPos;
	}
	
	//***************************************************
	
}
