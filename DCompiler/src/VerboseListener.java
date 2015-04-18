import java.util.Collections;
import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

//Definitive ANTLR Reference page 154
public class VerboseListener extends BaseErrorListener{
	
	String errors="Done";
	@Override
	public void syntaxError(Recognizer<?, ?> recognizer,Object offendingSymbol,int line, int charPositionInLine, String msg, RecognitionException e){
		if(errors.equals("Done")){
			errors="";
		}
		List<String> stack = ((Parser)recognizer).getRuleInvocationStack();
		Collections.reverse(stack);
		errors+="\nrule stack: "+stack+"\n\tline "+line+":"+charPositionInLine+" at "+offendingSymbol+": "+msg;
		System.err.println(errors);
	}
	public String getErrors(){
		return errors;
	}
}
