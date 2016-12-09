package sim.app.IA2.exploration.agents;

import sim.util.MutableInt2D;

public class SmallExplorerAgent extends ExplorerAgent {

	public SmallExplorerAgent(MutableInt2D loc) {
		/*
		 * Step -> The smaller explorer is faster
		 * View Range -> The smaller explorer has a narrower field of vision
		 * Identify time -> The smaller explorer has a worse scanner that takes more time to classify objects.
		 * */
		super(loc, 2*Math.sqrt(2), 25, 20);
		// TODO Auto-generated constructor stub
	}


}
