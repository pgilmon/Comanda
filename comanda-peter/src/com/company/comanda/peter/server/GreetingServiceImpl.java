package com.company.comanda.peter.server;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.company.comanda.peter.client.GreetingService;
import com.company.comanda.peter.server.model.MenuItem;
import com.company.comanda.peter.shared.FieldVerifier;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class GreetingServiceImpl extends RemoteServiceServlet implements
		GreetingService {

	
	public void greetServer(String input) throws IllegalArgumentException {
		// Verify that the input is valid. 
				if (!FieldVerifier.isValidName(input)) {
					// If the input is not valid, throw an IllegalArgumentException back to
					// the client.
					throw new IllegalArgumentException(
							"Name must be at least 4 characters long");
				}

				// Escape data from the client to avoid cross-site script vulnerabilities.
				input = escapeHtml(input);
				
				PersistenceManager pm = PMF.get().getPersistenceManager();   
			      MenuItem item = new MenuItem(input, "");
			   //persist
			      try{ pm.makePersistent(item); }
			      finally{ pm.close(); }
	}
	
	
	public List<String> getMenuItemNames(int start, int length){
		ArrayList<String> result = new ArrayList<String>();
		
	    PersistenceManager pm = null;
	    try{
	    	pm = PMF.get().getPersistenceManager();
		    Query query = pm.newQuery("select from " + MenuItem.class.getName());
		    @SuppressWarnings("unchecked")
			List<MenuItem> items = (List<MenuItem>) query.execute();
		    result.ensureCapacity(items.size());
		    int count = 0;
		    for(MenuItem item: items){
		    	result.add(item.getName());
		    	count++;
		    	if(count == length){
		    		break;
		    	}
		    }
	    }
	    finally{
	    	if(pm != null){
	    		pm.close();
	    	}
	    }
		return result;
	}
	
	/**
	 * Escape an html string. Escaping data received from the client helps to
	 * prevent cross-site script vulnerabilities.
	 * 
	 * @param html the html string to escape
	 * @return the escaped string
	 */
	private String escapeHtml(String html) {
		if (html == null) {
			return null;
		}
		return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;")
				.replaceAll(">", "&gt;");
	}
}
