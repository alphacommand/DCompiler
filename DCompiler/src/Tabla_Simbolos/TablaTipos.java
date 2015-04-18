package Tabla_Simbolos;

import java.util.ArrayList;

public class TablaTipos {
	private ArrayList<Tipo> tabla;
	//**********************************************************
	//constructores
	public TablaTipos(ArrayList<Tipo> tabla){
		this.tabla=tabla;
	}
	public TablaTipos(){
		tabla=new ArrayList<Tipo>();
		//inicializacion de tipos
		tabla.add(new Tipo("void",null));
		tabla.add(new Tipo("int",null));
		tabla.add(new Tipo("char",null));
		tabla.add(new Tipo("boolean",null));
		tabla.add(new Tipo("error",null));
	}
	//**********************************************************
	//search
	
	public Tipo search(String nombre){
		for(int i=0;i<tabla.size();i++){
			Tipo temp=tabla.get(i);
			if(temp.getNombre().equals(nombre)){
				return temp;
			}
		}
		return null;
	}
	public Tipo primitiveSearch(String nombre){
		for(int i=1;i<4;i++){
			Tipo temp=tabla.get(i);
			if(temp.getNombre().equals(nombre)){
				return temp;
			}
		}
		return null;
	}
	//entry
	public void entry(Tipo tipo)throws Exception{
		if(search(tipo.getNombre())==null){
			tabla.add(tipo);
		}
		else{
			throw new Exception("Duplicate struct name: "+tipo.getNombre());
		}
	}
	//empty
	public void empty(){
		tabla=new ArrayList<Tipo>();
	}
	//*****************************************************
	//Getters y Setters
	public ArrayList<Tipo> getTipos() {
		return tabla;
	}

	public void setTipos(ArrayList<Tipo> tabla) {
		this.tabla = tabla;
	}
	public Tipo voidType(){
		return tabla.get(0);
	}
	public Tipo intType(){
		return tabla.get(1);
	}
	public Tipo charType(){
		return tabla.get(2);
	}
	public Tipo boolType(){
		return tabla.get(3);
	}
	public Tipo errorType(){
		return tabla.get(4);
	}
	//*****************************************************
	
	public String toString(){
		String res="";
		for(int i=0;i<tabla.size();i++){
			res+=tabla.get(i);
		}
		return res;
	}
}
