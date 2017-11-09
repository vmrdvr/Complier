package cop5556sp17;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import cop5556sp17.AST.Dec;
import cop5556sp17.SymbolTable.SymbolTableEntryPart;


public class SymbolTable {
	int current_scope=-1,next_scope=-1;
	Stack<Integer>scope_stack = new Stack<Integer>();
	HashMap<String, ArrayList<SymbolTableEntryPart>> map = new HashMap<String, ArrayList<SymbolTableEntryPart>> ();
	
	public class SymbolTableEntryPart{
		int scope;
		Dec dec;
	}
	//TODO  add fields

	/** 
	 * to be called when block entered
	 */
	public void enterScope(){
		next_scope++;
		current_scope= next_scope;
		scope_stack.push(current_scope);		
	}
	
	
	/**
	 * leaves scope
	 */
	public void leaveScope(){
		scope_stack.pop();
		current_scope= scope_stack.peek();
	}
	
	public boolean insert(String ident, Dec dec){
		//TODO:  IMPLEMENT THIS
		SymbolTableEntryPart step= new SymbolTableEntryPart();
		step.scope=current_scope;
		step.dec=dec;
		if(!map.containsKey(ident)) {
			ArrayList<SymbolTableEntryPart> arrlist = new ArrayList<>();
			arrlist.add(step);
			map.put(ident, arrlist);
		}
		else{
			ArrayList<SymbolTableEntryPart> temparr= map.get(ident);
			for(SymbolTableEntryPart st : temparr){
				if(st.scope==current_scope){
					return false;
				}			
			}
			map.get(ident).add(0,step);
			map.put(ident, map.get(ident));
		}
		return true;
	}
	
	public Dec lookup(String ident){
		//TODO:  IMPLEMENT THIS
		if(map.containsKey(ident)){
			ArrayList<SymbolTableEntryPart> tempar= map.get(ident);
			for(SymbolTableEntryPart t: tempar){
				if(t.scope<=current_scope){ //please check where condition is LT or LE
					return t.dec;
				}
			}
		}
		return null;
	}
		
	public SymbolTable() {
		//TODO:  IMPLEMENT THIS
	 enterScope();
		
	}


	@Override
	public String toString() {
		//TODO:  IMPLEMENT THIS
		StringBuilder sb = new StringBuilder();
		for(String str : map.keySet()){
			sb.append(str);
			for(SymbolTableEntryPart st : map.get(str)){
				sb.append(st.scope);
				sb.append(st.dec);
			}
			sb.append(" ");
		}
		return sb.toString();
	}
	
	


}
