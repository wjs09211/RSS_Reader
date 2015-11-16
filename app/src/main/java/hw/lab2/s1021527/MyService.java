package hw.lab2.s1021527;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class MyService extends Service {
    private Handler handler = new Handler();
    private String[] url;
    private String[] pubDate;
    private int size;
    private RSSFeed feed;
    private SoundPool soundPool;
    private int alertId;
    private int second = 30;
    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        //這裡實作你想做的工作
        Bundle bundle = intent.getExtras();
        size = bundle.getInt("size");
        second = bundle.getInt("second");
        url = new String[size];
        url = bundle.getStringArray("urlArray");
        pubDate = new String[size];
        pubDate = bundle.getStringArray("pubDateArray");
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 5);
        alertId = soundPool.load(this, R.raw.nudge, 1);
        handler.postDelayed(showTime, 1000*10);
        return Service.START_STICKY;
    }
    @Override
    public void onDestroy() {
        handler.removeCallbacks(showTime);
        super.onDestroy();
    }
    private Runnable showTime = new Runnable() {
        public void run() {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < size; i++) {
                        feed = getFeed(url[i]);
                        if(feed != null)
                        {
                            if (!(feed.getPubDate().equals(pubDate[i]))) {
                                soundPool.play(alertId, 1, 1, 0, 1, 1);
                                Intent intent = new Intent("myService");
                                intent.putExtra("title", feed.getTitle());
                                intent.putExtra("url", feed.getUrl());
                                sendBroadcast(intent);
                            }
                        }
                    }
                }
            });
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            handler.postDelayed(this, 1000*second);
        }
    };
    public RSSFeed getFeed(String urlToRssFeed)
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
}
