package sim.app.exploration.agents;

import sim.util.MutableInt2D;

public class BigExplorerAgent extends ExplorerAgent {

	public BigExplorerAgent(MutableInt2D loc) {
		/*
		 * Step -> The bigger explorer is slower
		 * View Range -> The bigger explorer has a wider field of vision
		 * Identify time -> The bigger explorer has a better scanner that takes less time to classify objects.
		 * */
		super(loc, Math.sqrt(2), 40, 15);
		// TODO Auto-generated constructor stub
	}

	

}
