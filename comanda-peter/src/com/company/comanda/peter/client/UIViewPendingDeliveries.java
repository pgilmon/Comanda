package com.company.comanda.peter.client;

import com.company.comanda.peter.shared.BillState;
import com.company.comanda.peter.shared.BillType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class UIViewPendingDeliveries extends Composite {

    public static final int PAGE_SIZE = 25;
    
    private static ViewPendingDeliveriesUiBinder uiBinder = GWT
            .create(ViewPendingDeliveriesUiBinder.class);

    @UiField CellTable<String[]> odersTable;
    @UiField SimplePager odersPager;
    @UiField Label lblMessage;
    @UiField VerticalPanel ordersTableContainer;
    
    private BillsTableUpdater billsTableUpdater;
    
    interface ViewPendingDeliveriesUiBinder extends
            UiBinder<Widget, UIViewPendingDeliveries> {
    }

    public UIViewPendingDeliveries() {
        initWidget(uiBinder.createAndBindUi(this));
        
        TextColumn<String[]> tableNameColumn = new TextColumn<String[]>() {
            @Override
            public String getValue(String[] object) {
                return object[1];
            }
        };
        
        
        odersTable.addColumn(tableNameColumn, "Dirección");

        TextColumn<String[]> orderNameColumn = new TextColumn<String[]>() {
            @Override
            public String getValue(String[] object) {
                return object[2];
            }
        };
        odersTable.addColumn(orderNameColumn, "Fecha");
        
        billsTableUpdater = new BillsTableUpdater(odersTable);
        billsTableUpdater.setState(BillState.OPEN);
        billsTableUpdater.setType(BillType.DELIVERY);
        
        odersPager.setDisplay(odersTable);
        odersPager.setPageSize(PAGE_SIZE);
        
        
    }
    
    
    public void setAutoUpdate(boolean value){
        billsTableUpdater.setAutoUpdate(value);
    }
}