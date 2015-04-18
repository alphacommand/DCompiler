package Tabla_Simbolos;

public class VarDec implements Cloneable{
	private String nombre;
	private Tipo tipo;
	private int longitud;
	//*********************************************************
	//constructores
	public VarDec(String nombre, Tipo tipo, int longitud){
		this.nombre=nombre;
		this.tipo=tipo;
		this.longitud=longitud;
	}
	//*********************************************************
	//equals
	public boolean equals(VarDec compareTo){
		if(tipo.equals(compareTo.getTipo())){
			if(isList()==compareTo.isList()){
				return true;
			}
		}
		return false;
	}
	//******************************************
	//Getters y Setters
	public boolean isList(){
		if(longitud==-1||longitud>0){
			return true;
		}
		return false;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public Tipo getTipo() {
		return tipo;
	}
	public void setTipo(Tipo tipo) {
		this.tipo = tipo;
	}
	//longitud 0 indica que no es una lista
	//longitud -1 indica que es una lista, ademas de todos los numeros mayores a 0
	public int getLongitud() {
		return longitud;
	}
	public void setLongitud(int longitud) {
		this.longitud = longitud;
	}
	//****************************************
	public String toString(){
		if(tipo==null){
			return"soy null me llamo "+nombre;
		}
		return "var_name: "+nombre+", tipo: "+tipo.getNombre()+";";
	}
	public VarDec clone(){
		return new VarDec(nombre,(Tipo)tipo.clone(),longitud);
	}
}
