
public class TargetCodeGenerator {
	private String targetCode;
	private int startReg;
	private int endReg;
	private int auxReg;
	private int max;
	public TargetCodeGenerator(String code,int startReg,int endReg,int auxReg,int max){
		this.max=max;
		this.startReg=startReg;
		this.endReg=endReg;
		this.auxReg=auxReg;
		targetCode="";
		String[] inputs=code.split("\n");
		//codigo inicial
		targetCode+=".globl main\nmain:\nstmfd sp!, {lr}\nLDR R11, =_dataGlobal\npush {lr}\nBL main_0\nB _salir";
		for(int i=0;i<inputs.length;i++){
			String actual=inputs[i].trim();
			if((!actual.equals(""))&&(!actual.contains("--"))){
				String[] instruccion=actual.split("_");
				/*
				String spec="";
				for(int j=0;j<instruccion.length;j++){
					spec+=instruccion[j]+" ";
				}
				System.out.println(spec);
				*/
				targetCode+=generateCode(instruccion);
			}
			
		}
		//codigo final
		targetCode+="pop {pc}\n_salir:\nmov r0, #0\nmov r3, #0\nldmfd sp!, {lr}\nBX lr\n.section .data\n.align 2\n_IOOB:\n\t.asciz \"El indice no esta dentro del rango del arreglo \"\n_formatoInt:\n\t.asciz \"%d\\n\"\n_dataGlobal:\n\t.space "+max;
	}
	//*************************************************************************************************************
	//Getters and Setters
	public String getTargetCode() {
		return targetCode;
	}
	public void setTargetCode(String targetCode) {
		this.targetCode = targetCode;
	}
	//*************************************************************************************************************
	//Metodos para creacion de codigo objeto
	public String generateCode(String[] instruccion){
		String operador=instruccion[0];
		String res="";
		if(operador.equals("st")){
			res="STR "+getValue(instruccion[2])+",["+getGR()+","+getValue(instruccion[1])+"]";
		}
		else if(operador.equals("ld")){
			res="LDR "+getValue(instruccion[2])+",["+getGR()+","+getValue(instruccion[1])+"]";
		}
		else if(operador.equals("mv")){
			res="MOV "+getValue(instruccion[1])+","+getValue(instruccion[2]);
		}
		else if(operador.equals("add")){
			res="ADD "+getValue(instruccion[1])+","+getValue(instruccion[2])+","+getValue(instruccion[3]);
		}
		else if(operador.equals("sub")){
			res="SUB "+getValue(instruccion[1])+","+getValue(instruccion[2])+","+getValue(instruccion[3]);
		}
		else if(operador.equals("mul")){
			res="MUL "+getValue(instruccion[1])+","+getValue(instruccion[2])+","+getValue(instruccion[3]);
		}
		else if(operador.equals("div")){
			res="pendiente";
		}
		else if(operador.equals("mod")){
			res="pendiente";
		}
		else if(operador.equals("lt")){
			res="CMP "+getValue(instruccion[2])+","+getValue(instruccion[3])+"\n";
			res+="MOVLT "+getValue(instruccion[1])+",#"+1+"\n";
			res+="MOVGE "+getValue(instruccion[1])+",#"+0;
		}
		else if(operador.equals("gt")){
			res="CMP "+getValue(instruccion[2])+","+getValue(instruccion[3])+"\n";
			res+="MOVGT "+getValue(instruccion[1])+",#"+1+"\n";
			res+="MOVLE "+getValue(instruccion[1])+",#"+0;
		}
		else if(operador.equals("lte")){
			res="CMP "+getValue(instruccion[2])+","+getValue(instruccion[3])+"\n";
			res+="MOVLE "+getValue(instruccion[1])+",#"+1+"\n";
			res+="MOVGT "+getValue(instruccion[1])+",#"+0;
		}
		else if(operador.equals("gte")){
			res="CMP "+getValue(instruccion[2])+","+getValue(instruccion[3])+"\n";
			res+="MOVGE "+getValue(instruccion[1])+",#"+1+"\n";
			res+="MOVLT "+getValue(instruccion[1])+",#"+0;
		}
		else if(operador.equals("or")){
			//no usados
		}
		else if(operador.equals("and")){
			//no usados
		}
		else if(operador.equals("eq")){
			res="CMP "+getValue(instruccion[2])+","+getValue(instruccion[3])+"\n";
			res+="MOVEQ "+getValue(instruccion[1])+",#"+1+"\n";
			res+="MOVNE "+getValue(instruccion[1])+",#"+0;
		}
		else if(operador.equals("neq")){
			res="CMP "+getValue(instruccion[2])+","+getValue(instruccion[3])+"\n";
			res+="MOVNE "+getValue(instruccion[1])+",#"+1+"\n";
			res+="MOVEQ "+getValue(instruccion[1])+",#"+0;
		}
		else if(operador.equals("not")){
			res="CMP "+getValue(instruccion[1])+",#"+1+"\n";
			res+="MOVEQ "+getValue(instruccion[2])+",#"+0+"\n";
			res+="MOVNE "+getValue(instruccion[2])+",#"+1;
		}
		else if(operador.equals("neg")){
			res="MUL "+getValue(instruccion[1])+","+getValue(instruccion[2])+",#"+(-1);
		}
		else if(operador.equals("GoToF")){
			res="CMP "+getValue(instruccion[2])+",#"+0+"\n";
			res="BEQ "+instruccion[1];
		}
		else if(operador.equals("GoToT")){
			res="CMP "+getValue(instruccion[2])+",#"+1+"\n";
			res="BEQ "+instruccion[1];
		}
		else if(operador.equals("GoTo")){
			res="B "+instruccion[1];
		}
		else if(operador.equals("returnG")){
			res="MOV pc,"+getValue(instruccion[1]);
		}
		else if(operador.equals("GoToM")){
			res="BL "+instruccion[1];
		}
		else if(operador.equals("push")){
			res="push {"+getValue(instruccion[1])+"}";
		}
		else if(operador.equals("pop")){
			res="pop {"+getValue(instruccion[1])+"}";
		}
		else if(operador.equals("returndir")){
			res="push {lr}";
		}
		else{
			res="";
			for(int i=0;i<instruccion.length;i++){
				res+=instruccion[i];
			}
		}
		return res+"\n";
	}
	public String getValue(String val){
		if('t'==val.charAt(0)){
			return "R"+(startReg+Integer.parseInt(val.substring(1)));
		}
		else{
			return "#"+val;
		}
	}
	public String getGR(){
		return "R"+auxReg;
	}
}
