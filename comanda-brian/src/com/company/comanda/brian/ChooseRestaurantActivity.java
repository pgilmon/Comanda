package com.company.comanda.brian;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.company.comanda.brian.helpers.AsyncGetData;
import com.company.comanda.brian.model.Restaurant;
import com.company.comanda.brian.xmlhandlers.RestaurantListHandler;
import com.company.comanda.common.HttpParams.SearchRestaurants;

public class ChooseRestaurantActivity extends ListActivity {

    private double latitude;
    private double longitude;
    
    private ArrayList<Restaurant> restaurants;
    
    public static final String EXTRA_LATITUDE = "latitude";
    public static final String EXTRA_LONGITUDE = "longitude";
    public static final String EXTRA_NICE_ADDRESS = "niceAddress";
    public static final String EXTRA_ADDRESS_DETAILS = "addressDetails";
    
    private ItemAdapter adapter;
    
    
    
    private static class GetRestaurants extends AsyncGetData<ArrayList<Restaurant>>{
        
        @Override
        public void afterOnUIThread(ArrayList<Restaurant> data,
                Activity activity) {
            super.afterOnUIThread(data, activity);
            final ChooseRestaurantActivity local = (ChooseRestaurantActivity)activity;
            Log.d("Comanda", "afterOnUIThread");
            if(local.restaurants != null && local.restaurants.size() > 0)
            {
                
                local.adapter.notifyDataSetChanged();
                local.adapter.clear();
                for(int i=0;i<local.restaurants.size();i++){
                    Log.d("Comanda", "Item #" + i);
                    local.adapter.add(local.restaurants.get(i));
                }
            }
            local.adapter.notifyDataSetChanged();
        }
        
        @Override
        public void afterOnBackground(ArrayList<Restaurant> data,
                Activity activity) {
            super.afterOnBackground(data, activity);
            ((ChooseRestaurantActivity)activity).restaurants = data;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        this.latitude = extras.getDouble(EXTRA_LATITUDE);
        this.longitude = extras.getDouble(EXTRA_LONGITUDE);
        
        setContentView(R.layout.choose_restaurant);
        
        restaurants = new ArrayList<Restaurant>();
        adapter = new ItemAdapter(this, R.layout.restaurant_row, restaurants);
        
        setListAdapter(adapter);
        
        GetRestaurants getRestaurants = new GetRestaurants();
        
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>(2);
        params.add(new BasicNameValuePair(SearchRestaurants.PARAM_LATITUDE, "" + latitude));
        params.add(new BasicNameValuePair(SearchRestaurants.PARAM_LONGITUDE, "" + longitude));
        getRestaurants.execute(this, SearchRestaurants.SERVICE_NAME, params, 
                RestaurantListHandler.class);
        getListView().setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                    long id) {
                Restaurant restaurant = (Restaurant)arg0.getItemAtPosition(position);
                Intent intent = new Intent(
                        getApplicationContext(), 
                        ComandaActivity.class);
                intent.putExtra(ComandaActivity.EXTRA_REST_ID, 
                        restaurant.id);
                intent.putExtra(ComandaActivity.EXTRA_REST_NAME, 
                        restaurant.name);
                intent.putExtra(ComandaActivity.EXTRA_TABLE_ID, 
                        "");
                intent.putExtra(ComandaActivity.EXTRA_TABLE_NAME, 
                        "");
                startActivity(intent);
            }
        });
    }

    /*
     * PRIVATE ADAPTER CLASS. Assigns data to be displayed on the listview
     */
    private class ItemAdapter extends ArrayAdapter<Restaurant> 
    {
        //Hold array of items to be displayed in the list
        private ArrayList<Restaurant> items;

        public ItemAdapter(Context context, int textViewResourceId,
                ArrayList<Restaurant> items) 
        {
            super(context, textViewResourceId, items);
            this.items = items;// TODO Auto-generated catch block
        }
        //This method returns the actual view
        //that is displayed as a row (we will inflate with row.xml)
        @Override
        public View getView(int position, View convertView, ViewGroup parent) 
        {
            View v = convertView;
            if (v == null) 
            {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                //inflate using res/layout/row.xml
                v = vi.inflate(R.layout.restaurant_row, null);
            }
            //get the FoodMenuItem corresponding to 
            //the position in the list we are rendering
            Restaurant o = items.get(position);
            if (o != null) 
            {
                //Set all of the UI components 
                //with the respective Object data
                TextView tt = (TextView) v.findViewById(R.id.toptext);
                final String restaurantName = o.name;
                if (tt != null)
                {
                    tt.setText(restaurantName);   
                }
                
            }
            
            

            return v;
        }
    }
}