package hw.lab2.s1021527;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by 睡睡 on 2015/10/24.
 */
public class RSSFeed
{
    private String title = null;
    private String pubdate = null;
    private String url = null;
    private int itemcount = 0;
    private List<RSSItem> itemlist;


    public RSSFeed()
    {
        itemlist = new ArrayList<>();
    }
    public int addItem(RSSItem item)
    {
        itemlist.add(item);
        itemcount++;
        return itemcount;
    }

    public void setTitle(String str)
    {
        title = str;
    }
    public void setPubDate(String str)
    {
        pubdate = str;
    }
    public void setUrl(String str){ url = str;}
    public RSSItem getItem(int pos)
    {
        return itemlist.get(pos);
    }
    public List getItemList()
    {
        return itemlist;
    }
    public int getItemCount()
    {
        return itemcount;
    }
    public String getTitle()
    {
        return title;
    }
    public String getPubDate()
    {
        return pubdate;
    }
    public String getUrl(){ return url;}

}
