// MusicPlayer.aidl
package com.example.minuf;

// Declare any non-default types here with import statements

interface MusicPlayer {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */

     /* These are the methods that are implemented to achieve each action of the MediaPlayer */

    void playSong(int id);
    int pauseSong();
    int resumeSong(int duration);
    void stopSong();

}