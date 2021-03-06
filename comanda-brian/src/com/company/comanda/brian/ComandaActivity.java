package com.company.comanda.brian;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.peterkuterna.android.apps.swipeytabs.SwipeyTabs;
import net.peterkuterna.android.apps.swipeytabs.SwipeyTabsAdapter;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.LabeledIntent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.company.comanda.brian.helpers.AsyncGetData;
import com.company.comanda.brian.helpers.Formatter;
import com.company.comanda.brian.helpers.LayoutHelper;
import com.company.comanda.brian.helpers.QualifierTranslator;
import com.company.comanda.brian.model.Category;
import com.company.comanda.brian.model.FoodMenuItem;
import com.company.comanda.brian.model.OrderElement;
import com.company.comanda.brian.xmlhandlers.BooleanHandler;
import com.company.comanda.brian.xmlhandlers.CategoriesHandler;
import com.company.comanda.brian.xmlhandlers.MenuItemsHandler;
import com.company.comanda.common.HttpParams;
import com.company.comanda.common.HttpParams.GetCategories;
import com.company.comanda.common.HttpParams.GetMenuItems;

public class ComandaActivity extends FragmentActivity
{

    private static final Logger log = 
            LoggerFactory.getLogger(ComandaActivity.class);
    private ArrayList<FoodMenuItem> m_items = null;
    private ArrayList<Category> categories = null;
    private ItemAdapter[] adapters;
    private String tableName;
    private String restName;
    private String tableId;
    private String restId;
    private String address;
    private String addressDetails;
    private float deliveryCost;
    private float minimumForDelivery;
    private String city;

    private HashMap<Long, Category> categoriesMap = new HashMap<Long, Category>();

    private TextView tvTotalAmount;
    private float totalAmount;

    private static final int BIG_IMAGE_SIZE = 100;
    private static final int SMALL_IMAGE_SIZE = 50;

    private HashMap<String, Bitmap> smallBitmaps = new HashMap<String, Bitmap>();
    private HashMap<String, Bitmap> largeBitmaps = new HashMap<String, Bitmap>();

    private SwipeyTabs categoriesTabs;
    private ViewPager categoriesPager;

    private ArrayList<OrderElement> orderItems;
    private HashMap<OrderElement, Integer> orderNumbers;

    private ArrayAdapter<OrderElement> reviewOrdersAdapter;

    private static final int REVIEW_ORDER_DIALOG = 1;
    private static final int ITEM_DETAILS_DIALOG = 2;
    private static final int ADD_CHOOSE_QUALIFIER_DIALOG = 3;
    private static final int CHOOSE_EXTRA_DIALOG = 5;
    private static final int UNDER_MINIMUM_FOR_DELIVERY = 6;

    private FoodMenuItem selectedMenuItem;
    private int selectedQualifier;


    private static class AsyncGetMenuItems extends AsyncGetData<ArrayList<FoodMenuItem>>{

        @Override
        public void afterOnUIThread(ArrayList<FoodMenuItem> data,
                Activity activity) {
            super.afterOnUIThread(data, activity);
            ComandaActivity local = (ComandaActivity)activity;
            log.debug("afterOnUIThread");
            if(local.m_items != null && local.m_items.size() > 0)
            {

                for(int i=0;i<local.adapters.length; i++){
                    ItemAdapter adapter = local.adapters[i];
                    adapter.notifyDataSetChanged();
                    adapter.clear();
                    ArrayList<FoodMenuItem> currentList =
                            local.filterMenuItems(local.categories.get(i).id);
                    for(FoodMenuItem currentItem : currentList){
                        log.debug("Item #{}", i);
                        adapter.add(currentItem);
                    }
                }
            }
            for(ItemAdapter adapter : local.adapters){
                adapter.notifyDataSetChanged();
            }
        }


        @Override
        public void afterOnBackground(ArrayList<FoodMenuItem> data,
                Activity activity) {
            super.afterOnBackground(data, activity);
            ((ComandaActivity)activity).m_items = data;
        }


        @Override
        public void beforeOnBackground(List<NameValuePair> params,
                Activity activity) {
            super.beforeOnBackground(params, activity);
            params.add(new BasicNameValuePair(GetMenuItems.PARAM_RESTAURANT_ID, 
                    ((ComandaActivity)activity).restId));
        }

    }

    private class AsyncGetCategories extends AsyncGetData<ArrayList<Category>>{



        @Override
        public void afterOnUIThread(ArrayList<Category> data,
                Activity activity) {
            ComandaActivity local = (ComandaActivity) activity;

            super.afterOnUIThread(data, activity);
            categories = data;
            TextView tableNameTextView = (TextView)findViewById(R.id.tableNametextView);
            tableNameTextView.setText(restName);

            m_items = new ArrayList<FoodMenuItem>();
            orderItems = new ArrayList<OrderElement>();
            orderNumbers = new HashMap<OrderElement, Integer>();
            //set ListView adapter to basic ItemAdapter 
            //(it's a coincidence they are both called Item)
            final int noOfCategories = categories.size();
            adapters = new ItemAdapter[noOfCategories];
            for(int i=0;i<noOfCategories;i++){
                Category currentCategory = categories.get(i);
                adapters[i] = new ItemAdapter(ComandaActivity.this, 
                        R.layout.row, filterMenuItems(currentCategory.id));
                local.categoriesMap.put(currentCategory.id, currentCategory);
            }

            AsyncGetMenuItems getData = new AsyncGetMenuItems();
            getData.execute(ComandaActivity.this, GetMenuItems.SERVICE_NAME, new ArrayList<NameValuePair>(1), MenuItemsHandler.class);

            categoriesPager = (ViewPager)findViewById(R.id.categoriesPager);
            categoriesTabs = (SwipeyTabs)findViewById(R.id.categoriesTabs);

            final SwipeyTabsPagerAdapter adapter = new SwipeyTabsPagerAdapter(
                    ComandaActivity.this, getSupportFragmentManager());
            categoriesPager.setAdapter(adapter);
            categoriesTabs.setAdapter(adapter);
            categoriesPager.setOnPageChangeListener(categoriesTabs);
            categoriesPager.setCurrentItem(0);

            Button reviewOrderButton = (Button)findViewById(R.id.buttonReviewOrder);
            reviewOrderButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    showDialog(REVIEW_ORDER_DIALOG);

                }
            });
            reviewOrdersAdapter = new ReviewOrderAdapter(ComandaActivity.this, 
                    R.layout.row, orderItems);
        }
    }


    public static final int ORDER_PLACED_TOAST_DURATION = 10;

    public static final String EXTRA_TABLE_NAME = "tableName";
    public static final String EXTRA_TABLE_ID = "tableId";
    public static final String EXTRA_REST_NAME = "restaurantName";
    public static final String EXTRA_REST_ID = "restaurantId";
    public static final String EXTRA_NICE_ADDRESS = "niceAddress";
    public static final String EXTRA_CITY = "city";
    public static final String EXTRA_ADDRESS_DETAILS = "addressDetails";
    public static final String EXTRA_DELIVERY_COST = "deliveryCost";
    public static final String EXTRA_MINIMUM_FOR_DELIVERY = "minimumForDelivery";


    SharedPreferences prefs;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Bundle extras = getIntent().getExtras();
        tableName = extras.getString(EXTRA_TABLE_NAME);
        tableId = extras.getString(EXTRA_TABLE_ID);
        restName = extras.getString(EXTRA_REST_NAME);
        restId = extras.getString(EXTRA_REST_ID);
        address = extras.getString(EXTRA_NICE_ADDRESS);
        city = extras.getString(EXTRA_CITY);
        addressDetails = extras.getString(EXTRA_ADDRESS_DETAILS);
        deliveryCost = extras.getFloat(EXTRA_DELIVERY_COST);
        minimumForDelivery = extras.getFloat(EXTRA_MINIMUM_FOR_DELIVERY);

        //FIXME: This wouldn't work on not Euro-locales

        AsyncGetCategories getCategories = new AsyncGetCategories();
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>(1);
        params.add(new BasicNameValuePair(GetCategories.PARAM_RESTAURANT_ID, restId));
        getCategories.execute(this, 
                GetCategories.SERVICE_NAME, params, CategoriesHandler.class);


    }


    public static class CategoriesTabFragment extends Fragment {

        private ListAdapter adapter;

        public CategoriesTabFragment(ListAdapter adapter){
            this.adapter = adapter;
        }

        public static Fragment newInstance(String title, 
                ListAdapter adapter) {
            CategoriesTabFragment f = new CategoriesTabFragment(adapter);
            Bundle args = new Bundle();
            args.putString("title", title);
            f.setArguments(args);
            return f;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            ViewGroup root = (ViewGroup) inflater.
                    inflate(R.layout.menu_items_list, null);
            ((ListView) root.findViewById(
                    R.id.menu_items_listview)).setAdapter(adapter);
            return root;
        }

    }


    private class SwipeyTabsPagerAdapter extends FragmentPagerAdapter implements
    SwipeyTabsAdapter {

        private final Context mContext;

        public SwipeyTabsPagerAdapter(Context context, FragmentManager fm) {
            super(fm);

            this.mContext = context;
        }

        @Override
        public Fragment getItem(int position) {
            return CategoriesTabFragment.newInstance(
                    categories.get(position).name, adapters[position]);
        }

        @Override
        public int getCount() {
            return categories.size();
        }

        public TextView getTab(final int position, SwipeyTabs root) {
            TextView view = (TextView) LayoutInflater.from(mContext).inflate(
                    R.layout.swipey_tab_indicator, root, false);
            view.setText(categories.get(position).name);
            view.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    categoriesPager.setCurrentItem(position);
                }
            });

            return view;
        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        prefs = PreferenceManager.
                getDefaultSharedPreferences(getApplicationContext());
    }



    public void fetchContent()
    {


    }
    //OPTIONS MENU STUFF
    //     @Override
    //     public boolean onCreateOptionsMenu(Menu menu) 
    //     {
    //         MenuInflater inflater = getMenuInflater();
    //         inflater.inflate(R.menu.menu, menu);
    //         return true;
    //     }
    //     @Override
    //     public boolean onOptionsItemSelected(MenuItem item) 
    //     {
    //         // Handle item selection
    //         switch (item.getItemId()) {
    //         case R.id.refresh:
    //             fetchContent();
    //             return true;
    //         default:
    //             return super.onOptionsItemSelected(item);
    //         }
    //     }    

    //FIXME: Should accept Void instead of String
    private class PlaceOrderTask extends AsyncGetData<Boolean> {




        @Override
        public void afterOnUIThread(Boolean data, Activity activity) {
            super.afterOnUIThread(data, activity);
            dismissDialog(REVIEW_ORDER_DIALOG);
            orderItems.clear();
            orderNumbers.clear();
            reviewOrdersAdapter.notifyDataSetChanged();
            refreshAllTables();
            if(data){
                Toast.makeText(getApplicationContext(), 
                        R.string.order_placed, ORDER_PLACED_TOAST_DURATION).show();
                setResult(Constants.ORDER_PLACED_RESULT);
                finish();
            }
            else{
                Constants.showErrorDialog(
                        R.string.error_while_placing_order, 
                        ComandaActivity.this);
            }
        }


        @Override
        public void beforeOnBackground(List<NameValuePair> params,
                Activity activity) {
            super.beforeOnBackground(params, activity);

            // Add your data
            StringBuffer keyIds = new StringBuffer();
            StringBuffer qualifierIds = new StringBuffer();
            StringBuffer noOfItems = new StringBuffer();
            StringBuffer extras = new StringBuffer();
            for(int i=0;i<orderItems.size();i++){
                final OrderElement currentOrderElement = orderItems.get(i);
                final FoodMenuItem currentItem = currentOrderElement.menuItem;
                final int currentQualifierIndex = currentOrderElement.qualifierIndex;
                final String currentKeyId = currentItem.getKeyId();
                final int currentNoOfItems = orderNumbers.get(currentOrderElement);
                final StringBuffer currentExtras = new StringBuffer();
                if(currentOrderElement.extras != null){
                    for(int currentExtra : currentOrderElement.extras){
                        currentExtras.append(currentExtra);
                        currentExtras.append(",");
                    }
                    if(currentExtras.length() > 0){
                        currentExtras.deleteCharAt(currentExtras.length() - 1);
                    }
                }
                keyIds.append(currentKeyId);
                keyIds.append("&");
                qualifierIds.append(currentQualifierIndex);
                qualifierIds.append("&");
                noOfItems.append(currentNoOfItems);
                noOfItems.append("&");
                extras.append(currentExtras);
                extras.append("&");
            }
            keyIds.deleteCharAt(keyIds.length() - 1);
            qualifierIds.deleteCharAt(qualifierIds.length() - 1);
            noOfItems.deleteCharAt(noOfItems.length() - 1);
            extras.deleteCharAt(extras.length() - 1);

            log.debug("keyIds: {}", keyIds);
            log.debug("qualifiers: {}", qualifierIds);
            log.debug("noOfItems: {}", noOfItems);
            log.debug("extras: {}", extras);
            params.add(new BasicNameValuePair(HttpParams.PlaceOrder.PARAM_ITEM_IDS, 
                    keyIds.toString()));
            params.add(new BasicNameValuePair(HttpParams.PlaceOrder.PARAM_QUALIFIERS, 
                    qualifierIds.toString()));
            params.add(new BasicNameValuePair(HttpParams.PlaceOrder.PARAM_EXTRAS, 
                    extras.toString()));
            params.add(new BasicNameValuePair(HttpParams.PlaceOrder.PARAM_TABLE_ID, tableId));
            params.add(new BasicNameValuePair(HttpParams.PlaceOrder.PARAM_RESTAURANT_ID, restId));
            params.add(new BasicNameValuePair(HttpParams.PlaceOrder.PARAM_USER_ID, 
                    prefs.getString(ComandaPreferences.USER_ID, "")));
            params.add(new BasicNameValuePair(HttpParams.PlaceOrder.PARAM_NO_OF_ITEMS, 
                    noOfItems.toString()));
            params.add(new BasicNameValuePair(HttpParams.PlaceOrder.PARAM_PASSWORD, 
                    prefs.getString(ComandaPreferences.PASSWORD, "")));
            params.add(new BasicNameValuePair(HttpParams.PlaceOrder.PARAM_ADDRESS, address + ", " + addressDetails + ", " + city));
        }

    }


    /*
     * PRIVATE ADAPTER CLASS. Assigns data to be displayed on the listview
     */
    private class ItemAdapter extends ArrayAdapter<FoodMenuItem> 
    {
        //Hold array of items to be displayed in the list
        private ArrayList<FoodMenuItem> items;


        public ItemAdapter(Context context, int textViewResourceId,
                ArrayList<FoodMenuItem> items) 
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
                v = vi.inflate(R.layout.row, null);
            }
            //get the FoodMenuItem corresponding to 
            //the position in the list we are rendering
            final FoodMenuItem o = items.get(position);
            if (o != null) 
            {
                fillWithMenuItemInfo(v, o);

            }

            return v;
        }        
    }

    private void fillWithMenuItemInfo(View v, final FoodMenuItem o){
        //Set all of the UI components 
        //with the respective Object data
        ImageView icon = (ImageView) v.findViewById(R.id.icon);
        TextView tt = (TextView) v.findViewById(R.id.item_name);
        final String menuItemName = o.getName();
        final String menuItemDescription = o.getDescription();
        final String imageString = o.getImageString();
        if (tt != null)
        {
            tt.setText(menuItemName);   
        }
        TextView[] tvPrice = new TextView[3];
        tvPrice[0] = (TextView) v.findViewById(R.id.item_price1);
        tvPrice[1] = (TextView) v.findViewById(R.id.item_price2);
        tvPrice[2] = (TextView) v.findViewById(R.id.item_price3);
        final int noOfPrices = o.getPrices().size();
        for(int i=0;i<tvPrice.length;i++){
            if(i<noOfPrices){
                tvPrice[i].setText(Formatter.money(o.getPrices().get(i)));
                tvPrice[i].setVisibility(View.VISIBLE);
            }
            else{
                tvPrice[i].setVisibility(View.GONE);
            }

        }
        Bitmap rawBitMap = null;
        Bitmap finImg = null;
        if(icon != null && imageString != null && imageString.length() > 0
                && ("null".equals(imageString) == false))
        {
            if(smallBitmaps.containsKey(imageString)){
                finImg = smallBitmaps.get(imageString);
            }
            else{
                URL imageURL = null;
                try      
                {        
                    //use our image serve page to get the image URL
                    imageURL = new URL(imageString + "=s"
                            + BIG_IMAGE_SIZE);
                } 
                catch (MalformedURLException e) 
                {
                    e.printStackTrace();
                }
                try 
                {
                    //Decode and resize the image then set as the icon
                    //                BitmapFactory.Options options = new BitmapFactory
                    //                        .Options();
                    //                options.inJustDecodeBounds = true;
                    //                options.inSampleSize = 1/2;

                    if(largeBitmaps.containsKey(imageString)){
                        rawBitMap = largeBitmaps.get(imageString);
                    }
                    else if (imageURL != null){
                        InputStream bitmapIS = (InputStream)imageURL
                                .getContent();
                        rawBitMap = BitmapFactory
                                .decodeStream(bitmapIS);
                        bitmapIS.close();
                        if(rawBitMap != null){
                            largeBitmaps.put(imageString, rawBitMap);
                        }
                    }
                    if(smallBitmaps.containsKey(imageString) == false){
                        if(rawBitMap != null){
                            finImg = Bitmap
                                    .createScaledBitmap(rawBitMap, SMALL_IMAGE_SIZE, 
                                            SMALL_IMAGE_SIZE, false);
                            smallBitmaps.put(imageString, finImg);
                        }
                    }
                    else{
                        finImg = smallBitmaps.get(imageString);
                    }
                } 
                catch (IOException e) 
                {                        
                    e.printStackTrace();
                }
            }
        }
        //returns the view to the Adapter to be displayed
        if(finImg != null){
            icon.setVisibility(View.VISIBLE);
            icon.setImageBitmap(finImg);
        }
        else{
            icon.setVisibility(View.GONE);
        }
        View.OnClickListener clickListener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                selectedMenuItem = o;
                showDialog(ITEM_DETAILS_DIALOG);
            }
        };
        LinearLayout itemNameAndPrice = 
                (LinearLayout)v.findViewById(
                        R.id.item_name_and_price);
        itemNameAndPrice.setOnClickListener(clickListener);
        icon.setOnClickListener(clickListener);



        ImageButton placeOrderButton = (ImageButton)v.findViewById(R.id.placeorderbutton);
        placeOrderButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if(noOfPrices == 1){
                    addItemForOrder(o,0);
                }
                else{
                    selectedMenuItem = o;
                    showDialog(ADD_CHOOSE_QUALIFIER_DIALOG);
                }

            }
        });

        TextView[] no_of_items = new TextView[3];
        TextView[] qualifiers = new TextView[3];
        no_of_items[0] = (TextView)v.findViewById(R.id.no_of_items1);
        no_of_items[1] = (TextView)v.findViewById(R.id.no_of_items2);
        no_of_items[2] = (TextView)v.findViewById(R.id.no_of_items3);
        qualifiers[0] = (TextView)v.findViewById(R.id.labelQualifier1);
        qualifiers[1] = (TextView)v.findViewById(R.id.labelQualifier2);
        qualifiers[2] = (TextView)v.findViewById(R.id.labelQualifier3);
        for(int i=0;i<no_of_items.length;i++){
            if(i<noOfPrices){
                //                OrderElement orderElement = new OrderElement(o, i);
                //                Set<OrderElement> orderElements = orderNumbers.keySet();
                //                int numberOrdered = 0;
                //                for(OrderElement current : orderElements){
                //                    if(orderElement.menuItem.equals(current.menuItem) &&
                //                            orderElement.qualifierIndex == current.qualifierIndex){
                //                        numberOrdered = numberOrdered + orderNumbers.get(current);
                //                    }
                //                }
                StringBuffer qualifierText = new StringBuffer();
                qualifierText.append(QualifierTranslator.translate(
                        o.getQualifiers().get(i),
                        this));
                if(qualifierText.length() > 0){
                    qualifierText.append(": ");
                }
                qualifiers[i].setText(qualifierText);
                //                if(numberOrdered > 0){
                //                    StringBuffer numberText = new StringBuffer("x ");
                //                    numberText.append(numberOrdered);
                //                    no_of_items[i].setText(numberText);
                //                    no_of_items[i].setVisibility(View.VISIBLE);
                //                    showRemoveButton = true;
                //                }
                //                else{
                //                    no_of_items[i].setVisibility(View.INVISIBLE);
                //
                //                }
                qualifiers[i].setVisibility(View.VISIBLE);
            }
            else{

                qualifiers[i].setVisibility(View.GONE);
            }
            no_of_items[i].setVisibility(View.GONE);

        }



    }

    ArrayList<FoodMenuItem> filterMenuItems(long categoryId){
        ArrayList<FoodMenuItem> result = new ArrayList<FoodMenuItem>(m_items.size());
        for(FoodMenuItem elem : m_items){
            if(elem.getCategoryId() == categoryId){
                result.add(elem);
            }
        }
        return result;
    }


    @Override
    protected Dialog onCreateDialog(final int id) {
        Dialog result = null;
        if(id == REVIEW_ORDER_DIALOG){
            result = new Dialog(this);
            final Dialog dialog = result;
            result.setTitle(this.getString(R.string.review_order_dialog_title));
            result.setContentView(R.layout.review_order);

            ListView listView = (ListView)result.findViewById(R.id.listViewReviewOrder);
            listView.setAdapter(reviewOrdersAdapter);

            final Button commitOrderButton = (Button)result.findViewById(R.id.buttonCommitOrder);
            commitOrderButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if(totalAmount >= minimumForDelivery){
                        (new PlaceOrderTask()).execute(
                                ComandaActivity.this,
                                HttpParams.PlaceOrder.SERVICE_NAME,
                                new ArrayList<NameValuePair>(),
                                BooleanHandler.class);
                    }
                    else{
                        final AlertDialog alertDialog = new AlertDialog.Builder(
                                ComandaActivity.this).create();
                        alertDialog.setTitle(R.string.cannotPlaceOrder);
                        StringBuffer message = new StringBuffer(getString(R.string.underMinimumForDelivery1));
                        message.append(" ");
                        message.append(Formatter.money(minimumForDelivery));
                        alertDialog.setMessage(message);
                        alertDialog.setButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                           public void onClick(DialogInterface dialog, int which) {
                              alertDialog.dismiss();
                           }
                        });
                        alertDialog.show();
                    }
                }
            });

            Button backButton = (Button) result.findViewById(R.id.buttonReviewOrderBack);
            backButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    dialog.cancel();
                }
            });

            final TextView tvDeliveryCost = (TextView) result.findViewById(R.id.tvDeliveryCost);
            final LinearLayout deliveryCostPanel = (LinearLayout) result.findViewById(R.id.deliveryCostPanel);
            tvDeliveryCost.setText(Formatter.money(deliveryCost));
            if(orderItems.size() == 0){
                commitOrderButton.setEnabled(false);
                deliveryCostPanel.setVisibility(View.GONE);
            }
            else{
                commitOrderButton.setEnabled(true);
                deliveryCostPanel.setVisibility(View.VISIBLE);
            }
            tvTotalAmount = (TextView)dialog.findViewById(R.id.tvTotalAmount);
            reviewOrdersAdapter.registerDataSetObserver(new DataSetObserver() {

                @Override
                public void onChanged() {
                    if(orderItems.size() > 0){
                        commitOrderButton.setEnabled(true);
                        deliveryCostPanel.setVisibility(View.VISIBLE);
                    }
                    else{
                        commitOrderButton.setEnabled(false);
                        deliveryCostPanel.setVisibility(View.GONE);
                    }
                    refreshTotalAmount();
                }

            });
            refreshTotalAmount();
        }
        else if(id == ITEM_DETAILS_DIALOG){
            result = new Dialog(this);
            result.setContentView(R.layout.menu_item_details);
            Button btnClose = (Button) result.findViewById(R.id.btnCloseMenuItemPopup);
            btnClose.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    dismissDialog(ITEM_DETAILS_DIALOG);
                }
            });
        }
        else if(id == ADD_CHOOSE_QUALIFIER_DIALOG){
            result = new Dialog(this);
            result.setContentView(R.layout.choose_qualifier);
            Button[] btnQualifier = new Button[3];
            btnQualifier[0] = (Button)result.findViewById(R.id.btnQualifier1);
            btnQualifier[1] = (Button)result.findViewById(R.id.btnQualifier2);
            btnQualifier[2] = (Button)result.findViewById(R.id.btnQualifier3);
            for(int i=0;i<btnQualifier.length; i++){
                final int index = i;
                btnQualifier[i].setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        addItemForOrder(selectedMenuItem, index);
                        dismissDialog(id);
                    }
                });
            }
            Button btnBack = (Button)result.findViewById(R.id.btnBack);
            btnBack.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    dismissDialog(id);

                }
            });
        }
        else if(id == CHOOSE_EXTRA_DIALOG){
            result = new Dialog(this);
            result.setContentView(R.layout.choose_extra);
            Button btnBack = (Button)result.findViewById(R.id.btnBack);
            btnBack.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    dismissDialog(id);

                }
            });

        }
        else{
            result = super.onCreateDialog(id);
        }
        return result;
    }

    private void refreshTotalAmount(){
        float total = 0;
        if(orderItems != null){
            for(OrderElement currentElement : orderItems){
                total = total + currentElement.toalPrice * orderNumbers.get(currentElement);
            }
            if(orderItems.size() > 0){
                total = total + deliveryCost;
            }
            if(tvTotalAmount != null){
                tvTotalAmount.setText(Formatter.money(total));
            }
            totalAmount = total;
        }
    }

    @Override
    protected void onPrepareDialog(final int id, Dialog dialog) {

        if(id == ITEM_DETAILS_DIALOG){
            TextView text = (TextView) dialog.findViewById(R.id.contextMenuItemDescription);
            text.setText(selectedMenuItem.getDescription());
            TextView textName = (TextView) dialog.findViewById(R.id.contextMenuItemName);
            textName.setText(selectedMenuItem.getName());
            String imageString = selectedMenuItem.getImageString();
            Bitmap largeBitmap = null;
            if(largeBitmaps.containsKey(imageString)){
                largeBitmap = largeBitmaps.get(imageString);
            }
            if(largeBitmap != null){
                ImageView image = (ImageView) dialog.findViewById(R.id.image);
                image.setImageBitmap(largeBitmap);
            }
            LayoutHelper.dialog_fill_parent(dialog);
        }
        else if(id == REVIEW_ORDER_DIALOG){

            LayoutHelper.dialog_fill_parent(dialog);
        }
        else if(id == ADD_CHOOSE_QUALIFIER_DIALOG){
            TextView tvItemName = (TextView)dialog.findViewById(R.id.tvItemName);
            StringBuffer title = new StringBuffer();

            title.append(getString(R.string.add));
            title.append(" ");
            title.append(selectedMenuItem.getName());
            tvItemName.setText(title);
            //FIXME: Do this outside so as to save some time during dialog 
            //preparation
            Button[] btnQualifier = new Button[3];
            btnQualifier[0] = (Button)dialog.findViewById(R.id.btnQualifier1);
            btnQualifier[1] = (Button)dialog.findViewById(R.id.btnQualifier2);
            btnQualifier[2] = (Button)dialog.findViewById(R.id.btnQualifier3);
            final int noOfPrices = selectedMenuItem.getPrices().size();
            for(int i = 0;i<btnQualifier.length;i++){
                if(i<noOfPrices){
                    btnQualifier[i].setVisibility(View.VISIBLE);
                    btnQualifier[i].setText(QualifierTranslator.translate(
                            selectedMenuItem.getQualifiers().get(i),
                            this));
                }
                else{
                    btnQualifier[i].setVisibility(View.GONE);
                }
            }
            LayoutHelper.dialog_fill_parent(dialog);

        }
        else if(id == CHOOSE_EXTRA_DIALOG){
            TextView tvExtrasName = (TextView)dialog.findViewById(R.id.tvExtrasName);
            StringBuffer name = new StringBuffer(getString(R.string.choose));
            name.append(" ");
            name.append(selectedMenuItem.getExtrasName());
            tvExtrasName.setText(name);
            ListView lvExtras = (ListView)dialog.findViewById(R.id.lvExtras);
            final ExtrasAdapter adapter = new ExtrasAdapter(
                    this, R.layout.choose_extra_row, selectedMenuItem);
            lvExtras.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            Button btnOK = (Button)dialog.findViewById(R.id.btnOK);
            btnOK.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    doAddItemForOrder(selectedMenuItem, selectedQualifier, adapter.getSelected());
                    dismissDialog(id);
                }
            });
            LayoutHelper.dialog_fill_parent(dialog);
        }
        else{
            super.onPrepareDialog(id, dialog);
        }
    }

    private class ExtrasAdapter extends ArrayAdapter<String>{

        private FoodMenuItem item;
        private boolean[] selected;

        public ExtrasAdapter(Context context, int textViewResourceId,
                FoodMenuItem item){
            super(context, textViewResourceId, item.getExtras());
            this.item = item;
            selected = new boolean[item.getExtras().size()];
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if(v == null){
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.choose_extra_row, null);


            }
            CheckBox cbExtra = (CheckBox)v.findViewById(R.id.cbExtra);
            cbExtra = (CheckBox)v.findViewById(R.id.cbExtra);
            cbExtra.setText(item.getExtras().get(position));
            cbExtra.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    selected[position] = isChecked;

                }
            });
            TextView tvPrice = (TextView) v.findViewById(R.id.price);
            float price = item.getExtrasPrice().get(position);
            if(price > 0){
                tvPrice.setVisibility(View.VISIBLE);
                StringBuffer priceSB = new StringBuffer("+");
                priceSB.append(Formatter.money(price));
                tvPrice.setText(priceSB);
            }
            else{
                tvPrice.setVisibility(View.INVISIBLE);
            }
            cbExtra.setChecked(selected[position]);
            return v;
        }

        public List<Integer> getSelected(){
            final int totalExtras = selected.length;
            List<Integer> result = new ArrayList<Integer>(totalExtras);
            for(int i=0;i<totalExtras;i++){
                if(selected[i]){
                    result.add(i);
                }
            }
            return result;
        }

    }

    private class ReviewOrderAdapter extends ArrayAdapter<OrderElement>{

        public ReviewOrderAdapter(Context context, int textViewResourceId,
                List<OrderElement> objects) {
            super(context, textViewResourceId, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) 
            {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                //inflate using res/layout/row.xml
                v = vi.inflate(R.layout.review_order_row, null);
            }
            final OrderElement o = orderItems.get(position);
            if (o != null) 
            {
                final TextView tvNoOfItems = (TextView)v.findViewById(R.id.no_of_items);
                final TextView tvItemName = (TextView)v.findViewById(R.id.item_name);
                final TextView tvPrice = (TextView)v.findViewById(R.id.price);
                final ImageButton removeOrderButton = (ImageButton)v.findViewById(R.id.removeorderbutton);
                StringBuffer elementName = new StringBuffer();
                elementName.append(o.menuItem.getName());
                elementName.append(" (");
                elementName.append(categoriesMap.get(o.menuItem.getCategoryId()).name);
                elementName.append(")");
                String qualifier = o.menuItem.getQualifiers().get(o.qualifierIndex);
                if("SINGLE".equals(qualifier) == false){
                    elementName.append(" (");
                    elementName.append(QualifierTranslator.translate(qualifier, ComandaActivity.this));
                    elementName.append(")");
                }
                if(o.extras != null){
                    elementName.append(" - ");
                    elementName.append(o.menuItem.getExtrasName());
                    elementName.append(": ");
                    for(int i : o.extras){
                        elementName.append(o.menuItem.getExtras().get(i));
                        elementName.append(", ");
                    }
                    elementName.delete(elementName.length() - 2,
                            elementName.length());

                }
                tvItemName.setText(elementName);

                tvPrice.setText(Formatter.money(o.toalPrice));
                tvNoOfItems.setText("" + orderNumbers.get(o));
                removeOrderButton.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        removeItemFromOrder(o);
                    }
                });
            }

            return v;
        }

    }


    private void doAddItemForOrder(FoodMenuItem item, int qualifierIndex, List<Integer> extras){
        OrderElement element = new OrderElement(item, qualifierIndex);
        element.extras = extras;
        if(orderNumbers.containsKey(element)){
            int previousnumber = orderNumbers.get(element);
            orderNumbers.put(element,previousnumber + 1);
        }
        else{
            float price = item.getPrices().get(qualifierIndex);
            if(extras != null){
                for(int i : extras){
                    price = price + item.getExtrasPrice().get(i);
                }
            }
            element.toalPrice = price;
            orderItems.add(element);
            orderNumbers.put(element,1);
        }
        reviewOrdersAdapter.notifyDataSetChanged();
        //        refreshAllTables();
    }

    private void addItemForOrder(FoodMenuItem item, int qualifierIndex){
        if(item.getExtras() != null && item.getExtras().size() > 1){
            selectedMenuItem = item;
            selectedQualifier = qualifierIndex;
            showDialog(CHOOSE_EXTRA_DIALOG);
        }
        else{
            doAddItemForOrder(item, qualifierIndex, null);
        }
    }

    private void removeItemFromOrder(OrderElement element){
        if(orderNumbers.containsKey(element)){
            int previousnumber = orderNumbers.get(element);
            if(previousnumber > 1){
                orderNumbers.put(element,previousnumber - 1);
            }
            else{
                orderItems.remove(element);
                orderNumbers.remove(element);
            }
            reviewOrdersAdapter.notifyDataSetChanged();
            //            refreshAllTables();
        }
        else{
            throw new IllegalStateException("Not in the order list");
        }
    }

    private void refreshAllTables(){
        for(ItemAdapter adapter : adapters){
            adapter.notifyDataSetChanged();
        }
    }

}
