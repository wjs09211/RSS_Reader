package hw.lab2.s1021527;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ShowDescription extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_description);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String theStory = null;
        Intent startingIntent = getIntent();

        if (startingIntent != null)
        {
            Bundle b = startingIntent.getBundleExtra("android.intent.extra.INTENT");
            if (b == null)
            {
                theStory = "bad bundle?";
            }
            else
            {
                theStory = b.getString("title") + "\n\n"
                        + b.getString("pubdate") + "\n\n"
                        + b.getString("description")
                        + "\n\nMore information:\n" + b.getString("link");
            }
        }
        else
        {
            theStory = "Information Not Found.";
        }

        TextView db= (TextView) findViewById(R.id.storyBox);
        db.setText(theStory);

        Button backbutton = (Button) findViewById(R.id.btn_back);
        backbutton.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View v)
            {
                finish(); // 导致控制权返回给主调Activity，也就是RSSReader
            }
        });
    }

}
