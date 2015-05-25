package Tabla_Simbolos;

import java.util.ArrayList;

public class TablaMetodos {
	private ArrayList<Firma> tabla;
	
	//**********************************************************
	//constructores
	public TablaMetodos(ArrayList<Firma> tabla){
		this.tabla=tabla;
	}
	public TablaMetodos(){
		tabla=new ArrayList<Firma>();
	}
	//**********************************************************
	//entry
	public void entry(Firma element) throws Exception{
		if(search(element)==null){
			tabla.add(element);
			element.setLabel(newLabel(element));
		}
		else{
			throw new Exception("Method "+element+" already exists");
		}
	}
	//**********************************************************
	//search
	public ArrayList<Firma> search(String nombre){
		ArrayList<Firma> res= new ArrayList<Firma>();
		for(int i=0;i<tabla.size();i++){
			Firma temp=tabla.get(i);
			if(temp.getNombre().equals(nombre)){
				res.add(temp);
			}
		}
		return res;
	}
	public Firma search(Firma lookingFor){
		for(int i=0;i<tabla.size();i++){
			if(lookingFor.equals(tabla.get(i))){
				return tabla.get(i); 
			}
		}
		return null;
	}
	//**********************************************************
	//**********************************************************
	//Getters y Setters
	public ArrayList<Firma> getTabla() {
		return tabla;
	}
	
	public void setTabla(ArrayList<Firma> tabla) {
		this.tabla = tabla;
	}
	//**********************************************************
	public String toString(){
		String res="";
		for(int i=0;i<tabla.size();i++){
			res+=tabla.get(i);
		}
		return res;
	}
	public String newLabel(Firma metodo){
		int count=0;
		String res=metodo.getNombre()+""+count;
		boolean found=true;
		while(found){
			res=metodo.getNombre()+""+count;
			found=false;
			for(int i=0;i<tabla.size();i++){
				if(res.equals(tabla.get(i).getLabel())){
					found=true;
				}
			}
			count++;
		}
		return res;
	}
}
