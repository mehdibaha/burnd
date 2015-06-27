package com.insa.burnd.view.TutorialActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.insa.burnd.R;
import com.insa.burnd.utils.SPManager;
import com.insa.burnd.view.LoginActivity;
import com.viewpagerindicator.CirclePageIndicator;


public class TutorialActivity extends FragmentActivity {
    private ViewPager viewPager;
    private TutorialFragmentAdapter adapter;
    private final TutorialActivity activity=this;
    private static String TAG = "BURND-TutorialActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        // Création des "pages" et assignation d'un adapter pour écouter les pages.
        viewPager = (ViewPager) findViewById(R.id.tutorial_pager);
        adapter = new TutorialFragmentAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        CirclePageIndicator circleIndicator = (CirclePageIndicator) findViewById(R.id.circles);
        circleIndicator.setViewPager(viewPager);

        Button skipButton = (Button) findViewById(R.id.skip_button);
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(activity, LoginActivity.class));
            }
        });

        ImageButton nextButton = (ImageButton) findViewById(R.id.next_button);
        nextButton.setOnClickListener(new Button.OnClickListener() {
            int pageCount;
            int pageCountMax;

            @Override
            public void onClick(View v) {
                pageCount = viewPager.getCurrentItem();
                pageCountMax = adapter.getCount();

                Log.d(TAG, "pageCount = " + pageCount + ";pageCountMax = " + pageCountMax);
                if (pageCount == pageCountMax - 1) {  // pagecountMax = pagecount(max) - 0 + 1
                    SPManager.save(activity, "false", "FIRST_USER");
                    startActivity(new Intent(activity, LoginActivity.class));
                } else
                    viewPager.setCurrentItem(pageCount + 1);
            }
        });
    }

    // Object personnalisé qui gère le changement de pages de l'activité
    private class TutorialFragmentAdapter extends FragmentPagerAdapter {
        public TutorialFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    //Fragement for Android Tab
                    return new TutorialFragment1();
                case 1:
                    //Fragment for Ios Tab
                    return new TutorialFragment2();
                case 2:
                    return new TutorialFragment3();
                case 3:
                    return new TutorialFragment4();
            }
            return new Fragment();
        }

        @Override
        public int getCount() {
            return 4; //No of Tabs
        }
    }
}