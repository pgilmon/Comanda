package com.company.comanda.peter.server.model;

import com.google.appengine.api.datastore.Key;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class MenuItem
{
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;
    @Persistent
    private String name;
    @Persistent
    private String description;
    @Persistent
    private String imageString;

    public MenuItem(String name, String desc, String imageString)
    {
        this.name = name;
        this.description = desc;
        this.imageString = imageString;
    }
    public Key getKey() 
    {
        return key;
    }
    public String getName() 
    {
        return name;
    }
    public String getDescription() 
    {
        return description;
    }
    public void setName(String t)
    {
     this.name = t;
    }    
    public void setDescription(String d)
    {
     this.description = d;
    }
    public String getImageString() {
        return imageString;
    }
    public void setImageString(String imageString) {
        this.imageString = imageString;
    } 
    
    
}