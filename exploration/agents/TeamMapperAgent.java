package sim.app.IA2.exploration.agents;

import java.awt.Color;
import java.lang.reflect.Constructor;

import sim.app.IA2.exploration.objects.Prototype;
import sim.app.IA2.exploration.objects.SimObject;
import sim.util.Bag;
import sim.util.Int2D;
import sim.util.MutableInt2D;

public class TeamMapperAgent extends MapperAgent {
	
	private RefereeAgent referee;

	public TeamMapperAgent(int width, int height, RefereeAgent referee) {
		super(width, height);
		this.referee = referee;
	}
	
	/**
	 * Adds a series of objects to the known world, checking if they are already 
	 * mapped or not
	 * @param visible
	 */
	@Override
	public void addVisibleObjects(Bag visible) {
		
		for(Object o : visible){
			// If the object is not known to the world
			if(knownWorld.getObjectLocation(o) == null){
				
				SimObject s = (SimObject) o;
				knownWorld.setObjectLocation(s, s.getLoc().x, s.getLoc().y);
				
				/* REFEREE */
				if (referee.knownWorld.getObjectLocation(o) == null) {
					referee.knownWorld.setObjectLocation(s, s.getLoc().x, s.getLoc().y);
				}
			}
		}
	}

	@Override
	public void updateLocation(ExplorerAgent agent, MutableInt2D loc) {
		knownWorld.setObjectLocation(agent, new Int2D(loc));
		
		/* REFEREE */
		referee.knownWorld.setObjectLocation(agent, new Int2D(loc));
	}
	
	
	/* The TeamMapper knows what has been discovered by both teams */
	@Override
	public boolean isIdentified(Int2D loc) {
		return referee.isIdentified(loc);
	}
	
	public boolean isIdentifiedByTeam(Int2D loc) {
		return identifiedObjects[loc.getX()][loc.getY()] != null;
	}

	@Override
	public void identify(SimObject obj, Class highest) {
		
		//System.out.println("IDENTIFYING OBJ AT (" + obj.loc.x + "," + obj.loc.y + ") AS " + highest.getName());
		
		Int2D loc = obj.loc;
		
		identifiedObjects[loc.getX()][loc.getY()] = highest;
		
		/* REFEREE */
		referee.identifiedObjects[loc.getX()][loc.getY()] = highest;
	
		Class[] params = {Int2D.class, Color.class, double.class};
		Object[] args = {obj.loc, obj.color, obj.size};
		
		if(highest.isInstance(obj)){
			this.addObject(obj);
			
		}else{
			try{
				Constructor c = highest.getConstructor(params);
				SimObject newObj = (SimObject) c.newInstance(args);
				this.addObject(newObj);
				
			} catch (Exception e){
				System.err.println("No such constructor, please give up on life.");
			}
		}
	}

	@Override
	public void addObject(SimObject obj) {
		Int2D loc = obj.loc;
		
		Bag temp = knownWorld.getObjectsAtLocation(loc.x, loc.y);
		
		if(temp != null){
			Bag here = new Bag(temp);
			
			for(Object o : here){
				if(! (o instanceof ExplorerAgent) ){
					knownWorld.remove(o);
					
					/* REFEREE */
					referee.knownWorld.remove(o);
				}
			}
		}
		
		knownWorld.setObjectLocation(obj, loc);
		
		/* REFEREE */
		referee.knownWorld.setObjectLocation(obj, loc);
		
	}

	@Override
	public void addPrototype(SimObject obj, Class class1) {
		for(Prototype p : this.knownObjects) {
			if(class1 == p.thisClass){
				p.addOccurrence(obj.size, obj.color);
				return;
			}
		}
		
		this.knownObjects.add(new Prototype(class1, obj.size, obj.color));
		
		/* REFEREE */
		referee.knownObjects.add(new Prototype(class1, obj.size, obj.color));
	}

}
