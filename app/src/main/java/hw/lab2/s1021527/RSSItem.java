package hw.lab2.s1021527;

/**
 * Created by 睡睡 on 2015/10/24.
 */
public class RSSItem
{
    private String title = null;
    private String description = null;
    private String link = null;
    private String category = null;
    private String pubdate = null;

    public RSSItem(){}
    public void setTitle(String str){ title = str; }
    public void setDescription(String str){ description = str; }
    public void setLink(String str){ link = str; }
    public void setCategory(String str){ category = str; }
    public void setPubDate(String str){ pubdate = str; }
    public String getTitle(){return title;}
    public String getDescription(){return description;}
    public String getLink(){return link;}
    public String getCategory(){return category;}
    public String getPubDate(){return pubdate;}
    @Override
    public String toString() // 重载了方法toString()
    {
        // limit how much text we display
        if (title.length() > 42)
        {
            return title.substring(0, 42) + "...";
        }
        return title;
    }
}