package Tabla_Simbolos;

import java.util.ArrayList;

public class Tipo implements Cloneable{
	private ArrayList<VarDec> atributos;
	private String nombre;
	private int size=4;
	private String temp="-1";
	//*************************************************************
	//constructores
	public Tipo(String nombre, ArrayList<VarDec> atributos){
		this.nombre=nombre;
		this.atributos=atributos;
	}
	public Tipo(String nombre){
		this.nombre=nombre;
		atributos= new ArrayList<VarDec>();
	}
	//*************************************************************
	//add Atrib agrega un atributo
	public boolean addAtrib(VarDec atributo){
		int position=0;
		for(int x=0;x<atributos.size();x++){
			if(atributos.get(x).getNombre().equals(atributo.getNombre())){
				return false;  
			}
			position+=atributos.get(x).getByteSize();
		}
		atributo.setPosition(position);
		atributos.add(atributo);
		return true;
	}
	//equals
	public boolean equals(Tipo compareTo){
		if(nombre.equals(compareTo.getNombre())){
			return true;
		}
		return false;
	}
	//*******************************************************
	//Getters y Setters
	public ArrayList<VarDec> getAtributos() {
		return atributos;
	}
	public void setAtributos(ArrayList<VarDec> atributos) {
		this.atributos = atributos;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public VarDec searchAtribute(String att){
		for(int i=0;i<atributos.size();i++){
			if(att.equals(atributos.get(i).getNombre())){
				return atributos.get(i);
			}
		}
		return null;
	}
	//******************************************************
	//toString
	public String toString(){
		String res="\nname: "+nombre;
		if(atributos!=null){
			for(int i=0;i<atributos.size();i++){
				res+="\n"+atributos.get(i);
			}
		}
		return res;
	}
	
	public Tipo clone(){
		if(atributos==null){
			return new Tipo(nombre,null);
		}
		ArrayList<VarDec> nuevo=new ArrayList<VarDec>();
		for(int i=0;i<atributos.size();i++){
			nuevo.add(atributos.get(i));
		}
		return new Tipo(nombre,nuevo);
	}
	
	public int getByteSize(){
		if(atributos==null){
			return size;
		}
		else{
			size=0;
			for(int i=0;i<atributos.size();i++){
				size+=atributos.get(i).getByteSize();
			}
			return size;
		}
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public String getTemp() {
		return temp;
	}
	public void setTemp(String temp) {
		this.temp = temp;
	}
	public boolean isStruct(){
		if(atributos==null){
			return false;
		}
		return true;
	}
}
