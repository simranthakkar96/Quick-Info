package com.example.recyclerview;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {
    private Context mContext;
    private Cursor mCursor;

    String title ;
    String description;
    String category;
    String date;
    String img;
    String path;
    Double latitude;
    Double longitude;
    long id;

    public List<String> names;
    public List<String> passnames = new ArrayList<>();

    public NoteAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, OnMapReadyCallback {
        public TextView titleText, descriptionText, categoryText,dateText;
        public ImageView image;
        MapView map;
        GoogleMap mapCurrent;

        public ViewHolder(View itemView) {
            super(itemView);


            itemView.setOnClickListener(this);
            titleText = itemView.findViewById(R.id.textview_title_item);
            descriptionText = itemView.findViewById(R.id.textview_description_item);
            categoryText = itemView.findViewById(R.id.textview_category_item);
            dateText = itemView.findViewById(R.id.textview_date_item);
            image = itemView.findViewById(R.id.imgView);

            map = itemView.findViewById(R.id.listMap);
            if(map!=null){
                map.onCreate(null);
                map.onResume();
                map.getMapAsync(this);
            }
        }

        @Override
        public void onClick(View v) {

            String uri = "https://www.google.com/maps/?q=" + latitude+ "," +longitude ;


            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT,  " ID: " + id +"\n\nTITLE: \n" +  title + "\n\nDESCRIPTION: \n" + description + "\n\nCATEGORY: \n" + category+ "\n\nLOCATION: \n" + uri);
            intent.setType("text/plain");
            mContext.startActivity(Intent.createChooser(intent, "Send To"));

        }



        @Override
        public void onMapReady(GoogleMap googleMap) {
            MapsInitializer.initialize(mContext);
            mapCurrent = googleMap;
            LatLng latLng = new LatLng(latitude,longitude);
            googleMap.addMarker(new MarkerOptions().position(latLng).title("Note Here"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,12));

        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (!mCursor.moveToPosition(position)) {
            return;
        }

        GoogleMap thisMap = holder.mapCurrent;

        title = mCursor.getString(mCursor.getColumnIndex(NoteInfo.NoteColumns.COLUMN_TITLE));
        description = mCursor.getString(mCursor.getColumnIndex(NoteInfo.NoteColumns.COLUMN_DESCRIPTION));
        category = mCursor.getString(mCursor.getColumnIndex(NoteInfo.NoteColumns.COLUMN_CATEGORY));
        date = mCursor.getString(mCursor.getColumnIndex(NoteInfo.NoteColumns.COLUMN_DATE));
        img = mCursor.getString(mCursor.getColumnIndex(NoteInfo.NoteColumns.COLUMN_IMAGE));
        id = mCursor.getLong(mCursor.getColumnIndex(NoteInfo.NoteColumns._ID));
        latitude = mCursor.getDouble(mCursor.getColumnIndex(NoteInfo.NoteColumns.COLUMN_LATITUDE));
        longitude = mCursor.getDouble(mCursor.getColumnIndex(NoteInfo.NoteColumns.COLUMN_LONGITUDE));
        path = mCursor.getString(mCursor.getColumnIndex(NoteInfo.NoteColumns.COLUMN_PATH));


        holder.titleText.setText(title);
        holder.descriptionText.setText(description);
        holder.categoryText.setText(category);
        holder.dateText.setText(date);
        holder.itemView.setTag(id);
        decodeBase64AndSetImage(img,holder.image);
        passnames.add(title);

        if(thisMap!=null){

            LatLng latLng = new LatLng(latitude,longitude);
            Log.d("","WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW" + latitude.toString() + longitude.toString());
            thisMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        }



    }
    @Override
    public void onViewRecycled(ViewHolder holder){
        if(holder.mapCurrent!=null){
            holder.mapCurrent.clear();
            holder.mapCurrent.setMapType(GoogleMap.MAP_TYPE_NONE);
        }
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        if (mCursor != null) {
            mCursor.close();
        }

        mCursor = newCursor;

        if (newCursor != null) {
            notifyDataSetChanged();
        }
    }

    public void decodeBase64AndSetImage(String completeImageData, ImageView imageView) {
        String imageDataBytes = completeImageData.substring(completeImageData.indexOf(",")+1);

        InputStream stream = new ByteArrayInputStream(Base64.decode(imageDataBytes.getBytes(), Base64.DEFAULT));

        Bitmap bitmap = BitmapFactory.decodeStream(stream);

        imageView.setImageBitmap(bitmap);
    }

    public void onItemClick(){
        Intent intent = new Intent("custom-message");
        intent.putExtra("title",title);
        intent.putExtra("description",description);
        intent.putExtra("category",category);
        intent.putExtra("date",date);
        intent.putExtra("image",img);
        intent.putExtra("id",id);
        intent.putExtra("latitude",latitude);
        intent.putExtra("longitude",longitude);
        intent.putExtra("path",path);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    public void updateList(List<String> newList){
        names = new ArrayList<>();
        names.addAll(newList);
        Log.d("WWWWWWWWWWWWWWWWWWWW",names.toString());
    }

}