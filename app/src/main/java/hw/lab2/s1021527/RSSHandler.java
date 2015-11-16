package hw.lab2.s1021527;

import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Created by 睡睡 on 2015/10/24.
 */
public class RSSHandler extends DefaultHandler {
    RSSFeed feed;
    RSSItem item;
    String lastElementName = "";
    String url = null;
    boolean bFoundChannel = false;
    boolean firstTitle = true;
    boolean firstPubDate = true;
    final int RSS_TITLE = 1;
    final int RSS_LINK = 2;
    final int RSS_DESCRIPTION = 3;
    final int RSS_CATEGORY = 4;
    final int RSS_PUBDATE = 5;

    int depth = 0;
    int currentstate = 0;
    RSSHandler( String str) { url = str;}
    public RSSFeed getFeed() {return feed;}
    public void startDocument() throws SAXException
    {
        // initialize our RSSFeed object - this will hold our parsed contents
        feed = new RSSFeed();
        // initialize the RSSItem object - we will use this as a crutch to grab the info from the channel
        // because the channel and items have very similar entries..
        item = new RSSItem();
        feed.setUrl(url);
    }
    public void startElement(String namespaceURI, String localName,String qName, Attributes atts) throws SAXException
    {
        depth++;
        if (localName.equals("channel"))
        {
            currentstate = 0;
            return;
        }
        if (localName.equals("item"))
        {
            // create a new item
            item = new RSSItem();
            return;
        }
        if (localName.equals("title"))
        {
            currentstate = RSS_TITLE;
            return;
        }
        if (localName.equals("description"))
        {
            currentstate = RSS_DESCRIPTION;
            return;
        }
        if (localName.equals("link"))
        {
            currentstate = RSS_LINK;
            return;
        }
        if (localName.equals("category"))
        {
            currentstate = RSS_CATEGORY;
            return;
        }
        if (localName.equals("pubDate") || localName.equals("published"))
        {
            currentstate = RSS_PUBDATE;
            return;
        }
        // if we don't explicitly handle the element, make sure we don't wind up erroneously
        // storing a newline or other bogus data into one of our existing elements
        currentstate = 0;
    }
    public void characters(char ch[], int start, int length)
    {
        String theString = new String(ch,start,length);
        Log.i("RSSReader", "characters[" + theString + "]");

        switch (currentstate)
        {
            case RSS_TITLE:
                if ( firstTitle ){
                    feed.setTitle(theString);
                    firstTitle = false;
                }
                else
                    item.setTitle(theString);
                currentstate = 0;
                break;
            case RSS_LINK:
                item.setLink(theString);
                currentstate = 0;
                break;
            case RSS_DESCRIPTION:
                ///有空要做處理///
                item.setDescription(theString);
                currentstate = 0;
                break;
            case RSS_CATEGORY:
                item.setCategory(theString);
                currentstate = 0;
                break;
            case RSS_PUBDATE:
                if ( firstPubDate ){
                    feed.setPubDate(theString);
                    firstPubDate = false;
                }
                else
                    item.setPubDate(theString);
                currentstate = 0;
                break;
            default:
                return;
        }

    }
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException
    {
        depth--;
        if (localName.equals("item"))
        {
            // add our item to the list!
            feed.addItem(item);
            item = null;
            System.gc();
            return;
        }
    }
    public void endDocument() throws SAXException
    {
    }

}
