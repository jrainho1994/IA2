package sim.app.IA2.exploration.env;

import java.io.FileWriter;
import java.lang.reflect.Constructor;
import java.util.HashMap;

import sim.app.IA2.exploration.agents.ExplorerAgent;
import sim.app.IA2.exploration.agents.MapperAgent;
import sim.app.IA2.exploration.objects.Bush;
import sim.app.IA2.exploration.objects.House;
import sim.app.IA2.exploration.objects.SimObject;
import sim.app.IA2.exploration.objects.Tree;
import sim.app.IA2.exploration.objects.Wall;
import sim.app.IA2.exploration.objects.Water;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.grid.SparseGrid2D;
import sim.util.Bag;
import sim.util.Int2D;
import sim.util.MutableInt2D;

public abstract class Environment implements Steppable {
	
	private static final long serialVersionUID = 1L;
	
	protected final int maxSteps = 5000;
	protected int step = 0;
	protected FileWriter writer;
	protected SparseGrid2D world;
	protected Class[][] occupied;
	protected HashMap<String, Integer> milestones  = new HashMap<>(); //60% - 75% - 95% - 99% - 100%

	protected abstract void setup (SimState state);
	public abstract void step(SimState state);
	public abstract MapperAgent getMapper();
	
	/* Map Generation Methods */
	
	protected void buildRandomMap(SimState state) {
		Class classes[] = {Wall.class, Tree.class, Bush.class, Water.class, House.class};
		int numberOfInstances[] = {400, 200, 200, 100, 20};
		Int2D loc;
		
		for (int i = 0; i < classes.length; i++) {
			
			for(int j = 0; j < numberOfInstances[i]; j++) {
				do { loc = new Int2D(state.random.nextInt(world.getWidth()),state.random.nextInt(world.getHeight())); }
				while (occupied[loc.x][loc.y] != null);
				
				addObject(classes[i], loc);
			}
			
		}
	}
	
	protected void buildDonutMap(SimState state) {
		Int2D loc;
		
		// Define the two classes
		Class outer_class = Tree.class;
		Class inner_class = Bush.class;
		
		// Number of instances
		int num_outer = 500;
		int num_inner = 500;
		
		// Define the size of the inner square
		int inner_width = world.getWidth() / 2;
		int inner_height = world.getHeight() / 2;
		
		int inner_x = (world.getWidth() / 2) - (inner_width / 2);
		int inner_y = (world.getHeight() / 2) - (inner_height / 2);
		
		// Add the outer instances
		for(int j = 0; j < num_outer; j++) {
			do { loc = new Int2D(state.random.nextInt(world.getWidth()),state.random.nextInt(world.getHeight())); }
			while ( occupied[loc.x][loc.y] != null ||
					( (loc.x >= inner_x && loc.x <= inner_x + inner_width) &&
					(loc.y >= inner_y && loc.y <= inner_y + inner_height)));
			
			addObject(outer_class, loc);
		}
		
		// Add the inner instances
		for(int j = 0; j < num_inner; j++) {
			do { loc = new Int2D(state.random.nextInt(inner_width) + inner_x, state.random.nextInt(inner_height) + inner_y); }
			while ( occupied[loc.x][loc.y] != null);
			
			addObject(inner_class, loc);
		}
	}
	
	protected void buildStructuredMap(SimState state) {
		Int2D loc;
		
		// Number of instances per block
		int num_instances = 500;
		
		int height_separation = world.getHeight()/3;
		int width_separation = world.getWidth()/3;
		int sep = 50;
		
		// First Block - Top Forest
		for(int j = 0; j < num_instances; j++) {
			do { loc = new Int2D(state.random.nextInt(world.getWidth()), state.random.nextInt(height_separation - sep/2)); }
			while ( occupied[loc.x][loc.y] != null);
			
			addObject(Tree.class, loc);
		}
		
		// Bush Block - Bushes below the Forest
		for(int j = 0; j < num_instances; j++) {
			do { loc = new Int2D(state.random.nextInt(world.getWidth()), state.random.nextInt(30) + (height_separation - 30/2)); }
			while ( occupied[loc.x][loc.y] != null);
			
			addObject(Bush.class, loc);
		}
		
		// Central Block - House neighborhood
		for(int j = 0; j < num_instances; j++) {
			do { loc = new Int2D(state.random.nextInt(world.getWidth()), state.random.nextInt(height_separation - sep) + (height_separation + sep/2)); }
			while ( occupied[loc.x][loc.y] != null);
			
			addObject(House.class, loc);
		}
		
		// Wall Block - Wall below the neighborhood
		for(int j = 0; j < num_instances; j++) {
			do { loc = new Int2D(state.random.nextInt(world.getWidth()), state.random.nextInt(30) + (2*height_separation - 30/2)); }
			while ( occupied[loc.x][loc.y] != null);
			
			addObject(Wall.class, loc);
		}
		
		// Down Left Block - Forest
		for(int j = 0; j < num_instances; j++) {
			do { loc = new Int2D(state.random.nextInt(width_separation - sep/2), state.random.nextInt(height_separation - sep/2) + (2*height_separation + sep/2)); }
			while ( occupied[loc.x][loc.y] != null);
			
			addObject(Tree.class, loc);
		}
		
		// Down Center Block - Water
		for(int j = 0; j < num_instances; j++) {
			do { loc = new Int2D(state.random.nextInt(width_separation) + (width_separation), state.random.nextInt(height_separation - sep/2) + (2*height_separation + sep/2)); }
			while ( occupied[loc.x][loc.y] != null);
			
			addObject(Water.class, loc);
		}
		
		// Down Right Block - Forest
		for(int j = 0; j < num_instances; j++) {
			do { loc = new Int2D(state.random.nextInt(width_separation - sep/2) + (2*width_separation + sep/2), state.random.nextInt(height_separation - sep/2) + (2*height_separation + sep/2)); }
			while ( occupied[loc.x][loc.y] != null);
			
			addObject(Tree.class, loc);
		}
	}
	
	/* End of Map Methods */
	
	private void addObject(Class c, Int2D loc) {
		Class[] params = {int.class,int.class};
		Object[] args = {loc.x,loc.y};
		SimObject obj;
		
		try {
			Constructor cons = c.getConstructor(params);	
			obj = (SimObject) cons.newInstance(args);
		}
		
		catch (Exception e) { System.err.println("Oops. See addObject."); return; };
		
		world.setObjectLocation(obj,loc);
		occupied[loc.x][loc.y] = c;
	}
	
	public Bag getVisibleObejcts(int x, int y, int viewRange) {
		Bag all = world.getVonNeumannNeighbors(x, y, viewRange, SparseGrid2D.BOUNDED, true, null, null, null);
		Bag visible = new Bag();
		
		for(Object b: all){
			if(b instanceof ExplorerAgent) continue;
			
			SimObject o = (SimObject) b;
			visible.add(new SimObject(o));
		}
		
		return visible;
	}

	public SimObject identifyObject(Int2D loc) {
		Bag here = world.getObjectsAtLocation(loc.getX(), loc.getY());
		int i = 0;
		
		if(here == null){
			return null;
		}
		
		SimObject real = null;
		
		for (Object obj : here) {
			if (!(obj instanceof ExplorerAgent)) {
				real = (SimObject) obj;
				break;
			}
		}
		
		return real;
	}

	public void updateLocation(ExplorerAgent agent, MutableInt2D loc) {
		world.setObjectLocation(agent, new Int2D(loc));
	}
	
	

}
