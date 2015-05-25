package Tabla_Simbolos;

import java.util.ArrayList;

public class Firma implements Cloneable{
	private String nombre;
	private ArrayList<VarDec> parametros; 
	private Tipo tipo;
	String label;
	//*******************************************************************
	//constructores
	public Firma(String nombre, Tipo tipo, ArrayList<VarDec> parametros){
		this.nombre=nombre;
		this.parametros=parametros;
		this.tipo = tipo;
	}
	public Firma(String nombre, Tipo tipo){
		this.nombre=nombre;
		this.tipo = tipo;
		parametros = new ArrayList<VarDec>();
	}
	//********************************************************************
	//addParam agrega nuevo parametro a la firma
	public void addParam(VarDec newParam){
		parametros.add(newParam);
	}
	//equals compara las firmas
	public boolean equals(Firma compareTo){
		if(nombre.equals(compareTo.getNombre())){
			ArrayList<VarDec> temp=compareTo.getParametros();
			if(parametros.size()==temp.size()){
				for(int i=0;i<parametros.size();i++){
					VarDec temp1=parametros.get(i);
					VarDec temp2=temp.get(i);
					if(!temp1.equals(temp2)){
						return false;
					}
				}
				return true;
			}
			else{
				return false;
			}
		}
		else{
			return false;
		}
	}
	//**************************************************************
	//Getters y Setters
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public ArrayList<VarDec> getParametros() {
		return parametros;
	}
	public void setParametros(ArrayList<VarDec> parametros) {
		this.parametros = parametros;
	}
	public Tipo getTipo() {
		return tipo;
	}
	public void setTipo(Tipo tipo) {
		this.tipo = tipo;
	}
	//*******************************************************************
	//toStirng
	public String toString(){
		String res="";
		res="\nMethod Name: "+nombre+"\nReturn Type: "+tipo.getNombre()+"\nParametros";
		for(int i=0;i<parametros.size();i++){
			res+="\n\t"+parametros.get(i).getNombre();
		}
		return res;
	}
	public Firma clone(){
		ArrayList<VarDec> nuevo=new ArrayList<VarDec>();
		for(int i=0;i<parametros.size();i++){
			nuevo.add(parametros.get(i));
		}
		return new Firma(nombre,tipo.clone(),nuevo);
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
}
