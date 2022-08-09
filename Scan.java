import java.util.*;
import java.util.Map.Entry;

import cs132.vapor.ast.VFunction;
import cs132.vapor.ast.VVarRef;

public class Scan {

	private Map<String, Interval> active;
	private Map<String, Register> registers;
	private List<Register> freeRegisters;
	private Set<String> stackLocation;

	public Scan() {
		active = new LinkedHashMap<String, Interval>();
		registers = new LinkedHashMap<String, Register>();
		freeRegisters = new ArrayList<Register>();
		stackLocation = new HashSet<>();
	}

	public Map<String, Register> getRegisterMap() {
		return this.registers;
	}

	public void performScan(Map<String, Interval> li, List<String> args, List<String> vars) {
		Map<String, Interval> variables = new HashMap<>();
		Map<String, Interval> parameters = new HashMap<>();
		Register r = new Register();
		freeRegisters.addAll(r.getCallerSaved());
		for (Map.Entry<String, Interval> i : li.entrySet()) {
			if (args.contains(i.getKey())) {
				parameters.put(i.getKey(), i.getValue());
			} else {
				if (vars.contains(i.getKey())) {
					variables.put(i.getKey(), i.getValue());
				}
			}
		}

		scanRegAlloc(variables);
		r.setCallerSaved(freeRegisters);
		freeRegisters.clear();
		freeRegisters.addAll(r.getCalleeSaved());
		active.clear();
		scanRegAlloc(parameters);
		r.setCalleeSaved(freeRegisters);
	}

	public void scanRegAlloc(Map<String, Interval> li) {
		Map<String, Interval> l = sortbyStart(li);
		for (Map.Entry<String, Interval> i : l.entrySet()) {
			ExpireOldIntervals(i);
			if (freeRegisters.size() == 0) {
				SpillAtInterval(i);
			} else {
				registers.put(i.getKey(), freeRegisters.get(0));
				freeRegisters.remove(0);
				active.put(i.getKey(), i.getValue());
			}
		}
	}

	public void ExpireOldIntervals(Map.Entry<String, Interval> i) {
		active = sortByEnd(active);
		List<String> removeArr = new ArrayList<>();
		boolean removeSomething = false;
		for (Map.Entry<String, Interval> j : active.entrySet()) {
			if (j.getValue().getStop() >= i.getValue().getStart()) {
				if (removeSomething == true) {
					for (String s : removeArr) {
						active.remove(s);
					}
				}
				return;
			}
			removeArr.add(j.getKey());
			removeSomething = true;
			Register r = registers.get(j.getKey());
			freeRegisters.add(r);
			sortFreeRegisters();
		}
		if (removeSomething == true) {
			for (String s : removeArr) {
				active.remove(s);
			}
		}
	}

	public void SpillAtInterval(Map.Entry<String, Interval> i) {
		Map.Entry<String, Interval> spill = getLast(active);
		if (spill != null) {
			if (spill.getValue().getStop() > i.getValue().getStop()) {
				String key = i.getKey();
				Register r = registers.get(spill.getKey());
				registers.put(key, r);
				stackLocation.add(spill.getKey());
				active.remove(spill.getKey());
				active.put(i.getKey(), i.getValue());
				active = sortByEnd(active);
			}
		} else {
			stackLocation.add(i.getKey());
		}
	}

	public Map.Entry<String, Interval> getLast(Map<String, Interval> map) {
		Iterator<Map.Entry<String, Interval>> iterator = map.entrySet().iterator();
		Map.Entry<String, Interval> result = null;
		while (iterator.hasNext()) {
			result = iterator.next();
		}
		return result;
	}

	public Map<String, Interval> sortbyStart(Map<String, Interval> li) {
		List<Entry<String, Interval>> sorted = new ArrayList<>(li.entrySet());
		Collections.sort(sorted, new Comparator<Entry<String, Interval>>() {
			@Override
			public int compare(Entry<String, Interval> o1, Entry<String, Interval> o2) {
				return o1.getValue().compareStart(o2.getValue());
			}
		});
		Map<String, Interval> sortedMap = new LinkedHashMap<String, Interval>();
		for (Entry<String, Interval> entry : sorted) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}

	public Map<String, Interval> sortByEnd(Map<String, Interval> li) {
		List<Entry<String, Interval>> sorted = new ArrayList<>(li.entrySet());
		Collections.sort(sorted, new Comparator<Entry<String, Interval>>() {
			@Override
			public int compare(Entry<String, Interval> o1, Entry<String, Interval> o2) {
				return o1.getValue().compareStop(o2.getValue());
			}
		});
		Map<String, Interval> sortedMap = new LinkedHashMap<String, Interval>();
		for (Entry<String, Interval> entry : sorted) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}

	private void sortFreeRegisters() {
		Collections.sort(freeRegisters, new Comparator<Register>() {
			@Override
			public int compare(Register o1, Register o2) {

				return o1.toString().compareTo(o2.toString());
			}
		});
	}
}