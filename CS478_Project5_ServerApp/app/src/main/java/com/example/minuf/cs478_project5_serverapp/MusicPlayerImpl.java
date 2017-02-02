package com.example.minuf.cs478_project5_serverapp;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.RemoteException;

import com.example.minuf.*;

import java.util.ArrayList;
import java.util.List;

public class MusicPlayerImpl extends Service {

    MediaPlayer mediaPlayer = new MediaPlayer(); // creating an instance of mediaplayer
    final int[] audioFiles = {R.raw.english,R.raw.music1,R.raw.music2,R.raw.tamil}; // the array of audio files to be played
    int currentSong;


    //Implementation of the interface MusicPlayer defined in the aidl file
    private final MusicPlayer.Stub mBinder = new MusicPlayer.Stub(){


        // The below method implements the Play action to be performed when the user clicks 'Play' button in the client app
        public void playSong(int songId) throws RemoteException{
            currentSong = songId;
            if(mediaPlayer.isPlaying())
            {
                mediaPlayer.stop(); // Stop the player if it is currently playing any song
            }
            // Play the current song after stopping the previous song (if it was same or different than current one)
            if( songId <= audioFiles.length && songId > 0) {
                mediaPlayer = MediaPlayer.create(getApplicationContext(), audioFiles[songId - 1]); // Creates the MediaPlayer with the chosen song
                mediaPlayer.start(); //starts the media player with the chosen song
            }

        }

        // Defines the action to be performed on click of the 'Pause' button in the client app
        public int pauseSong() throws RemoteException{
            if(mediaPlayer.isPlaying()) // Pause the song only if the song is playing currently
            {
                mediaPlayer.pause(); // Pause the mediaplayer
                return mediaPlayer.getCurrentPosition(); //return the location at which pause was clicked (Time)
            }
            return 0;
        }

        // Defines the action to be performed on click of the 'Resume' button in the client app
        public int resumeSong(int duration) throws RemoteException{
            if(!mediaPlayer.isPlaying()) // Resume only if the media player is currently paused
            {
                mediaPlayer.seekTo(duration); // Resume the player from the location where it was paused
                mediaPlayer.start();
                return 1;
            }
            else {
                return 0;
            }
        }

        // Defines the action to be performed on click of the 'Stop' button in the client app
        public void stopSong() throws RemoteException{

            if(mediaPlayer.isPlaying()) // Stop the player if it is currently playing the song
            {
                mediaPlayer.stop(); // Stop the mediaplayer
            }

        }

    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }
}
