import java.util.HashSet;

public class Node {

	public NodeList succesors;
	public NodeList predecessors;
	private int value;
	public HashSet<String> in;
	public HashSet<String> out;
	public HashSet<String> def;
	public HashSet<String> use;
	public boolean inoutCheck;
	public HashSet<String> actives;

	public Node(int x){
		this.value = x;
		this.succesors = new NodeList();
		this.predecessors = new NodeList();
		this.in = new HashSet<String>();
		this.out = new HashSet<String>();
		this.def = new HashSet<String>();
		this.use = new HashSet<String>();
		this.actives = new HashSet<String>();
		inoutCheck = false;
	}

	public int getValue(){
		return this.value;
	}
	public NodeList succ(){
		return this.succesors;
	}
	public NodeList pred(){
		return this.predecessors;
	}

	public NodeList adj(){
		return null;
	}
	public int outDegree(){
		return succesors.size();
	}
	public int inDegree(){
		return predecessors.size();
	}

	public void print(){
		String output = "";
		output = output + this.getValue() + "\n";
		Object[] pre = this.pred().toArray();
		Object[] suc = this.succ().toArray();
		output += "Pred: ";
		for(int x = 0; x < pre.length; x++){
			output += ((Node)pre[x]).getValue() + ",";
		}
		output += "\n";
		output += "Succ: ";
		for(int x = 0; x < suc.length; x++){
			output += ((Node)suc[x]).getValue() + ",";
		}
		output += "\n";
		output += "Use: " + this.use.toString();
		output += "\n";
		output += "Def: " + this.def.toString();
		output += "\n";
		output += "In: " + this.in.toString();
		output += "\n";
		output += "Out: " + this.out.toString();
		output += "\n \n";
		System.out.println(output);
	}

	public int getKey() {
		return 0;
	}
 }