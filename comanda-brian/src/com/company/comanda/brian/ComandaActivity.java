package com.company.comanda.brian;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.company.comanda.brian.model.FoodMenuItem;

public class ComandaActivity extends ListActivity
{
    private ProgressDialog m_ProgressDialog = null; 
    private ArrayList<FoodMenuItem> m_items = null;
    private ItemAdapter m_adapter;
    private Runnable viewItems;
    private Runnable returnRes = new Runnable()
    {
        @Override
        public void run() 
        {
            if(m_items != null && m_items.size() > 0)
            {
                m_adapter.notifyDataSetChanged();
                m_adapter.clear();
                for(int i=0;i<m_items.size();i++)
                    m_adapter.add(m_items.get(i));
            }
            m_ProgressDialog.dismiss();
            m_adapter.notifyDataSetChanged();
        }
    };
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        fetchContent();
    }
    public void fetchContent()
    {
        m_items = new ArrayList<FoodMenuItem>();
        //set ListView adapter to basic ItemAdapter 
        //(it's a coincidence they are both called Item)
        this.m_adapter = new ItemAdapter(this, R.layout.row, m_items);
        setListAdapter(this.m_adapter);
        //create a Runnable object that does the work 
        //of retrieving the XML data online
        //This will be run in a new Thread
        viewItems = new Runnable()
        {
            @Override
            public void run() 
            {
                //this is where we populate m_items (ArrayList<FoodMenuItem>) 
                //which we can get from XML
                //the XML can be updated via Google App-Engine
                try
                {
                    getData();
                } 
                catch (Exception e) 
                { 
                    Log.e("ListViewSampleApp", "Unable to retrieve data.", e);
                }
                //This executes returnRes (see above) which will use the 
                //ItemAdapter to display the contents of m_items
                runOnUiThread(returnRes);
            }
        };
        //Create a new Thread to run viewItems
        Thread thread =  new Thread(null, viewItems, "MagentoBackground");
        thread.start();
        //Make a popup progress dialog while we fetch and parse the data
        m_ProgressDialog = ProgressDialog.show(this, "Please wait...",
                "Retrieving data ...", true);
    }
    private void getData() throws IOException
    {
        try 
        {
            // Create a URL we want to load some xml-data from.
            URL url = new URL("http://pgmtestapp.appspot.com/menuitems");
            // Get a SAXParser from the SAXPArserFactory.
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            // Get the XMLReader of the SAXParser we created.
            XMLReader xr = sp.getXMLReader();
            // Create a new ContentHandler and 
            //apply it to the XML-Rea der
            XMLHandler xmlHandler = new XMLHandler();
            xr.setContentHandler(xmlHandler);
            InputSource xmlInput = new InputSource(url.openStream());
            Log.e("ListViewSampleApp", "Input Source Defined: "+ xmlInput.toString());
            /* Parse the xml-data from our URL. */
            xr.parse(xmlInput);
            /* Parsing has finished. */
            /* XMLHandler now provides the parsed data to us. */
            m_items = xmlHandler.getParsedData(); 
        } 
        catch (Exception e) 
        {
            Log.e("ListViewSampleApp XMLParser", "XML Error", e);
        }
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
            this.items = items;
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
            FoodMenuItem o = items.get(position);
            if (o != null) 
            {
                //Set all of the UI components 
                //with the respective Object data
//                ImageView icon = (ImageView) v.findViewById(R.id.icon);
                TextView tt = (TextView) v.findViewById(R.id.toptext);
                TextView bt = (TextView) v.findViewById(R.id.bottomtext);
                if (tt != null)
                {
                    tt.setText("Name: "+o.getName());   
                }
                if(bt != null)
                {
                    bt.setText("No description");
                }
                //                      if(icon != null)
                //                      {
                //                          URL imageURL = null;
                //                          try      
                //                          {        
                //                              //use our image serve page to get the image URL
                //                              imageURL = new URL("http://yourapp.appspot.com/serveBlob?id="
                //                                      + o.getImageKey());
                //                          } 
                //                          catch (MalformedURLException e) 
                //                          {
                //                              e.printStackTrace();
                //                          }
                //                          try 
                //                          {
                //                              //Decode and resize the image then set as the icon
                //                              BitmapFactory.Options options = new BitmapFactory
                //                                      .Options();
                //                              options.inJustDecodeBounds = true;
                //                              options.inSampleSize = 1/2;
                //                              Bitmap bitmap = BitmapFactor
                //                                      .decodeStream((InputStream)imageURL
                //                                              .getContent());
                //                              Bitmap finImg = Bitmap
                //                                      .createScaledBitmap(bitmap, 50, 50, false);
                //                              icon.setImageBitmap(finImg);
                //                          } 
                //                          catch (IOException e) 
                //                          {                        
                //                              e.printStackTrace();
                //                          }
                //                      }
            }
            //returns the view to the Adapter to be displayed
            return v;
        }        
    }
}
