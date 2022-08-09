import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.Appendable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


import cs132.util.IndentPrinter;
import cs132.util.ProblemException;
import cs132.vapor.ast.VBuiltIn.Op;
import cs132.vapor.ast.VAddr;
import cs132.vapor.ast.VCall;
import cs132.vapor.ast.VCodeLabel;
import cs132.vapor.ast.VDataSegment;
import cs132.vapor.ast.VFunction;
import cs132.vapor.ast.VInstr;
import cs132.vapor.ast.VOperand;
import cs132.vapor.ast.VReturn;
import cs132.vapor.ast.VVarRef;
import cs132.vapor.ast.VaporProgram;
import cs132.vapor.parser.VaporParser;

public class V2VM {

	public static void main(String[] args) throws IOException {
		VaporProgram vp = parseVapor(System.in, System.err);
		VDataSegment[] vData = vp.dataSegments;
		VFunction[] vfunc = vp.functions;
		convertDataSegments(vData);
		convertFunctions(vfunc);

	}

	public static void convertDataSegments(VDataSegment[] vData) throws IOException {
		for (VDataSegment a : vData) {
			System.out.println("const " + a.ident);
			for (VOperand m : a.values) {
				System.out.println(" " + m);
			}
			System.out.println();
		}
	}

	public static void convertFunctions(VFunction[] vf) throws IOException {
		for (VFunction x : vf) {
			Find fl = new Find(x); 
			Map<String, Interval> li = fl.LivenessAnalysis();

			List<String> args = fl.getSavedRegs();
			Scan ls = new Scan();
			List<String> variables = new ArrayList<>();
			for (String vari : x.vars) {
				variables.add(vari.toString());
			}
			ls.performLinearScan(li, args, variables); 

			int local = args.size();
			int in = 0;
			if (x.params.length > 4) {
				in = x.params.length - 4;
			}
			int out = fl.getOut();
			if (out < 0) {
				out = 0;
			}

			String s = "func " + x.ident + " [in " + in + ", out " + out + ", local " + local + "]\n";
			int sizeTotal = 0;
			sizeTotal = local;
			if (sizeTotal > 8) {
				sizeTotal = 8;
			}
			for (int loc = 0; loc < sizeTotal; loc++) {
				s += "  local[" + loc + "] = " + "$s" + loc + "\n";
			}
			Map<String, Register> registers = ls.getRegisterMap();
			List<String> params = new ArrayList<>();
			for (VVarRef v : x.params) {
				if (!args.contains(v.toString())) {
					params.add(v.toString());
				}
			}
			for (int p = 0; p < x.params.length; p++) {
				if (p < 4) {
					if (registers.containsKey(x.params[p].ident)) {
						s += "  " + registers.get(x.params[p].ident).toString() + " = " + "$a" + p + "\n";
					}

				} else {
					int y = p - 4;
					s += "  " + registers.get(params.get(p)).toString() + " = " + "in[" + y + "]\n";
				}
			}

			System.out.print(s);
			HashMap<Integer, String> labels = new HashMap<Integer, String>();
			for (VCodeLabel lab : x.labels) {
				if (labels.containsKey(lab.instrIndex)) {
					String labelString = labels.get(lab.instrIndex) + ":\n";
					labelString += lab.ident;
					labels.put(lab.instrIndex, labelString);
				} else {
					labels.put(lab.instrIndex, lab.ident);
				}
			}
			Visitor tp = new Visitor(registers, labels, local);
			VInstr[] main = x.body;
			int index = 1;
			if (labels.containsKey(0)) {
				tp.printLabel(0);
			}
			for (VInstr i : main) {
				i.accept(tp);
				tp.printLabel(index);
				index++;
			}
			System.out.println();
		}

	}

	public static VaporProgram parseVapor(InputStream in, PrintStream err) throws IOException {

		Op[] ops = { Op.Add, Op.Sub, Op.MulS, Op.Eq, Op.Lt, Op.LtS, Op.PrintIntS, Op.HeapAllocZ, Op.Error, };
		boolean allowLocals = true;
		String[] registers = null;
		boolean allowStack = false;

		VaporProgram tree;
		try {
			tree = VaporParser.run(new InputStreamReader(in), 1, 1, java.util.Arrays.asList(ops), allowLocals, registers,
					allowStack);
		} catch (ProblemException ex) {
			err.println(ex.getMessage());
			return null;
		}

		return tree;
	}

}