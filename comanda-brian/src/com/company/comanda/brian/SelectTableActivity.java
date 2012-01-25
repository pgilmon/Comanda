package com.company.comanda.brian;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class SelectTableActivity extends Activity
{
    private ProgressDialog m_ProgressDialog = null; 
    private ArrayList<String> tables = null;
    private ArrayAdapter<String> m_adapter;
    private Runnable viewItems;
    private Runnable returnRes = new Runnable()
    {
        @Override
        public void run() 
        {
            if(tables != null && tables.size() > 0)
            {
                m_adapter.notifyDataSetChanged();
                m_adapter.clear();
                for(int i=0;i<tables.size();i++)
                    m_adapter.add(tables.get(i));
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
        setContentView(R.layout.select_table);
        fetchContent();
    }
    public void fetchContent()
    {
        tables = new ArrayList<String>();
        //set ListView adapter to basic ItemAdapter 
        //(it's a coincidence they are both called Item)
        Spinner spinner = (Spinner) findViewById(R.id.select_table_spinner);
        this.m_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, tables);
        this.m_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(m_adapter);
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
            URL url = new URL("http://10.0.2.2:8888/getTables");
            // Get a SAXParser from the SAXPArserFactory.
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            // Get the XMLReader of the SAXParser we created.
            XMLReader xr = sp.getXMLReader();
            // Create a new ContentHandler and 
            //apply it to the XML-Rea der
            TablesXMLHandler xmlHandler = new TablesXMLHandler();
            xr.setContentHandler(xmlHandler);
            InputSource xmlInput = new InputSource(url.openStream());
            Log.e("ListViewSampleApp", "Input Source Defined: "+ xmlInput.toString());
            /* Parse the xml-data from our URL. */
            xr.parse(xmlInput);
            /* Parsing has finished. */
            /* XMLHandler now provides the parsed data to us. */
            tables = xmlHandler.getParsedData(); 
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

    
    
}