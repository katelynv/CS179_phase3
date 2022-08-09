import java.util.*;
import cs132.vapor.ast.*;

public class Find {

	private List<String> registers;
	VFunction func;
	Graph g;
	List<Interval> liveSet;

	private int outVariables;

	public Find(VFunction f) {
		this.func = f;
		this.registers = new ArrayList<>();
		outVariables = 0;
	}

	public Map<String, Interval> LivenessAnalysis() {
		Graph g = createCFG(func); 
		this.g = g;
		Map<String, Interval> li = findLiveTime(g);
		for (String s : func.vars) {
			if (li.containsKey(s)) {
				if (li.get(s).useSize() == 0) {
					li.remove(s);
				}
			}
		}

		List<Interval> live = getLive(g);
		this.liveSet = live;
		findSavedRegs(func, g);
		return li;
	}

	public Graph getGraph() {
		return this.g;
	}

	public List<Interval> getLiveSet() {
		return this.liveSet;
	}

	private List<Interval> getLive(Graph g) {
		List<Integer> key = g.getAllKey();
		for (Integer i : key) {
			Node n = g.getNode(i);
			n.actives.addAll(n.in);
			n.actives.addAll(n.def);
		}
		List<Interval> prevIntervals = new ArrayList<Interval>();
		List<String> prev = new ArrayList<String>();
		List<Interval> curr = new ArrayList<Interval>();
		for (Integer i : key) {
			Node n = g.getNode(i);
			if (prev.size() == 0) {
				for (String s : n.actives) {
					prev.add(s);
					prevIntervals.add(new Interval(s, i, i));
				}
			} else {
				for (int j = 0; j < prev.size(); j++) {
					if (!n.actives.contains(prev.get(j))) {
						prev.remove(j);
						curr.add(prevIntervals.remove(j));
					} else {
						prevIntervals.get(j).setStop(i);
					}

				}
				for (String s : n.actives) {
					if (!prev.contains(s)) {
						prev.add(s);
						Interval live = new Interval(s, i, i);
						prevIntervals.add(live);
					}
				}
			}
		}
		curr.addAll(prevIntervals);

		return curr;
	}

	private static Map<String, Interval> findLiveTime(Graph g) {
		Map<String, Interval> li = new HashMap<String, Interval>();
		List<Integer> key = g.getAllKey();
		for (Integer i : key) {
			Node n = g.getNode(i);
			n.actives.addAll(n.in);
			n.actives.addAll(n.def);
		}
		for (Integer i : key) {
			for (String s : g.getNode(i).actives) {
				if (li.containsKey(s)) {
					li.get(s).setStop(i);
				} else {
					li.put(s, new Interval(s, i, i));
				}
			}
		}
		for (Integer i : key) {
			Node n = g.getNode(i);
			for (String use : n.use) {
				li.get(use).addUse(n.getValue());
			}

		}

		return li;
	}

	private static Graph createCFG(VFunction f) {
		Graph g = new Graph();
		for (VInstr y : f.body) {
			g.newNode(y.sourcePos.line);
		}
		Node curr;
		Node prev = null;
		for (int index = 0; index < f.body.length; index++) {
			VInstr y = f.body[index];
			curr = g.getNode(y.sourcePos.line);
			setVariableInfo(y, curr);
			if (y instanceof VGoto) {
				g.addEdge(prev, curr);
				VAddr<VCodeLabel> lab = ((VGoto) y).target;
				VAddr.Label<VCodeLabel> l = ((VAddr.Label<VCodeLabel>) lab);
				VCodeLabel codeLab = l.label.getTarget();
				Node jumpTo = g.getNode(codeLab.sourcePos.line);
				int jumpVal = 1;
				while (jumpTo == null) {
					jumpTo = g.getNode(codeLab.sourcePos.line + jumpVal);
					jumpVal++;
				}

				g.addEdge(curr, jumpTo);
			} else if (y instanceof VBranch) {
				if (prev != null) {
					g.addEdge(prev, curr);
				}

				VCodeLabel codeLab = ((VBranch) y).target.getTarget();
				Node jumpTo = g.getNode(codeLab.sourcePos.line + 1);
				g.addEdge(curr, jumpTo);
				prev = curr;
			} else {
				boolean checkgoto = true;
				if (index != 0) {
					checkgoto = !(f.body[index - 1] instanceof VGoto);
				}
				if (checkgoto) {
					if (prev != null) {
						g.addEdge(prev, curr);
					}
				}
				prev = curr;
			}
		}
		findInOut(f, g);

		return g;
	}

	private static void findInOut(VFunction f, Graph g) {
		for (VInstr y : f.body) {
			Node n = g.getNode(y.sourcePos.line);
			n.in.clear();
			n.out.clear();
			n.inoutCheck = false;
		}
		do {
			for (VInstr y : f.body) {
				g.getNode(y.sourcePos.line).inoutCheck = false;
			}
			for (VInstr y : f.body) {
				Node n = g.getNode(y.sourcePos.line);
				HashSet<String> temp_in = new HashSet<String>(n.in);
				HashSet<String> temp_out = new HashSet<String>(n.out);
				n.in.addAll(n.out);
				n.in.removeAll(n.def);
				n.in.addAll(n.use);
				Object[] suc = n.succ().toArray();
				for (int i = 0; i < suc.length; i++) {
					int sucVal = ((Node) suc[i]).getValue();
					Node sucNode = g.getNode(sucVal);
					n.out.addAll(sucNode.in);

				}
				if (temp_in.equals(n.in) && temp_out.equals(n.out)) {
					n.inoutCheck = true;
				}
			}
		} while (!checkDone(g));
	}

	public static boolean checkDone(Graph g) {
		Object[] Nodes = g.nodes().toArray();
		boolean done = true;
		for (int i = 0; i < Nodes.length; i++) {
			if (((Node) Nodes[i]).inoutCheck == false) {
				done = false;
			}
		}
		return done;
	}

	public void findSavedRegs(VFunction f, Graph g) {
		List<String> arguments = new ArrayList<>();
		for (VInstr i : f.body) {
			if (i instanceof VCall) {
				arguments.clear();
				arguments.add(((VCall) i).dest.ident);
				Node n = g.getNode(((VCall) i).sourcePos.line);
				for (String s : n.out) {
					if (!arguments.contains(s)) {
						if (!registers.contains(s)) {
							registers.add(s);
						}
					}
				}
				if (((VCall) i).args.length > 4) {
				}
				this.outVariables = ((VCall) i).args.length - 4;
			}
		}

	}

	public List<String> getSavedRegs() {
		return this.registers;
	}

	public int getOut() {
		return this.outVariables;
	}

	private static void setVariableInfo(VInstr y, Node curr) {
		if (y instanceof VAssign) {
			curr.def.add(((VAssign) y).dest.toString());
			if (((VAssign) y).source instanceof VVarRef) {
				curr.use.add(((VAssign) y).source.toString());
			}
		} else if (y instanceof VBranch) {
			curr.use.add(((VBranch) y).value.toString());
		} else if (y instanceof VBuiltIn) {
			VBuiltIn b = ((VBuiltIn) y);
			if (b.dest != null) {
				curr.def.add(((VBuiltIn) y).dest.toString());
			}
			for (VOperand vO : b.args) {
				if (vO instanceof VVarRef) {
					curr.use.add(vO.toString());
				}
			}
		} else if (y instanceof VCall) {
			VCall b = ((VCall) y);
			if (b.dest != null) {
				curr.def.add(((VCall) y).dest.toString());
			}
			if (b.addr instanceof VAddr.Var) {
				curr.use.add(b.addr.toString());
			}
			for (VOperand vO : b.args) {
				if (vO instanceof VVarRef) {
					curr.use.add(vO.toString());
				}
			}
		} else if (y instanceof VMemRead) {
			VMemRead read = (VMemRead) y;
			curr.def.add(read.dest.toString());
			if (read.source instanceof VMemRef.Global) {
				VMemRef.Global vmem = (VMemRef.Global) read.source;
				curr.use.add(vmem.base.toString());
			}
		} else if (y instanceof VMemWrite) {
			VMemWrite b = ((VMemWrite) y);
			if (b.source instanceof VVarRef) {
				curr.use.add(b.source.toString());
			}
			if (b.dest instanceof VMemRef.Global) {
				VMemRef.Global vmem = (VMemRef.Global) b.dest;
				curr.use.add(vmem.base.toString());
			}
		} else if (y instanceof VReturn) {
			VReturn ret = ((VReturn) y);
			if (ret.value != null) {
				if (ret.value instanceof VVarRef) {
					curr.use.add(ret.value.toString());
				}
			}
		}
	}
}