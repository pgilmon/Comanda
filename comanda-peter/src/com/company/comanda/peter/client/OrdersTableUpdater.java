package com.company.comanda.peter.client;

import com.company.comanda.peter.shared.Constants;
import com.company.comanda.peter.shared.OrderState;
import com.company.comanda.peter.shared.PagedResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.RangeChangeEvent;

public class OrdersTableUpdater {
	
    public interface UpdateListener{
        void onUpdate();
    }
	private final GUIServiceAsync greetingService = GWT
            .create(GUIService.class);
	private CellTable<String[]> ordersTable;
	private AsyncDataProvider<String[]> ordersProvider;
	private OrderState selectedState;
	private Long selectedTableId;

	private MyTimer autoUpdateTimer;
	
	private UpdateListener updateListener;
    
	
	class MyTimer extends Timer{

        public void run(){
            refreshTable();
        }
    }
	
	public OrdersTableUpdater(CellTable<String[]> ordersTable){
		this.ordersTable = ordersTable;
	}
	
	public void setAutoUpdate(boolean value){
        if(ordersProvider == null){
            ordersProvider = new AsyncDataProvider<String[]>() {
                @Override
                protected void onRangeChanged(HasData<String[]> display) {
                    final int start = display.getVisibleRange().getStart();
                    int length = display.getVisibleRange().getLength();
                    AsyncCallback<PagedResult<String[]>> callback = new AsyncCallback<PagedResult<String[]>>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            Window.alert(caught.getMessage());
                        }
                        @Override
                        public void onSuccess(PagedResult<String[]> result) {
                            updateRowData(start, result.getList());
                            updateRowCount(result.getTotal(), true);
                        }
                    };
                    // The remote service that should be implemented
                    greetingService.getOrders(start, length, 
                            selectedState, selectedTableId, 
                            callback);
                }
            };
            ordersProvider.addDataDisplay(ordersTable);
        }
        if(autoUpdateTimer == null){
        	autoUpdateTimer = new MyTimer();
        }
        else{
        	autoUpdateTimer.cancel();
        }
        if(value){
            autoUpdateTimer.run();
            autoUpdateTimer.scheduleRepeating(Constants.AUTOUPDATE_PERIOD);
        }
    }
	
	public synchronized void refreshTable(){
        Range range = ordersTable.getVisibleRange();
        RangeChangeEvent.fire(ordersTable, range);
        if(updateListener != null){
            updateListener.onUpdate();
        }
    }
	
	public void setSelectedTableId(Long tableId){
		this.selectedTableId = tableId;
	}
	
	public void setSelectedState(OrderState state){
		this.selectedState = state;
	}
	
	public synchronized void setUpdateListener(UpdateListener listener){
	    this.updateListener = listener;
	}
}
