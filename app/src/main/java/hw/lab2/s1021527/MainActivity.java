package hw.lab2.s1021527;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FeedMenu feedMenu;  //左邊欄位的Menu
    private Button btn_search;  //搜尋的按鈕
    private RSSFeed feed;       //目前頁面的feed
    private RSSFeed feedNew;    //新的feed用在自動刷新
    private String str_Url;     //網址
    private EditText editText;  //EditText
    private SoundPool soundPool;//聲音
    private int alertId;        //聲音檔ID
    private int second = 30;    //刷新秒數
    MenuItem addItem;           //星星
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        feedMenu = new FeedMenu(navigationView.getMenu());//把左邊欄位Menu傳給FeedMenu Class 交給class處理
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 5);
        alertId = soundPool.load(this, R.raw.nudge, 1);     //獲得聲音檔
        //region 搜尋RSS按鈕
        editText = (EditText)findViewById(R.id.editText);
        btn_search = (Button) findViewById(R.id.btn_search);
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                str_Url = editText.getEditableText().toString();
                new Thread() {
                    @Override
                    public void run() {     //使用網路相關的功能要用thread android 4.0以上都要這樣，為了安全性
                        feedNew = getFeed(str_Url);    //從URL取得目前feed
                        if(feedNew != null)
                            feed = feedNew;
                        Message msgg = new Message();//component要交給Handler處理
                        msgg.what = 1;
                        mHandler.sendMessage(msgg);
                    }
                }.start();
            }
        });
        //endregion

        load(); //讀檔

        //region sever檢查跟新---接收broadcast
        IntentFilter filter = new IntentFilter("myService"); //哪個key的訊息與Service對應
        MyReceiver Receiver = new MyReceiver();
        registerReceiver(Receiver, filter);     //註冊接收broadcast
        //endregion

        //updateAllFeed(); //thread跟新   改成service所以註解掉
        startService();//啟用service
    }
    public class MyReceiver extends BroadcastReceiver {     //接收broadcast
        @Override
        public void onReceive(Context context, Intent intent) {
            final Bundle bundle = intent.getExtras();   //接收的參數
            String title = bundle.getString("title");   //有更新的feed title
            Toast.makeText(MainActivity.this, "Sever檢查" + title + "有更新", Toast.LENGTH_SHORT).show();
            Thread t = new Thread(new Runnable() {  //有更新就要更新feedMenu內的資料
                @Override
                public void run() {
                    feedNew = getFeed(bundle.getString("url"));
                    feedMenu.updateFeed(feedNew);   //更新Menu
                    if( feed != null ) {
                        if (feedNew.getTitle().equals(feed.getTitle())) {
                            feed = feedNew;    //從URL取得目前feed
                            Message msgg = new Message();//component要交給Handler處理
                            msgg.what = 1;
                            mHandler.sendMessage(msgg);
                        }
                    }
                    stopService();  //停止Service
                    startService(); //啟動Service 為了跟新Service參數
                }
            });
            t.start();
            //region 通知欄
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MainActivity.this);
            Intent intent2 = new Intent(context, MainActivity.class);
            // 建立大圖示需要的Bitmap物件
            Bitmap largeIcon = BitmapFactory.decodeResource(
                    getResources(), R.drawable.ic_grade_white);
            //PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            mBuilder.setContentTitle("睡睡RSS")//設置通知欄標題
                    .setContentText(title + "已更新") //設置通知欄顯示內容
                    .setContentIntent(getDefalutIntent(Notification.FLAG_AUTO_CANCEL)) //設置通知欄點擊意圖
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.yee))
                             // 	.setNumber(number) //設置通知集合的數量
                    .setTicker(title + "已更新") //通知首次出現在通知欄，帶上升動畫效果的
                    .setWhen(System.currentTimeMillis())//通知產生的時間，會在通知信息里顯示，一般是系統獲取到的時間
                    .setPriority(Notification.PRIORITY_DEFAULT) //設置該通知優先順序
                    .setAutoCancel(true)//設置這個標誌當用戶單擊面板就可以讓通知將自動取消
                    .setVibrate(new long[] {0,300,500, 700})
                    .setOngoing(false)//ture，設置他為一個正在進行的通知。他們通常是用來表示一個後台任務,用戶積极參与(如播放音樂)或以某種方式正在等待,因此佔用設備(如一個文件下載,同步操作,主動網路連接)
                    .setDefaults(R.raw.nudge)//向通知添加聲音、閃燈和振動效果的最簡單、最一致的方式是使用當前的用戶默認設置，使用defaults屬性，可以組合
                            //Notification.DEFAULT_ALL  Notification.DEFAULT_SOUND 添加聲音 // requires VIBRATE permission
                    .setSmallIcon(R.drawable.ic_grade_white);//設置通知小ICON
            Notification notification = mBuilder.build();
            notification.flags = Notification.FLAG_AUTO_CANCEL;
            mNotificationManager.notify(0, mBuilder.build());
            //endregion
        }
    }
    public PendingIntent getDefalutIntent(int flags){
        PendingIntent pendingIntent= PendingIntent.getActivity(this, 1, new Intent(), flags);
        return pendingIntent;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService();//關掉Service
    }
    //region 儲存功能
    @Override
    protected void onPause() {
        super.onPause();
        //subMenu save
        if(feedMenu.size() > 0) {
            try {
                FileOutputStream fos = openFileOutput("SubMenu.txt", MODE_PRIVATE); //(儲存的名稱,可覆蓋)
                OutputStreamWriter osw = new OutputStreamWriter(fos);
                BufferedWriter bw = new BufferedWriter(osw);
                String[] str = feedMenu.getAllSubMenuTitle();
                int size = feedMenu.getSubSize();
                for (int i = 0; i < size; i++) {
                    bw.write(Integer.toString(feedMenu.getSubGroup(i)));
                    bw.newLine();
                    bw.write(str[i]);
                    bw.newLine();
                }
                bw.flush();
                bw.close();
            } catch (IOException e) {

            }
            //feed save
            try {
                FileOutputStream fos = openFileOutput("Feed.txt", MODE_PRIVATE);
                OutputStreamWriter osw = new OutputStreamWriter(fos);
                BufferedWriter bw = new BufferedWriter(osw);
                int size = feedMenu.size();
                for (int i = 0; i < size; i++) {
                    bw.write(Integer.toString(feedMenu.getGroup(i)));
                    bw.newLine();
                    bw.write(feedMenu.getFeed(i).getUrl());
                    bw.newLine();
                }
                bw.flush();
                bw.close();
            } catch (IOException e) {

            }
        }
    }
    private void load()
    {
        try {
            FileInputStream fis=openFileInput("SubMenu.txt");
            InputStreamReader isr=new InputStreamReader(fis);
            BufferedReader br=new BufferedReader(isr);
            String Gtemp="";
            while((Gtemp = br.readLine()) !=null){
                String Stemp = br.readLine();
                if( !Stemp.equals("default")) {
                    feedMenu.addSubMenu(Stemp, Integer.parseInt(Gtemp));
                }
            }
            br.close( );
        }
        catch (IOException e) {

        }
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    FileInputStream fis=openFileInput("Feed.txt");
                    InputStreamReader isr=new InputStreamReader(fis);
                    BufferedReader br=new BufferedReader(isr);
                    String GroupTemp="";
                    while((GroupTemp = br.readLine()) !=null){
                        String UrlTemp = br.readLine();
                        RSSFeed FeedTemp = getFeed(UrlTemp);
                        feedMenu.addMenuItem(FeedTemp, Integer.parseInt(GroupTemp));
                    }
                    br.close( );
                }
                catch (IOException e) {

                }
            }
        });
        t.start();
        try {
            t.join();   //等待所有資料讀取完畢
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    //endregion

    //region 處理Rss訊息
    //因為component不能再thread裡更改，所以需要Handler的幫助
    public Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msgg) {
            switch(msgg.what){
                case 1:
                    UpdateDisplay();//更新listView
                    break;
            }
            super.handleMessage(msgg);
        }
    };
    public RSSFeed getFeed(String urlToRssFeed) //取得Feed(書上範例)
    {
        try
        {
            // setup the url
            URL url = new URL(urlToRssFeed);
            // create the factory
            SAXParserFactory factory = SAXParserFactory.newInstance();
            // create a parser
            SAXParser parser = factory.newSAXParser();

            // create the reader (scanner)
            XMLReader xmlreader = parser.getXMLReader();
            // instantiate our handler
            RSSHandler theRssHandler = new RSSHandler(urlToRssFeed);
            // assign our handler
            xmlreader.setContentHandler(theRssHandler);
            // get our data via the url class
            xmlreader.parse(new InputSource(url.openStream()));
            //Log.d(urlToRssFeed, is.toString());
            // perform the synchronous parse
            // get the results - should be a fully populated RSSFeed instance, or null on error
            return theRssHandler.getFeed();
        }
        catch (Exception ee)
        {
            Log.e("ERROR", "ER");
            ee.printStackTrace();
            // if we have a problem, simply return null
            return null;
        }
    }
    public void UpdateDisplay()
    {
        // 展示channel的标题和发布时间
        TextView feedtitle = (TextView) findViewById(R.id.feedtitle);
        TextView feedpubdate = (TextView) findViewById(R.id.feedpubdate);
        // 展示RSSFeed的RSSItems
        ListView itemlist = (ListView) findViewById(R.id.listView);
        addItem.setIcon(R.drawable.ic_grade_black);
        if (feedNew == null)
        {
            feedtitle.setText("No RSS Feed Available");
            return;
        }
        if( feedMenu.Isfind(feed) )
            addItem.setIcon(R.drawable.ic_grade_white);
        feedtitle.setText(feed.getTitle());
        feedpubdate.setText(feed.getPubDate());

        /*
         * 为了使得RSSItems能够呈现在ListView中，我们通过带参数的构造函数创建一个ArrayAdapter实例，
         * ArrayAdapter是用来管理RSSItem类型的items的
         * RSSItem列表是由Android平台内置的资源android.R.layout.simple_list_item_1来进行布局的
         * 把调用RSSFeed的getAllItems()方法得到的所有RSSItem作为参数传递给ArrayAdapter的构造函数
         */
        ArrayAdapter<RSSItem> adapter = new ArrayAdapter<RSSItem>(this,android.R.layout.simple_list_item_1,feed.getItemList());

        itemlist.setAdapter(adapter);
        // 设置监听器对item的选择进行响应

        //點擊跳到ShowDescription畫面，顯示詳細資料
        itemlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("tag", "item clicked! [" + feed.getItem(position).getTitle() + "]");

                Intent itemintent = new Intent(MainActivity.this, ShowDescription.class);

                Bundle b = new Bundle();
                b.putString("title", feed.getItem(position).getTitle());
                b.putString("description", feed.getItem(position).getDescription());
                b.putString("link", feed.getItem(position).getLink());
                b.putString("pubdate", feed.getItem(position).getPubDate());
                itemintent.putExtra("android.intent.extra.INTENT", b);

                startActivityForResult(itemintent, 0);
            }
        });
        // 默认选择第一个item
        itemlist.setSelection(0);
    }
    //endregion

    //region thread跟新 因為不用這個了 所以懶得打註解
    public void updateAllFeed()
    {
        new Thread() {
            @Override
            public void run()
            {
                while(true) {
                    for (int i = 0; i < feedMenu.size(); i++) {
                        feedNew = getFeed(feedMenu.getFeed(i).getUrl());
                        if(feedNew != null) {
                            if (!feedNew.getPubDate().equals(feedMenu.getFeed(i).getPubDate())) {
                                feedMenu.updateFeed(feedNew, i);
                                showToast(feedNew.getTitle() + "更新");
                                soundPool.play(alertId, 1, 1, 0, 1, 1);
                                if (feedNew.getTitle().equals(feed.getTitle())) {
                                    feed = feedNew;
                                    Message msgg = new Message();
                                    msgg.what = 1;
                                    mHandler.sendMessage(msgg);
                                    stopService();
                                    startService();
                                }
                            }
                        }
                    }
                    try {
                        Thread.sleep(1000 * second);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
    //endregion
    //region 啟用&關閉Service
    private void startService()
    {
        Intent intent = new Intent(MainActivity.this, MyService.class);
        Bundle bundle = new Bundle();
        bundle.putInt("size",feedMenu.size());
        bundle.putInt("second",second);
        bundle.putStringArray("urlArray", feedMenu.getAllMenuUrl());
        bundle.putStringArray("pubDateArray", feedMenu.getAllMenuPubDate());
        intent.putExtras(bundle);
        startService(intent);
    }
    private void stopService()
    {
        Intent intent = new Intent(MainActivity.this, MyService.class);
        stopService(intent);
    }
    //endregion
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);//左拉的選單
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);//關閉左拉的選單
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        addItem = menu.findItem(R.id.action_add);//取得星星的meun
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Dialog d = onCreateDialogNumberPicker();    //設定時間的Dialog
            d.show();
            return true;
        }else if (id == R.id.action_add) {               //新增的 Dialog
            if (feed != null) {
                if (!feedMenu.Isfind(feed)) {//沒有重複才能新增
                    Dialog d = onCreateDialogAdd();
                    d.show();
                }
            }
        }else if (id == R.id.action_refresh){           //刷新的 Dialog
            new Thread() {
                @Override
                public void run() {
                    if (feed != null) {
                        feedNew = getFeed(feed.getUrl());
                        if(feedNew != null) {
                            if (!feedNew.getPubDate().equals(feed.getPubDate())) {
                                showToast(feedNew.getTitle()+"更新");
                                soundPool.play(alertId, 1, 1, 0, 1, 1); //聲音
                                feed = feedNew;
                                feedMenu.updateFeed(feed);  //更新左拉menu
                                Message msgg = new Message();
                                msgg.what = 1;
                                mHandler.sendMessage(msgg);
                                stopService();  //重新啟動Service因為要更新資料
                                startService();
                            }
                        }
                    }
                }
            }.start();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if ( id == Menu.FIRST+100001 ) { // deletefeed
            Dialog d = onCreateDialogDeleteItem();
            d.show();
        }
        else if( id == Menu.FIRST+100002 ){ //AddGroup
            Dialog d = onCreateDialogAddGroup();
            d.show();
        }
        else if( id == Menu.FIRST+100003 ){ //DeleteSubItem
            Dialog d = onCreateDialogDeleteSubItem();
            d.show();
        }
        for ( int i = 0 ; i < feedMenu.size() ; i++ ) { //點選左拉的哪個Feed，顯示哪個Feed
            if (id == feedMenu.getmenuItemID(i)) {
                feed = feedMenu.getFeed(i);
                UpdateDisplay();
            }
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);//關閉左拉選單
        return true;
    }

    //region Dialog
    public Dialog onCreateDialogDeleteSubItem() {

        final ArrayList<Integer> mSelectedItems = new ArrayList<Integer>();  // Where we track the selected items
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the dialog title
        builder.setTitle("刪除類別（會刪除類別下所有網頁）")
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setMultiChoiceItems(feedMenu.getAllSubMenuTitle(), null,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                if (isChecked) {
                                    // If the user checked the item, add it to the selected items
                                    mSelectedItems.add(which);
                                } else if (mSelectedItems.contains(which)) {
                                    // Else, if the item is already in the array, remove it
                                    mSelectedItems.remove(Integer.valueOf(which));
                                }
                            }
                        })
                        // Set the action buttons
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK, so save the mSelectedItems results somewhere
                        // or return them to the component that opened the dialog
                        for (int i = mSelectedItems.size() - 1; i > 0; i--) {       //Bubble sort
                            for (int j = 0; j < i; j++) {
                                if (mSelectedItems.get(j) > mSelectedItems.get(j + 1)) {
                                    int temp = mSelectedItems.get(j);
                                    mSelectedItems.set(j,mSelectedItems.get(j+1));
                                    mSelectedItems.set(j + 1, temp);
                                }
                            }
                        }
                        for (int i = mSelectedItems.size() - 1; i >= 0; i--) {
                            feedMenu.deleteSub(mSelectedItems.get(i));
                        }
                        ///因為library有BUG 要這樣做 不要問我為什麼 我也不知道 fuck
                        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                        Menu m = navigationView.getMenu();
                        MenuItem mi = m.getItem(m.size() - 1);
                        mi.setTitle(mi.getTitle());
                        if (!feedMenu.Isfind(feed)) {
                            addItem.setIcon(R.drawable.ic_grade_black);
                        }
                        stopService();
                        startService();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        return builder.create();
    }

    public Dialog onCreateDialogDeleteItem() {

        final ArrayList<Integer> mSelectedItems = new ArrayList<Integer>();  // Where we track the selected items
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the dialog title
        builder.setTitle("刪除feed")
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setMultiChoiceItems(feedMenu.getAllMenuTitle(), null,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                if (isChecked) {
                                    // If the user checked the item, add it to the selected items
                                    mSelectedItems.add(which);
                                } else if (mSelectedItems.contains(which)) {
                                    // Else, if the item is already in the array, remove it
                                    mSelectedItems.remove(Integer.valueOf(which));
                                }
                            }
                        })
                        // Set the action buttons
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK, so save the mSelectedItems results somewhere
                        // or return them to the component that opened the dialog
                        for (int i = mSelectedItems.size() - 1; i > 0; i--) {           //Bubble sort
                            for (int j = 0; j < i; j++) {
                                if (mSelectedItems.get(j) > mSelectedItems.get(j + 1)) {
                                    int temp = mSelectedItems.get(j);
                                    mSelectedItems.set(j,mSelectedItems.get(j+1));
                                    mSelectedItems.set(j+1,temp);
                                }
                            }
                        }
                        for (int i = mSelectedItems.size() - 1; i >= 0; i--) {
                            feedMenu.delete(mSelectedItems.get(i));
                        }
                        ///因為library有BUG 要這樣做 不要問我為什麼 我也不知道 fuck
                        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                        Menu m = navigationView.getMenu();
                        MenuItem mi = m.getItem(m.size() - 1);
                        mi.setTitle(mi.getTitle());
                        if (!feedMenu.Isfind(feed)) {
                            addItem.setIcon(R.drawable.ic_grade_black);
                        }
                        stopService();
                        startService();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        return builder.create();
    }

    public Dialog onCreateDialogAdd() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("選擇類別")
           .setItems(feedMenu.getAllSubMenuTitle(), new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int which) {
                   // The 'which' argument contains the index position
                   // of the selected item
                   feedMenu.addMenuItem(feed, which);
                   addItem.setIcon(R.drawable.ic_grade_white);
                   stopService();
                   startService();
               }
           });
    return builder.create();
}

    public Dialog onCreateDialogAddGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();
        builder.setTitle("新增分類");
        final View inputView = inflater.inflate(R.layout.edit_layout, null);
        final EditText AddGroup = (EditText)inputView.findViewById(R.id.addSubMenu);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inputView)
                // Add action buttons
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // sign in the user ...
                        String s = AddGroup.getText().toString();
                        feedMenu.addSubMenu(AddGroup.getEditableText().toString());
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        return builder.create();
    }
    public Dialog onCreateDialogNumberPicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();
        builder.setTitle("設定刷新時間(秒)");
        final View inputView = inflater.inflate(R.layout.number_picker, null);
        final NumberPicker picker = (NumberPicker)inputView.findViewById(R.id.numberPicker1);
        picker.setMaxValue(60);
        picker.setMinValue(10);
        picker.setValue(second);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inputView)
                // Add action buttons
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // sign in the user ...
                        second = picker.getValue();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        return builder.create();
    }
    //endregion
    public void showToast(final String toast)   //把它寫成function  有些時候可以避免Bug 例如在thread裡使用他
    {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
