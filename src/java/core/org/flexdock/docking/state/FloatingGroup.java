/*
 * Created on May 3, 2005
 */
package org.flexdock.docking.state;

import java.awt.Rectangle;
import java.io.Serializable;
import java.util.HashSet;

import org.flexdock.docking.floating.frames.DockingFrame;

/**
 * @author Christopher Butler
 */
public class FloatingGroup implements Cloneable, Serializable {
	private String name;
	private Rectangle windowBounds;
	private DockingFrame frame;
	private HashSet dockables; // contains String dockableIds
	
	public FloatingGroup(String groupName) {
		name = groupName==null? String.valueOf(this.hashCode()): groupName;
		dockables = new HashSet();
	}
	
	private FloatingGroup(String groupName, HashSet dockableSet) {
		name = groupName;
		dockables = dockableSet;		
	}
	
	public Rectangle getBounds() {
		return windowBounds==null? null: (Rectangle)windowBounds.clone();
	}
	
	public void setBounds(Rectangle rect) {
		if(rect==null) {
			windowBounds = null;
		}
		else {
			if(windowBounds==null)
				windowBounds = (Rectangle)rect.clone();
			else
				windowBounds.setBounds(rect);
		}
	}
	
	public String getName() {
		return name;
	}

	public DockingFrame getFrame() {
		return frame;
	}
	
	public void setFrame(DockingFrame frame) {
		this.frame = frame;
	}
	
	public void addDockable(String dockableId) {
		dockables.add(dockableId);
	}
	
	public void removeDockable(String dockableId) {
		dockables.remove(dockableId);
	}
	
	public int getDockableCount() {
		return dockables.size();
	}
	
	public void destroy() {
		dockables.clear();
		setFrame(null);
		setBounds(null);
	}
	
	public Object clone() {
		HashSet set = (HashSet)dockables.clone();
		FloatingGroup clone = new FloatingGroup(name, set);
		clone.frame = frame;
		clone.windowBounds = (Rectangle)windowBounds.clone();
		return clone;
	}
}