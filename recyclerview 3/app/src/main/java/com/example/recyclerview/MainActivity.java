package com.example.recyclerview;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import android.view.Menu;
import android.view.MenuItem;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.maps.OnMapReadyCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Permission;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static android.graphics.Color.GREEN;
import static android.graphics.Color.RED;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener,OnMapReadyCallback,
        AdapterView.OnItemSelectedListener{
    public SQLiteDatabase mDatabase;
    public NoteAdapter mAdapter;
    public EditText mEditTextName;
    public EditText mEditTextDescription;
    public TextView textDate;
    public LinearLayout collapse;
    public FloatingActionButton fab,fabmic,fabcall,fabrec,fabcat;
    public Button buttonAdd,buttonClear;
    public ImageView image;
    public long id;
    public boolean expand = false;
    public boolean expand1 = false;
    public boolean edit = false;
    public Double latitude;
    public Double longitude;
    public String imageString;
    public boolean sortIcon = false;
    public boolean start = false;
    int mCurrentPosition = 0;

    CardView cardview;
    SeekBar mSeekBar;
    Handler mHandler;
    ImageButton startRecord;
    String pathSave ="";
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;
    DBHelper dbHelper;
    SupportMapFragment supportMapFragment;
    final int REQUEST_PERMISSION_CODE = 1000;
    boolean record = false;

    Location currentLocation;
    Spinner spinner;
    FusedLocationProviderClient fusedLocationProviderClient;
    public static final int REQUEST_CODE = 101;

    private List<String> names = new ArrayList<>();

    Animation sys_anim1;
    Animation sys_anim2;
    Animation sys_anim3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!CheckPermissionFromDevice())
        {
            requestPermissions();
        }


        collapse = findViewById(R.id.collapse);
        dbHelper = new DBHelper(this);
        mDatabase = dbHelper.getWritableDatabase();
        textDate = findViewById(R.id.text_date);
        fab = findViewById(R.id.fab);
        image = findViewById(R.id.imgView);
        fabmic = findViewById(R.id.fabMic);
        fabcall = findViewById(R.id.fabCall);
        fabcat = findViewById(R.id.fabCat);
        fabrec = findViewById(R.id.fabRec);
        spinner = findViewById(R.id.spinner);
        startRecord = findViewById(R.id.startRecord);
        mSeekBar = findViewById(R.id.seekBar2);
        buttonClear = findViewById(R.id.button_clear);
        cardview = findViewById(R.id.cardview);
        
        mediaPlayer = new MediaPlayer();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        fetchLocation();


        spinner.setOnItemSelectedListener(this);

        // Loading spinner data from database
        loadSpinnerData();



        fabrec.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                   recordClicked(); }
        });
        startRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!start)
                {
                    mediaPlayer = new MediaPlayer();
                    startRecord.setImageResource(R.drawable.stop2);
                    startRecord.setBackgroundResource(R.drawable.radius);
                    try {
                        mediaPlayer.setDataSource(pathSave);
                        mediaPlayer.prepare();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    mediaPlayer.start();
                    mSeekBar.setMax(mediaPlayer.getDuration()/10);
                    Toast.makeText(MainActivity.this, "Playing....", Toast.LENGTH_SHORT).show();
                    start = true;
                }
                else if(start)
                {
                    startRecord.setBackgroundResource(R.drawable.playback);
                    startRecord.setImageResource(R.drawable.play);
                    if(mediaPlayer!=null)
                    {
                        mediaPlayer.stop();
                        mSeekBar.setProgress(0);
                        setupMediaRecorder();
                    }
                    start = false;
                }


            }
        });


        mHandler = new Handler();
        MainActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if(mediaPlayer != null){
                    mCurrentPosition = mediaPlayer.getCurrentPosition() / 10;
                    mSeekBar.setProgress(mCurrentPosition);
                }
                mHandler.postDelayed(this, 10);
            }
        });

        sys_anim1 = AnimationUtils.loadAnimation(MainActivity.this,R.anim.rotate);
        sys_anim2 = AnimationUtils.loadAnimation(MainActivity.this,R.anim.fadein);
        sys_anim3 = AnimationUtils.loadAnimation(MainActivity.this,R.anim.fadeout);

        mEditTextName = findViewById(R.id.edittext_name);
        mEditTextDescription = findViewById(R.id.edittext_description);
        textDate = findViewById(R.id.text_date);
        buttonAdd = findViewById(R.id.button_add);

        collapse();

        final Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-YYYY");
        final String strDate = dateFormat.format(date);
        textDate.setText(strDate);


        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new NoteAdapter(this, getAllItems());
        recyclerView.setAdapter(mAdapter);


        names = mAdapter.passnames;


        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("custom-message"));



        new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final long ID = (long) viewHolder.itemView.getTag();
                if(direction == 4) {
                    Toast.makeText(MainActivity.this, "" + String.valueOf(direction), Toast.LENGTH_SHORT).show();
                    final CharSequence[] options = {"DELETE", "CANCEL"};

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Are You Sure You Want To Delete This Note?");

                    builder.setItems(options, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int item) {

                            if (options[item].equals("DELETE")) {
                                removeItem(ID);

                            } else if (options[item].equals("CANCEL")) {
                                dialog.dismiss();
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                    builder.show();
                }
                else if(direction == 8){
                    mAdapter.onItemClick();
                }

            }
        }).attachToRecyclerView(recyclerView);



        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem();
                loadSpinnerData();
            }
        });

        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditTextName.setText("");
                mEditTextDescription.setText("");
                image.setImageResource(android.R.drawable.ic_menu_camera);
            }
        });


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabClicked();
            }
        });

        fabmic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!edit) {
                    mEditTextName.setEnabled(true);
                    mEditTextDescription.setEnabled(true);
                    fabmic.setBackgroundTintList(ColorStateList.valueOf(RED));
                    edit = true;
                }
                else if(edit) {
                    mEditTextName.setEnabled(false);
                    mEditTextDescription.setEnabled(false);
                    fabmic.setBackgroundTintList(ColorStateList.valueOf(GREEN));
                    edit = false;
                }
            }
        });

        fabcall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Choose your profile picture");

                builder.setItems(options, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int item) {

                        if (options[item].equals("Take Photo")) {
                            Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(takePicture, 0);

                        } else if (options[item].equals("Choose from Gallery")) {
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(pickPhoto, 1);

                        } else if (options[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            }
        });

        fabcat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RecyclerView recyclerView = findViewById(R.id.recyclerview);
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                mAdapter = new NoteAdapter(MainActivity.this, getSortCatItems());
                recyclerView.setAdapter(mAdapter);
                fabClicked();
            }
        });

    }


    private void setupMediaRecorder() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(pathSave) ;
    }

    public void recordClicked() {
        if(CheckPermissionFromDevice()) {
            if (!record) {
                pathSave = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + UUID.randomUUID().toString() + "_audio_record.3gp";
                setupMediaRecorder();
                try {

                    mediaRecorder.prepare();
                    mediaRecorder.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                startRecord.setEnabled(false);

                Toast.makeText(MainActivity.this, "Recording....", Toast.LENGTH_SHORT).show();

                record = true;
            } else if (record) {
                mediaRecorder.stop();
                startRecord.setEnabled(true);
                record = false;
            }
        }
        else
            {
                requestPermissions();
            }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case REQUEST_PERMISSION_CODE:
            {
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this, "Granted", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "Denied", Toast.LENGTH_SHORT).show();
            }
            break;
        }
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        },REQUEST_PERMISSION_CODE);
    }

    public boolean CheckPermissionFromDevice()
    {
        int write_external_storage_result = ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.RECORD_AUDIO);
        return write_external_storage_result == PackageManager.PERMISSION_GRANTED &&  record_audio_result == PackageManager.PERMISSION_GRANTED;
    }

    private void loadSpinnerData() {
        // database handler

        // Spinner Drop down elements
        List<String> lables = dbHelper.getAllLabels();

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, lables);

        // Drop down layout style - list view with radio button
        dataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
    }


    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            fabClicked();
            expand1 = true;
            mEditTextName.setText(intent.getStringExtra("title"));
            mEditTextDescription.setText(intent.getStringExtra("description"));
            textDate.setText(intent.getStringExtra("date"));
            mAdapter.decodeBase64AndSetImage(intent.getStringExtra("image"),image);
            latitude =  intent.getDoubleExtra("latitude",currentLocation.getLatitude());
            longitude =  intent.getDoubleExtra("longitude",currentLocation.getLongitude());
            id = intent.getLongExtra("id",0);
            pathSave = intent.getStringExtra("path");
            Toast.makeText(MainActivity.this,"Well Done" ,Toast.LENGTH_SHORT).show();

            mEditTextName.setEnabled(false);
            mEditTextDescription.setEnabled(false);
        }
    };



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        image.setImageBitmap(selectedImage);
                    }

                    break;
                case 1:
                    if(resultCode == RESULT_OK){
                        Uri selectedImage = data.getData();
                        image.setImageURI(selectedImage);
                    }
                    break;
            }
        }
    }

    private void addItem() {

        if (mEditTextName.getText().toString().trim().length() == 0 ) {
            return;
        }

        String name = mEditTextName.getText().toString();
        String description = mEditTextDescription.getText().toString();
        String date = textDate.getText().toString();


        Bitmap bitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // In case you want to compress your image, here it's at 40%
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        imageString = Base64.encodeToString(byteArray, Base64.DEFAULT);

        ContentValues cv = new ContentValues();

        cv.put(NoteInfo.NoteColumns.COLUMN_TITLE, name);
        cv.put(NoteInfo.NoteColumns.COLUMN_DESCRIPTION, description);
        cv.put(NoteInfo.NoteColumns.COLUMN_CATEGORY,spinner.getSelectedItem().toString());
        cv.put(NoteInfo.NoteColumns.COLUMN_DATE,date);
        cv.put(NoteInfo.NoteColumns.COLUMN_IMAGE,imageString);
        cv.put(NoteInfo.NoteColumns.COLUMN_LATITUDE,latitude);
        cv.put(NoteInfo.NoteColumns.COLUMN_LONGITUDE,longitude);
        cv.put(NoteInfo.NoteColumns.COLUMN_PATH,pathSave);

        mDatabase.insert(NoteInfo.NoteColumns.TABLE_NAME, null, cv);
        mAdapter.swapCursor(getAllItems());

        mEditTextName.getText().clear();
        mEditTextDescription.getText().clear();
        image.setImageResource(android.R.drawable.ic_menu_camera);

        if(expand1){
            mDatabase.delete(NoteInfo.NoteColumns.TABLE_NAME, NoteInfo.NoteColumns._ID + "=" + id,null);
            mAdapter.swapCursor(getAllItems());
            mAdapter.notifyDataSetChanged();
        }

    }



    private  void removeItem(long id){
        mDatabase.delete(NoteInfo.NoteColumns.TABLE_NAME, NoteInfo.NoteColumns._ID + "=" + id,null);
        mAdapter.swapCursor(getAllItems());
    }

    private Cursor getAllItems() {
        return mDatabase.query(
                NoteInfo.NoteColumns.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private Cursor getSearchItems(String txt) {
        return mDatabase.query(
                NoteInfo.NoteColumns.TABLE_NAME,
                null,
                "title = ?",
                new String[] { txt },
                null,
                null,
                null
        );
    }

    private Cursor getSortNameItems() {
        return mDatabase.query(
                NoteInfo.NoteColumns.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                "title"
        );
    }

    private Cursor getSortDateItems() {
        return mDatabase.query(
                NoteInfo.NoteColumns.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                "date"
        );
    }


    private Cursor getSortCatItems() {
        return mDatabase.query(
                NoteInfo.NoteColumns.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                "category"
        );
    }


    private void collapse(){
        collapse.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                0,  // fromYDelta
                -2000);                // toYDelta
        animate.setDuration(0);
        animate.setFillAfter(true);
        collapse.startAnimation(animate);

        buttonAdd.animate().translationX(-450).setDuration(0);
        buttonClear.animate().translationX(450).setDuration(0);

        mEditTextName.setVisibility(View.INVISIBLE);
        mEditTextDescription.setVisibility(View.INVISIBLE);
        fabmic.setBackgroundColor(GREEN);


    }
    public void fabClicked(){

        sys_anim1.setFillAfter(true);
        if(!expand) {
            fab.startAnimation(sys_anim1);
            TranslateAnimation animate = new TranslateAnimation(
                    0,                 // fromXDelta
                    0,                 // toXDelta
                    -2000,                 // fromYDelta
                    0); // toYDelta
            animate.setDuration(500);
            animate.setFillAfter(true);
            collapse.startAnimation(animate);

            fabmic.animate().translationY(-275);
            fabmic.animate().translationX(25);

            fabcall.animate().translationY(-250);
            fabcall.animate().translationX(-135);

            fabcat.animate().translationX(-250);
            fabcat.animate().translationY(-135);

            fabrec.animate().translationX(-275);
            fabrec.animate().translationY(25);

            buttonAdd.animate().translationX(-50).setDuration(300);
            buttonClear.animate().translationX(60).setDuration(300);

            mEditTextName.setVisibility(View.VISIBLE);
            mEditTextDescription.setVisibility(View.VISIBLE);

            expand = true;
        }
        else if(expand) {
            sys_anim1.setFillAfter(false);
            fab.startAnimation(sys_anim1);
            collapse.setVisibility(View.VISIBLE);
            TranslateAnimation animate = new TranslateAnimation(
                    0,                 // fromXDelta
                    0,                 // toXDelta
                    0,  // fromYDelta
                    -2000);                // toYDelta
            animate.setDuration(500);
            animate.setFillAfter(true);
            collapse.startAnimation(animate);


            fabmic.animate().translationY(0);
            fabmic.animate().translationX(0);

            fabcall.animate().translationY(0);
            fabcall.animate().translationX(0);

            fabcat.animate().translationX(0);
            fabcat.animate().translationY(0);

            fabrec.animate().translationX(0);
            fabrec.animate().translationY(0);

            buttonAdd.animate().translationX(-450).setDuration(300);
            buttonClear.animate().translationX(450).setDuration(300);

            mEditTextName.setVisibility(View.INVISIBLE);
            mEditTextDescription.setVisibility(View.INVISIBLE);



            expand = false;




        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        final MenuItem sortitem = menu.findItem(R.id.action_sort);
        final MenuItem catitem = menu.findItem(R.id.action_category);
        SearchView searchView = (SearchView) menuItem.getActionView();

        catitem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {


                final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                final EditText input = new EditText(MainActivity.this);
                input.setSingleLine();
                input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

                alert.setTitle("Add category");
                alert.setView(input);
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString().trim();
                        dbHelper.insertLabel(value);
                        loadSpinnerData();

                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });
                alert.show();


                return true;
            }
        });

        searchView.setOnQueryTextListener(this);
        sortitem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(!sortIcon) {
                    Toast.makeText(MainActivity.this, "Sorted By TITLE", Toast.LENGTH_SHORT).show();


                    RecyclerView recyclerView = findViewById(R.id.recyclerview);
                    recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                    mAdapter = new NoteAdapter(MainActivity.this, getSortNameItems());
                    recyclerView.setAdapter(mAdapter);
                    sortitem.setIcon(android.R.drawable.ic_menu_my_calendar);
                    sortIcon = true;
                }
                else if(sortIcon){

                    Toast.makeText(MainActivity.this, "Sorted By DATE", Toast.LENGTH_SHORT).show();


                    RecyclerView recyclerView = findViewById(R.id.recyclerview);
                    recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                    mAdapter = new NoteAdapter(MainActivity.this, getSortDateItems());
                    recyclerView.setAdapter(mAdapter);
                    sortitem.setIcon(android.R.drawable.ic_menu_sort_alphabetically);

                    sortIcon = false;
                }
                return  true;
            }
        });




        return true;
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new NoteAdapter(this, getSearchItems(query));
        recyclerView.setAdapter(mAdapter);


        return true;
    }



    @Override
    public boolean onQueryTextChange(String newText) {

            RecyclerView recyclerView = findViewById(R.id.recyclerview);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            mAdapter = new NoteAdapter(this, getAllItems());
            recyclerView.setAdapter(mAdapter);

        return false;

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng latLng = new LatLng(latitude,longitude);
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("I AM HERE!");
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,12));
        googleMap.addMarker(markerOptions);
    }

    private void fetchLocation(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);

        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location!=null){
                    Log.d("","SUCCESSSSSSSSSSS");
                    currentLocation = location;
                    latitude = currentLocation.getLatitude();
                    longitude = currentLocation.getLongitude();
                    Toast.makeText(getApplicationContext(), currentLocation.getLatitude() + " " + currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                    supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.myMap);
                    assert supportMapFragment != null;
                    supportMapFragment.getMapAsync(MainActivity.this);
                }
            }
        });

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        // On selecting a spinner item
        String label = adapterView.getItemAtPosition(i).toString();

        // Showing selected spinner item
        Toast.makeText(adapterView.getContext(), "You selected: " + label,
                Toast.LENGTH_LONG).show();

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}







