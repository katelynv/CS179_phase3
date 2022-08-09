import java.io.IOException;
import java.util.List;
import java.util.Map;

import cs132.vapor.ast.VAddr;
import cs132.vapor.ast.VAssign;
import cs132.vapor.ast.VBranch;
import cs132.vapor.ast.VBuiltIn;
import cs132.vapor.ast.VCall;
import cs132.vapor.ast.VGoto;
import cs132.vapor.ast.VInstr;
import cs132.vapor.ast.VLitInt;
import cs132.vapor.ast.VMemRead;
import cs132.vapor.ast.VMemRef;
import cs132.vapor.ast.VMemWrite;
import cs132.vapor.ast.VOperand;
import cs132.vapor.ast.VReturn;
import cs132.vapor.ast.VVarRef;

public class Visitor extends VInstr.Visitor<IOException> {
	Map<String, Register> register;
	private int indent;
	Map<Integer, String> labels;
	int locals;
	boolean vUse = false;

	public Visitor(Map<String, Register> reg, Map<Integer, String> labels2, int local) {
		this.register = reg;
		this.indent = 1;
		this.labels = labels2;
		locals = local;
	}

	public void printLabel(int x) {
		if (labels.containsKey(x)) {
			System.out.println(labels.get(x) + ":");
		}
	}

	public void visit(VAssign a) throws IOException {
		String dest = a.dest.toString();
		if (register.containsKey(dest)) {
			String source = a.source.toString();
			dest = register.get(dest).toString();
			if (a.source instanceof VVarRef) {
				if (register.containsKey(source)) {
					source = register.get(source).toString();
					String s = dest + " = " + source;
					System.out.println(printIndent() + s);
				}
			} else {
				if (register.containsKey(source)) {
					source = register.get(source).toString();
				}
				String s = dest + " = " + source;
				System.out.println(printIndent() + s);
			}
		}
	}

	public void visit(VBranch b) throws IOException {
		String s;
		if (b.positive) {
			s = "if ";
		} else {
			s = "if0 ";
		}
		s += register.get(b.value.toString()).toString() + " goto ";
		if (b.target.toString() != null) {
			s += b.target.toString();
		}
		System.out.println(printIndent() + s);
	}

	public void visit(VBuiltIn c) throws IOException {
		String s = "";
		String arguments = "";
		for (VOperand o : c.args) {
			if (o instanceof VVarRef) {
				arguments += register.get(o.toString()).toString() + " ";
			} else {
				arguments += o.toString() + " ";
			}
		}
		arguments = arguments.trim();
		if (c.dest != null) {
			s = register.get(c.dest.toString()).toString() + " = " + c.op.name + "(" + arguments + ")";
		} else {
			s = c.op.name + "(" + arguments + ")";
		}
		System.out.println(printIndent() + s);
	}

	private String printIndent() {
		int spaces = indent * 2;
		return String.format("%" + spaces + "s", "");
	}

	public void visit(VCall c) throws IOException {
		String s = "VCall";
		int i = 0;
		for (VOperand a : c.args) {
			if (i < 4) {
				if (a instanceof VVarRef) {
					System.out.println(printIndent() + "$a" + i + " = " + register.get(a.toString()).toString());
				} else {
					System.out.println(printIndent() + "$a" + i + " = " + a.toString());
				}
			} else {
				if (a instanceof VVarRef) {
					int index = i - 4;
					System.out.println(printIndent() + "out[" + index + "] = " + register.get(a.toString()).toString());
				} else {
					int index = i - 4;
					System.out.println(printIndent() + "out[" + index + "] = " + a.toString());
				}
			}
			i++;
		}
		String output;
		if (vUse == true) {
			int x = locals - 1;
			System.out.println(printIndent() + "$v0 = local[" + x + "]");
		}
		Register regis = register.get(c.addr.toString());
		if (regis != null) {
			output = regis.toString();
		} else {
			output = c.addr.toString();
		}
		System.out.println(printIndent() + "call " + output);
		if (c.dest != null) {
			if (register.containsKey(c.dest.toString())) {
				s = register.get(c.dest.toString()).toString();
				System.out.println(printIndent() + s + " = $v0");
			}
		}
	}

	public void visit(VGoto a) throws IOException {
		String s = a.target.toString();
		System.out.println(printIndent() + "goto " + s);
	}

	public void visit(VMemRead b) throws IOException {
		VMemRef.Global vmem = (VMemRef.Global) b.source;
		String offset = "";
		if (vmem.byteOffset > 0) {
			offset = "+" + vmem.byteOffset;
		}
		String r1 = register.get(b.dest.toString()).toString();
		String r2 = register.get(vmem.base.toString()).toString();
		String more = "";
		if (r1.contains("$v0") || r1.contains("$v1")) {
			int loc = locals - 1;
			more += "\n  local[" + loc + "] = " + r1 + "\n  ";
			more += r1 + " = local[" + loc + "]";
			vUse = true;
		}
		String s = r1 + " = [" + r2 + offset + "]" + more;
		System.out.println(printIndent() + s);
	}

	public void visit(VMemWrite c) throws IOException {
		String s = "";
		if (c.dest instanceof VMemRef.Global) {
			VMemRef.Global vmem = (VMemRef.Global) c.dest;
			String offset = "";
			if (vmem.byteOffset > 0) {
				offset = "+" + vmem.byteOffset;
			}
			String dest = vmem.base.toString();
			String source = c.source.toString();
			if (register.containsKey(dest)) {
				dest = register.get(dest).toString();
			}
			if (register.containsKey(source)) {
				source = register.get(source).toString();
			}
			s = "[" + dest + offset + "] = " + source;
		} else {
			String dest = c.dest.toString();
			String source = c.source.toString();
			if (register.containsKey(dest)) {
				dest = register.get(dest).toString();
			}
			if (register.containsKey(source)) {
				source = register.get(source).toString();
			}
			s = dest + " = " + source;
		}
		System.out.println(printIndent() + s);
	}

	public void visit(VReturn c) throws IOException {
		String s = "ret";
		if (c.value != null) {
			if (c.value instanceof VVarRef) {
				s = c.value.toString();
				System.out.println(printIndent() + "$v0 = " + register.get(s).toString());
			} else {
				s = c.value.toString();
				System.out.println(printIndent() + "$v0 = " + s);
			}
		}
		if (locals > 8) {
			locals = 8;
		}
		for (int loc = 0; loc < locals; loc++) {
			String p = "  $s" + loc + " = local[" + loc + "]";
			System.out.println(p);
		}
		System.out.println(printIndent() + "ret");
	}
}