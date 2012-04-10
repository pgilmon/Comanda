package com.company.comanda.brian.xmlhandlers;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


import com.company.comanda.brian.model.FoodMenuItem;
import static com.company.comanda.common.XmlTags.MenuItemList.*;


public class MenuItemsHandler extends ComandaXMLHandler<ArrayList<FoodMenuItem>>
{

    // ===========================================================
    // Fields
    // ===========================================================
    

    private static final Logger log = LoggerFactory.getLogger(MenuItemsHandler.class);
    
    private boolean in_item = false;
    private boolean in_name = false;
    private boolean in_keyId = false;
    private boolean in_description = false;
    private boolean in_imageString = false;
    private boolean in_categoryId = false;
    private boolean in_price = false;
    private boolean in_qualifier = false;
    private boolean in_extra_name = false;
    private boolean in_extra_price = false;
    private boolean in_extras_global_name = false;

    private FoodMenuItem item = null;
    private List<Float> prices = null;
    private List<String> qualifiers = null;
    private List<String> extras = null;
    private List<Float> extrasPrices = null;
    

    private ArrayList<FoodMenuItem> items = null;


    // ===========================================================
    // Methods
    // ===========================================================
    @Override
    public void startDocument() throws SAXException 
    {
        log.debug("Initiating parser...");
        this.items = new ArrayList<FoodMenuItem>();
    }

    @Override
    public void endDocument() throws SAXException 
    {
        // Nothing to do
    }

    /** Gets be called on opening tags like: 
     * <tag> 
     * Can provide attribute(s), when xml was like:
     * <tag attribute="attributeValue">*/
    @Override
    public void startElement(String namespaceURI, 
            String localName, String qName, 
            Attributes atts) throws SAXException{
        if (localName.equals(ITEM)) 
        {
            this.in_item = true;
            item = new FoodMenuItem();
            prices = new ArrayList<Float>();
            qualifiers = new ArrayList<String>();
            extras = new ArrayList<String>();
            extrasPrices = new ArrayList<Float>();
            log.debug("Found an Item");
        }
        else if (localName.equals(NAME)) 
        {
            this.in_name = true;
        }
        else if (localName.equals(IMAGE_STRING)) 
        {
            this.in_imageString = true;
        }
        else if (localName.equals(ID)) 
        {
            this.in_keyId = true;
        }
        else if (localName.equals(DESCRIPTION)) 
        {
            this.in_description = true;
        }
        else if (localName.equals(CATEGORY_ID)) 
        {
            this.in_categoryId = true;
        }
        else if (localName.equals(PRICE)) 
        {
            this.in_price = true;
        }
        else if (localName.equals(QUALIFIER)) 
        {
            this.in_qualifier = true;
        }
        else if (localName.equals(EXTRA_NAME)) 
        {
            this.in_extra_name = true;
        }
        else if (localName.equals(EXTRA_PRICE)) 
        {
            this.in_extra_price = true;
        }
        else if (localName.equals(EXTRAS_GLOBAL_NAME)) 
        {
            this.in_extras_global_name = true;
        }
    }

    /** Gets be called on closing tags like: 
     * </tag> */
    @Override
    public void endElement(String namespaceURI, String localName,
            String qName) throws SAXException {
        if (localName.equals(ITEM)) 
        {
            this.in_item = false;
            item.setPrices(prices);
            item.setQualifiers(qualifiers);
            item.setExtras(extras);
            item.setExtrasPrice(extrasPrices);
            items.add(item);
        }
        else if (localName.equals(NAME)) 
        {
            this.in_name = false;
        }
        else if (localName.equals(IMAGE_STRING)) 
        {
            this.in_imageString = false;
        }
        else if (localName.equals(ID)) 
        {
            this.in_keyId = false;
        }
        else if (localName.equals(DESCRIPTION)) 
        {
            this.in_description = false;
        }
        else if (localName.equals(CATEGORY_ID)) 
        {
            this.in_categoryId = false;
        }
        else if (localName.equals(PRICE)) 
        {
            this.in_price = false;
        }
        else if (localName.equals(QUALIFIER)) 
        {
            this.in_qualifier = false;
        }
        else if (localName.equals(EXTRA_NAME)) 
        {
            this.in_extra_name = false;
        }
        else if (localName.equals(EXTRA_PRICE)) 
        {
            this.in_extra_price = false;
        }
        else if (localName.equals(EXTRAS_GLOBAL_NAME)) 
        {
            this.in_extras_global_name = false;
        }
    }

    /** Gets be called on the following structure: 
     * <tag>characters</tag> */

    @Override
    public void characters(char ch[], int start, int length) 
    {
        if(this.in_item)
        {
            String textBetween = new String(ch, start, length);
            if(this.in_name){
                log.debug("Name: {}", textBetween);
                item.setName(textBetween);
            }
            else if(this.in_imageString){
                log.debug("ImageString: {}", textBetween);
                item.setImageString(textBetween);
            }
                
            else if(this.in_description){
                log.debug("Description: {}", textBetween);
                item.setDescription(textBetween);
            }
            else if(this.in_keyId){
                log.debug("KeyId: {}", textBetween);
                //We are not parsing the long here. What for?
                item.setKeyId(textBetween);
            }
            else if(this.in_categoryId){
                log.debug("CategoryId: {}", textBetween);
                item.setCategoryId(Long.parseLong(textBetween));
            }
            else if(this.in_price){
                log.debug("Price: {}", textBetween);
                prices.add(Float.parseFloat(textBetween));
            }
            else if(this.in_qualifier){
                log.debug("Qualifier: {}", textBetween);
                qualifiers.add(textBetween);
            }
            else if(this.in_extra_name){
                log.debug("Extra: {}", textBetween);
                extras.add(textBetween);
            }
            else if(this.in_extra_price){
                log.debug("Extra price: {}", textBetween);
                extrasPrices.add(Float.parseFloat(textBetween));
            }
            else if(this.in_extras_global_name){
                log.debug("Extras global name: {}", textBetween);
                item.setExtrasName(textBetween);
            }
        }
    }

    @Override
    public ArrayList<FoodMenuItem> getParsedData() {
        return this.items;
    }
}
