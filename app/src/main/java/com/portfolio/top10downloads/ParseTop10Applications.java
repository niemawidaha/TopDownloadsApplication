package com.portfolio.top10downloads;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;

public class ParseTop10Applications {

    private static final String TAG = "ParseTop10Applications";

    private ArrayList<RSSFeedEntry> applications; // to save the incoming data

    public ParseTop10Applications() {

        // initialize array list
        applications = new ArrayList<>();
    }

    public ArrayList<RSSFeedEntry> getApplications() {
        return applications;
    }

    // the method that will manipulate the XML string for our data
    public boolean parse(String xmlData){

        boolean status = true;
        RSSFeedEntry currentRecords = null;
        boolean inEntry = false; // to make sure we're at the right section
        String textValue = "";

        try{
            // to set up the XML classes that are part of java

            XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
            xmlPullParserFactory.setNamespaceAware(true);
            XmlPullParser xpp = xmlPullParserFactory.newPullParser();
            xpp.setInput(new StringReader(xmlData));
            int eventType = xpp.getEventType();

            // this loop will keep processing through XML until the end of the doc is reached
            while(eventType != xpp.END_DOCUMENT){

                // this is where we can get the value of the data we're interested in
                String tagName = xpp.getName();

                switch(eventType){
                    case XmlPullParser.START_TAG:
                        Log.d(TAG, "parse: Starting tag for - " + tagName);

                        if("entry".equalsIgnoreCase(tagName)){
                            inEntry = true;
                            currentRecords = new RSSFeedEntry();
                        }
                        break;

                    case XmlPullParser.TEXT:
                        textValue = xpp.getText();
                        break;

                    case XmlPullParser.END_TAG:
                        Log.d(TAG, "parse: Ending tag for - " + tagName);
                        if(inEntry){
                            if("entry".equalsIgnoreCase(tagName)){
                                applications.add(currentRecords);
                                inEntry = false;
                            } else if("name".equalsIgnoreCase(tagName)){
                                currentRecords.setName(textValue);
                            } else if("artist".equalsIgnoreCase(tagName)){
                                currentRecords.setArtist(textValue);
                            } else if("releaseDate".equalsIgnoreCase(tagName)){
                                currentRecords.setReleaseDate(textValue);
                            } else if("summary".equalsIgnoreCase(tagName)){
                                currentRecords.setSummary(textValue);
                            } else if("image".equalsIgnoreCase(tagName)){
                                currentRecords.setImageURL(textValue);
                            }
                        }
                        break;

                    default:
                        // Nothing else to do
                } // ends switch
                eventType = xpp.next();
            } // ends while

            // traverse the list and print the list:
            for(RSSFeedEntry entry: applications){
                Log.d(TAG, "***********");
                Log.d(TAG, entry.toString());
            }

        } catch (Exception e){
            // any type of exception will be called; there's no specifics
            status = false;
            e.printStackTrace();
        }

        return status;
    }
}
