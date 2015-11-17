package hw.lab2.s1021527;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import java.util.ArrayList;

/**
 * Created by 睡睡 on 2015/10/28.
*   這個class用來控制左拉式的選單
 */

public class FeedMenu {
    //設定ID從100000開始
    //subMenID從10000開始
    //menuItemID從0開始
    private Menu menu;
    private ArrayList<RSSFeed> feedList = new ArrayList<RSSFeed>();
    private ArrayList<MenuItem> menuItemList = new ArrayList<MenuItem>();
    private ArrayList<SubMenu> subMenuList = new ArrayList<SubMenu>();
    private int num = 0;
    private int group = 0;
    FeedMenu( Menu m )
    {
        menu = m;
        subMenuList.add(menu.addSubMenu(group, Menu.FIRST + 10000, group, "default"));
        SubMenu sub = menu.addSubMenu(100, Menu.FIRST + 100000, 100, "setting");
        sub.add(100, Menu.FIRST + 100000 + 1, 100, "刪除feed");
        sub.add(100, Menu.FIRST + 100000 + 2, 101, "新增分類");
        sub.add(100, Menu.FIRST + 100000 + 3, 102, "刪除分類");
        group++;
    }
    public void addMenuItem(RSSFeed _feed, int group)//新增
    {
        if ( _feed != null ) {
            feedList.add(_feed);
            SubMenu sub = subMenuList.get(group);
            menuItemList.add(sub.add(sub.getItem().getGroupId(), Menu.FIRST + num, subMenuList.size(), _feed.getTitle()));
            num++;
        }
    }
    public void addSubMenu(String str)//新增
    {
        subMenuList.add(menu.addSubMenu(group, Menu.FIRST + 10000 + group, group, str));
        group++;
    }
    public void addSubMenu(String str, int g)//新增
    {
        subMenuList.add(menu.addSubMenu(g, Menu.FIRST + 10000 + g, g, str));
        group = g + 1;
    }
    public boolean Isfind(RSSFeed _feed)///是否重複
    {
        if ( _feed == null )
            return false;
        for ( int i = 0 ; i < feedList.size() ; i++ ){
            if ( feedList.get(i).getTitle().equals(_feed.getTitle()) )
                    return true;
        }
        return false;
    }
    public void delete( int pos )
    {
        feedList.remove(pos);
        int g = getGroup(pos);
        for ( int i = 0 ; i < subMenuList.size() ; i++ ){
            if ( g == subMenuList.get(i).getItem().getGroupId() )
                subMenuList.get(i).removeItem(getmenuItemID(pos));
        }
        menuItemList.remove(pos);

    }
    public void deleteSub( int pos )
    {
        int g = subMenuList.get(pos).getItem().getGroupId();
        for ( int i = 0 ; i < size() ; i++ ) {
            if( g == getGroup(i) ) {
                delete(i);
                i--;
            }
        }
        subMenuList.remove(pos);
    }
    public void updateFeed( RSSFeed _feed)
    {
        if ( _feed != null ) {
            int i = feedPos(_feed);
            if ( i == -1 ) {
                Log.e("error","updataFeed error");
            }
            else{
                feedList.set(i, _feed);
            }
        }
    }
    public void updateFeed( RSSFeed _feed, int i)
    {
        if ( _feed != null ) {
            feedList.set(i, _feed);
        }
    }


    public String[] getAllMenuTitle()
    {
        String[] s = new String[feedList.size()];
        for ( int i = 0 ; i < feedList.size() ; i++ ) {
            s[i] = feedList.get(i).getTitle();
        }
        return s;
    }
    public String[] getAllMenuUrl()
    {
        String[] s = new String[feedList.size()];
        for ( int i = 0 ; i < feedList.size() ; i++ ) {
            s[i] = feedList.get(i).getUrl();
        }
        return s;
    }
    public String[] getAllMenuPubDate()
    {
        String[] s = new String[feedList.size()];
        for ( int i = 0 ; i < feedList.size() ; i++ ) {
            s[i] = feedList.get(i).getPubDate();
        }
        return s;
    }
    public String[] getAllSubMenuTitle()
    {
        String[] s = new String[subMenuList.size()];
        for ( int i = 0 ; i < subMenuList.size() ; i++ ) {
            s[i] = subMenuList.get(i).getItem().getTitle().toString();
        }
        return s;
    }
    public int getSubGroup( int pos )
    {
        return subMenuList.get(pos).getItem().getGroupId();
    }
    public int getSubSize()
    {
        return subMenuList.size();
    }

    public int getmenuItemID( int pos )
    {
        return menuItemList.get(pos).getItemId();
    }
    public Menu getMenu()
    {
        return menu;
    }
    public int getGroup( int pos )
    {
        return menuItemList.get(pos).getGroupId();
    }
    public int size()
    {
        return feedList.size();
    }
    public RSSFeed getFeed( int id )
    {
        return feedList.get(id);
    }

    private int feedPos( RSSFeed _feed)
    {
        if( _feed != null) {
            for ( int i = 0 ; i < size() ; i++ ){
                if( feedList.get(i).getTitle().equals(_feed.getTitle()) )
                    return i;
            }
        }
        return -1;
    }
}
