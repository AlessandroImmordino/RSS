package com.example.rss;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RssFragment extends Fragment{

    private String URL;
    private View view;
    private ListView listView;
    private ArrayList<Row> rows;
    private DownloadFeed df;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)  {


        view = inflater.inflate(R.layout.home_fragment,
                container, false);

        listView = view.findViewById(R.id.lista);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Uri uri = Uri.parse(rows.get(position).getLink());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        rows = new ArrayList<Row>();

        URL = "https://www.repubblica.it/rss/homepage/rss2.0.xml";

        df = new DownloadFeed();
        df.execute(URL);

        setHasOptionsMenu(true);
        return view;
    }



    class DownloadFeed extends AsyncTask<String,Void,String>{

        @SuppressLint("LongLogTag")
        @Override
        protected String doInBackground(String... strings) {
            URL url=null;
            FileOutputStream fos=null;
            InputStream in=null;
            int bytesRead=-1;
            boolean insideItem = false;
            int eventType = 0;

            try {
                url = new URL(strings[0]);

                in=url.openStream();

                XmlPullParserFactory xppf = XmlPullParserFactory.newInstance();

                xppf.setNamespaceAware(false);

                XmlPullParser xpp = xppf.newPullParser();

                xpp.setInput(in,"UTF_8");

                eventType = xpp.getEventType();

                String t = "",l = "",d = "";
                while (eventType != XmlPullParser.END_DOCUMENT){


                    if (eventType == XmlPullParser.START_TAG) {

                        if (xpp.getName().equalsIgnoreCase("item")) {

                            insideItem = true;

                        }
                        else if (xpp.getName().equalsIgnoreCase("title")) {

                            if (insideItem) {
                                t=xpp.nextText();
                            }
                        }

                        else if (xpp.getName().equalsIgnoreCase("link")) {

                            if (insideItem) {
                                l=xpp.nextText();
                            }
                        }

                        else if (xpp.getName().equalsIgnoreCase("pubDate")) {

                            if (insideItem) {
                                d=xpp.nextText().substring(0,25);
                            }

                        }

                        if(!t.equals("") && !d.equals("") && !l.equals("")){

                            rows.add(new Row(t,d,l));
                            t=""; d=""; l="";
                        }


                    }else if (eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item")) {

                        insideItem = false;

                    }

                    eventType = xpp.next();
                }


            } catch (IOException | XmlPullParserException e) {
                Log.d("DOWNLOADFEED.DOINBACKGROUND",e.getMessage());
            }

            return "Downloaded";
        }

        @Override
        protected void onPostExecute(String result){
            CustomAdapter adapter = new CustomAdapter(getActivity(), android.R.layout.simple_selectable_list_item, rows);

            listView.setAdapter(adapter);
            Toast.makeText(getActivity(),"CI SIAMO!\n"+ Calendar.getInstance().getTime().toString().substring(0,19),Toast.LENGTH_LONG).show();
        }



    }

    public class CustomAdapter extends ArrayAdapter<Row>{


        public CustomAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Row> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.array_row, null);
            TextView data = (TextView)convertView.findViewById(R.id.DateOnRow);
            TextView titolo = (TextView)convertView.findViewById(R.id.TitleOnRow);
            Row r = getItem(position);
            data.setText(r.getData());
            titolo.setText(r.getTitolo());
            return convertView;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_refresh:

                df = new DownloadFeed();
                rows = new ArrayList<Row>();
                df.execute(URL);

                break;
        }

        return true;
    }

    public class Row {
        String titolo;
        String data;
        String link;

        public Row(String titolo, String data, String link){
            this.titolo=titolo;
            this.data=data;
            this.link=link;
        }

        public String getTitolo() {
            return titolo;
        }

        public String getData(){
            return data;
        }

        public String getLink(){
            return link;
        }

        public void setTitolo(String titolo) {
            this.titolo = titolo;
        }

        public void setData(String data) {
            this.data = data;
        }

        public void setLink(String link) {
            this.link = link;
        }

    }
}
