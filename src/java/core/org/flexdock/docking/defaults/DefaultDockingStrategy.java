/*
 * Created on Mar 14, 2005
 */
package org.flexdock.docking.defaults;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;

import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.DockingPort;
import org.flexdock.docking.DockingStrategy;
import org.flexdock.docking.drag.DragToken;
import org.flexdock.docking.event.DockingEvent;
import org.flexdock.docking.event.EventDispatcher;
import org.flexdock.util.DockingUtility;
import org.flexdock.util.RootWindow;
import org.flexdock.util.SwingUtility;

/**
 * @author Christopher Butler
 */
public class DefaultDockingStrategy implements DockingStrategy {

	public void dock(Dockable dockable, DockingPort port, String region) {
		dock(dockable, port, region, null);
	}

	public void dock(Dockable dockable, DockingPort port, String region, DragToken token) {
		if(dockable==null || dockable.getDockable()==null || port==null)
			return;
		
		if(!DockingManager.isValidDockingRegion(region))
			throw new IllegalArgumentException("'" + region + "' is not a valid docking region.");
		
		// cache the old parent
		DockingPort oldPort = DockingUtility.getParentDockingPort(dockable);

		// perform the drop operation.
		DockingResults results = dropComponent(dockable, port, region, token);

		// perform post-drag operations
		DockingPort newPort = results.dropTarget;
		int evtType = results.success? DockingEvent.DOCKING_COMPLETE: DockingEvent.DOCKING_CANCELED;
		DockingEvent evt = new DockingEvent(dockable, oldPort, newPort, evtType);

		// notify the old docking port
		EventDispatcher.notifyDockingMonitor(oldPort, evt);
		// notify the new docking port
		EventDispatcher.notifyDockingMonitor(newPort, evt);
		// notify the dockable
		EventDispatcher.notifyDockingMonitor(dockable, evt);
	}
	

	protected DockingResults dropComponent(Dockable dockable, DockingPort target, String region, DragToken token) {
		DockingResults results = new DockingResults(target, false);
		
		if (DockingPort.UNKNOWN_REGION.equals(region) || target==null) {
			return results;
		}
			
		Component docked = target.getDockedComponent();
		Component dockableCmp = dockable.getDockable();
		if (dockableCmp!=null && dockableCmp == docked) {
			// don't allow docking the same compnent back into the same port
			return results;
		}

		// obtain a reference to the content pane that holds the target DockingPort.
		// MUST happen before undock(), in case the undock() operation removes the 
		// target DockingPort from the container tree.
		Container contentPane = SwingUtility.getContentPane((Component)target);
		Point contentPaneLocation = token==null? null: token.getCurrentMouse(contentPane);
		
		// undock the current Dockable instance from it's current parent container
		undock(dockable);

		// when the original parent reevaluates its container tree after undocking, it checks to see how 
		// many immediate child components it has.  split layouts and tabbed interfaces may be managed by 
		// intermediate wrapper components.  When undock() is called, the docking port 
		// may decide that some of its intermedite wrapper components are no longer needed, and it may get 
		// rid of them. this isn't a hard rule, but it's possible for any given DockingPort implementation.  
		// In this case, the target we had resolved earlier may have been removed from the component tree 
		// and may no longer be valid.  to be safe, we'll resolve the target docking port again and see if 
		// it has changed.  if so, we'll adopt the resolved port as our new target.
		if(contentPaneLocation!=null && contentPane!=null) {
			results.dropTarget = DockingUtility.findDockingPort(contentPane, contentPaneLocation);
			target = results.dropTarget;
		}

		results.success = target.dock(dockableCmp, dockable.getDockableDesc(), region);
		SwingUtility.revalidateComponent((Component) target);
		return results;
	}
	
	/*

	
	private boolean dropIntoFloatingWindow(Dockable dockable, DragToken token_) {

	}
	
	*/
	
	public void undock(Dockable dockable) {
		if(dockable==null)
			return;
		
		Component dragSrc = dockable.getDockable();
		Container parent = dragSrc.getParent();
		RootWindow rootWin = RootWindow.getRootContainer(parent);
		
		// if there's no parent container, then we really don't have anything from which to to 
		// undock this component, now do we?
		if(parent==null)
			return;
		
		DockingPort dockingPort = DockingUtility.getParentDockingPort(dragSrc);
		if(dockingPort!=null)
			// if 'dragSrc' is currently docked, then undock it instead of using a 
			// simple remove().  this will allow the DockingPort to do any of its own 
			// cleanup operations associated with component removal.
			dockingPort.undock(dragSrc);
		else
			// otherwise, just remove the component
			parent.remove(dragSrc);
		
		if(rootWin!=null)
			SwingUtility.revalidateComponent(rootWin.getContentPane());
	}
	
	protected class DockingResults {
		public DockingResults(DockingPort port, boolean status) {
			dropTarget = port;
			success = status;
		}
		public DockingPort dropTarget;
		public boolean success;
	}
}