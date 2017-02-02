package com.example.minuf.cs478_project5_clientapp;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


import com.example.minuf.*;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private boolean mIsBound = false;
    protected static final String TAG = "MusicPlayerClient";
    private MusicPlayer mMusicPlayer;
    int currentSong = 0;
    int pauseDuration;
    public ListView requestsList;
    boolean isPlaying = false; // This flag checks if the mediaplayer is currently playing a song or not
    boolean isPaused = false; // This flag checks if the mediaplayer is currently paused or not
    ArrayList<String> requestsInitialList = new ArrayList<String>(); // This arraylist is used to maintain the list of requests

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Getting the controls from the layout file
        final EditText songIdTextView = (EditText) findViewById(R.id.songId);
        Button btnPlayMusic = (Button) findViewById(R.id.btnPlayMusic);
        Button btnPauseMusic = (Button) findViewById(R.id.btnPauseMusic);
        Button btnResumeMusic = (Button) findViewById(R.id.btnResumeMusic);
        Button btnStopMusic = (Button) findViewById(R.id.btnStopMusic);
        requestsList = (ListView) findViewById(R.id.requestsList);

        // On clicking play button, the song should be played from starting
        btnPlayMusic.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    String songIdVal = songIdTextView.getText().toString();
                    if(songIdVal == null || songIdVal.isEmpty())
                    {
                        Toast.makeText(getApplicationContext(), "Enter a Valid Choice (1, 2, 3, 4)", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if(mIsBound) // if the service has been bound, only then play the music
                    {
                        int songId = Integer.parseInt(songIdVal);
                        if(songId > 4 || songId < 0)
                        {
                            Toast.makeText(getApplicationContext(), "Enter a Valid Choice (1, 2, 3, 4)", Toast.LENGTH_LONG).show();
                            return;
                        }
                        else
                        {
                            currentSong = songId;
                            isPlaying = true; // currently the song is being played
                            isPaused = false; // currently song is not paused
                            mMusicPlayer.playSong(Integer.parseInt(songIdVal)); // mMusicPlayer.playSong calls the method defined in the server app which plays the chosen song
                            requestsInitialList.add("Play Song " + currentSong); // Client app keeps track of the requests by adding to this list whenever an action is performed
                            displayRequests(); // Display the list with the requests till the current request
                        }
                    }
                    else
                    {
                        Log.i(TAG,"Service was not bound Successfully");
                    }
                }
                catch (RemoteException e) {
                    Log.e(TAG, e.toString());
                }
            }
        });

        // On clicking the Pause button, the current song should pause
        btnPauseMusic.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {


                try
                {
                    if(mIsBound)
                    {
                        if(!isPaused)
                        {

                            Log.i("Pause","Inside Pause");
                            isPlaying = false;
                            isPaused = true;
                            pauseDuration = mMusicPlayer.pauseSong(); // calls the pauseSong method defined in the server and returns the paused location into pauseDuration
                            requestsInitialList.add("Pause Song " + currentSong); // Client app keeps track of the requests by adding to this list whenever an action is performed
                            displayRequests(); // Display the list with the requests till the current request
                        }

                    }
                    else
                    {
                        Log.i(TAG,"Service was not bound Successfully");
                    }
                }
                catch (RemoteException e)
                {
                    Log.e(TAG, e.toString());
                }
            }
        });

        // On clicking the Resume button, the current song should start from the paused location
        btnResumeMusic.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    if(mIsBound)
                    {
                        if(isPaused)
                        {
                            Log.i("Resume","Inside Resume");
                            int mediaPlayerStatus = mMusicPlayer.resumeSong(pauseDuration); // calls the resumeSong method defined in the server app which starts the current sng from the pauseDuration
                            if(mediaPlayerStatus == 1) // Only if the song is paused, resume should be performed
                            {
                                requestsInitialList.add("Resume Song " + currentSong); // Client app keeps track of the requests by adding to this list whenever an action is performed
                                displayRequests(); // Display the list with the requests till the current request
                                isPlaying = true;
                                isPaused = false;
                            }
                        }


                    }
                    else
                    {
                        Log.i(TAG,"Service was not bound Successfully");
                    }
                }
                catch (RemoteException e)
                {
                    Log.e(TAG, e.toString());
                }
            }
        });

        // On clicking the Stop button, the current song should stop playing
        btnStopMusic.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    if(mIsBound)
                    {
                        Log.i("Stop","Inside Stop");
                        mMusicPlayer.stopSong(); // calls the stopSong method defined in teh server app to stop the current song from playing
                        requestsInitialList.add("Stop Song " + currentSong);
                        displayRequests(); // Display the list with the requests till the current request
                        isPlaying = false;
                        isPaused = false;
                    }
                    else
                    {
                        Log.i(TAG,"Service was not bound Successfully");
                    }
                }
                catch (RemoteException e)
                {
                    Log.e(TAG, e.toString());
                }
            }
        });

    }

    // The below method displays all the requests that have been tracked by the client app so far
    public void displayRequests()
    {
            if(mIsBound)
            {
                // Binding the listview with the array adapter to display the requests
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,R.layout.listviewlayout,requestsInitialList);
                requestsList.setAdapter(adapter); // sets the adapter for the listview to display all the requests in order
            }
    }

    // Binding the service in onResume makes sure that the service does not stop even when the activity is stopped
    protected void onResume()
    {
        super.onResume();
        Log.i("Resume","Inside on Resume");
        if(!mIsBound) // If service is not bound yet
        {
            boolean serviceFlag = false; // this flag indicates if the service has been bound or not
            Intent i = new Intent(MusicPlayer.class.getName()); // This intent is used to bind to a service

            ResolveInfo info = getPackageManager().resolveService(i, Context.BIND_AUTO_CREATE);
            i.setComponent(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));

            serviceFlag = bindService(i, this.mConnection, Context.BIND_AUTO_CREATE); // Establishes the connection with the server app
            if (serviceFlag) {
                Log.i(TAG, "bindService() method has succeeded!");
            } else {
                Log.i(TAG, "bindService() method has failed!");
            }
        }
    }

    @Override
    public void onStart(){
        Log.i("Start","Inside onStart");
        super.onStart();
    }

    @Override
    protected void onPause(){
        Log.i("Pause","Inside onPause");
        super.onPause();
    }

    @Override
    public void onStop(){
        Log.i("Stop","Inside onStop");
        super.onStop();
    }

    // Once the activity is destroyed, the service should also be stopped and unbound. This logic is written in the below onDestroy() method
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.i("Destroy","Destroyed");
        try
        {
            if(mIsBound)
            {
                Log.i("Destroy","Unbinding");
                mMusicPlayer.stopSong(); // stopping the meadia player so that the MediaPlayer service gets stopped
                unbindService(mConnection); // Unbinding the service
            }
        }
        catch (RemoteException e)
        {
            Log.e(TAG, e.toString());
        }
    }


    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            mMusicPlayer = MusicPlayer.Stub.asInterface(service); // sets the mMusicPlayer with the service returned from the server app
            mIsBound = true; // mIsBound = true indicates the service has been bound successfully
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("ServiceDisc","Service Disconnected");
            mMusicPlayer = null; // Once the connection is disabled, sets the value of music player to null
            mIsBound = false; // Assigns the boolean as false to indicate that the service is no longer bound with the client
        }
    };


}
