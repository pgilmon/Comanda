package com.company.comanda.peter.client;

import java.util.List;

import com.company.comanda.peter.shared.OrderState;
import com.company.comanda.peter.shared.PagedResult;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface GreetingServiceAsync {
	void greetServer(String input, AsyncCallback<Void> callback)
			throws IllegalArgumentException;
	void getMenuItems(int start, int length, AsyncCallback<PagedResult<String[]>> callback);
	void getOrders(int start, int length, OrderState state, String tableName,
	        AsyncCallback<PagedResult<String[]>> callback);
	void getUploadUrl(AsyncCallback<String> callback);
	void acceptOrder(String orderKey, AsyncCallback<Void> callback);
	void deleteMenuItems(long[] keyIds, AsyncCallback<Void> callback);
}
