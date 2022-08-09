import java.util.ArrayList;
import java.util.List;

public class Graph {
	private NodeList cfg;

	public Graph(){
		this.cfg = new NodeList();
 	}
	public NodeList nodes(){
		return this.cfg;
	}
	public Node newNode(int x){
		Node n = new Node(x);
		cfg.addNode(n);
		return n;
	}

	public Node getNode(int x){
		return cfg.find(x);
	}

	public void addEdge(Node from, Node to){
		from.succ().addNode(to);
		to.pred().addNode(from);
	}

	public void show(){
		cfg.print();
	}

	public int size() {
		return cfg.size();
	}

	public List<Integer> getAllKey(){
		List<Integer> key
            = new ArrayList<Integer>(); 
		Object[] c = cfg.toArray();
		for(int i = 0; i < c.length; i++) {
			Node n = (Node) c[i];
			key.add(n.getValue());
		}
		return key;
	}
}


