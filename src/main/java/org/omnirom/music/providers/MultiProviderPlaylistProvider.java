/*
 * Copyright (C) 2014 Fastboot Mobile, LLC.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses>.
 */

package org.omnirom.music.providers;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;

import org.omnirom.music.framework.PluginsLookup;
import org.omnirom.music.model.Album;
import org.omnirom.music.model.Artist;
import org.omnirom.music.model.Genre;
import org.omnirom.music.model.Playlist;
import org.omnirom.music.model.SearchResult;
import org.omnirom.music.model.Song;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by h4o on 17/06/2014.
 */
public class MultiProviderPlaylistProvider extends IMusicProvider.Stub {
    private static final String TAG = "MultiProviderPlaylist";

    private Handler mHandler = new Handler();
    private HashMap<String, Playlist> mPlaylists;
    private HashMap<String, ProviderIdentifier> mSongsProviders;
    private ProviderIdentifier mProviderIdentifier;
    private Context mContext;

    private final List<IProviderCallback> mCallbacks;
    private MultiProviderDatabaseHelper mMultiProviderDatabaseHelper;

    public MultiProviderPlaylistProvider(Context context) {
        mContext = context;
        mPlaylists = new HashMap<>();
        mMultiProviderDatabaseHelper = new MultiProviderDatabaseHelper(mContext, mLocalCallback);
        mCallbacks = new ArrayList<>();
    }

    private IMusicProvider getBinder(ProviderIdentifier id) {
        return PluginsLookup.getDefault().getProvider(id).getBinder();
    }

    @Override
    public int getVersion() throws RemoteException {
        return Constants.API_VERSION;
    }

    @Override
    public void setIdentifier(ProviderIdentifier identifier) throws RemoteException {
        mProviderIdentifier = identifier;
        mMultiProviderDatabaseHelper.setIdentifier(identifier);
    }

    @Override
    public void registerCallback(IProviderCallback cb) throws RemoteException {
        synchronized (mCallbacks) {
            mCallbacks.add(cb);
        }
    }

    @Override
    public void unregisterCallback(IProviderCallback cb) throws RemoteException {
        synchronized (mCallbacks) {
            mCallbacks.remove(cb);
        }
    }

    @Override
    public boolean isSetup() throws RemoteException {
        return mMultiProviderDatabaseHelper.isSetup();
    }

    @Override
    public boolean login() throws RemoteException {
        return true;
    }

    @Override
    public boolean isAuthenticated() throws RemoteException {
        return true;
    }

    @Override
    public boolean isInfinite() throws RemoteException {
        return false;
    }

    @Override
    public List<Album> getAlbums() throws RemoteException {
        return null;
    }

    @Override
    public List<Artist> getArtists() throws RemoteException {
        return null;
    }

    @Override
    public List<Song> getSongs() throws RemoteException {
        return null;
    }

    @Override
    public List<Playlist> getPlaylists() throws RemoteException {
        return mMultiProviderDatabaseHelper.getPlaylists();
    }

    @Override
    public Song getSong(String ref) throws RemoteException {
        return mMultiProviderDatabaseHelper.getSong(ref);
    }

    @Override
    public Artist getArtist(String ref) throws RemoteException {
        return null;
    }

    @Override
    public Album getAlbum(String ref) throws RemoteException {
        return null;
    }

    @Override
    public Playlist getPlaylist(String ref) throws RemoteException {
        return null;
    }

    @Override
    public boolean getArtistArt(Artist entity, IArtCallback callback) throws RemoteException {
        return false;
    }

    @Override
    public boolean getAlbumArt(Album entity, IArtCallback callback) throws RemoteException {
        return false;
    }

    @Override
    public boolean getPlaylistArt(Playlist entity, IArtCallback callback) throws RemoteException {
        return false;
    }

    @Override
    public boolean getSongArt(Song entity, IArtCallback callback) throws RemoteException {
        return false;
    }

    @Override
    public boolean fetchArtistAlbums(String artistRef) {
        return false;
    }

    @Override
    public boolean fetchAlbumTracks(String albumRef) throws RemoteException {
        return false;
    }

    @Override
    public void setAudioSocketName(String socketName) throws RemoteException {
    }

    @Override
    public long getPrefetchDelay() throws RemoteException {
        return 0;
    }

    @Override
    public void prefetchSong(String ref) throws RemoteException {

    }

    @Override
    public boolean playSong(String ref) throws RemoteException {
        ProviderIdentifier providerId = mSongsProviders.get(ref);
        return getBinder(providerId).playSong(ref);
    }

    @Override
    public void pause() throws RemoteException {
    }

    @Override
    public void resume() throws RemoteException {
    }

    @Override
    public void seek(long timeMs) {
    }

    @Override
    public boolean onUserSwapPlaylistItem(int oldPosition, int newPosition, String playlistRef) throws RemoteException {
        return mMultiProviderDatabaseHelper.swapPlaylistItem(oldPosition, newPosition, playlistRef);
    }

    @Override
    public boolean deletePlaylist(String playlistRef) throws RemoteException {
        return mMultiProviderDatabaseHelper.deletePlaylist(playlistRef);
    }

    @Override
    public boolean deleteSongFromPlaylist(int songPosition, String playlistRef) throws RemoteException {
        return mMultiProviderDatabaseHelper.deleteSongFromPlaylist(songPosition, playlistRef);
    }

    @Override
    public boolean addSongToPlaylist(String songRef, String playlistRef, ProviderIdentifier providerIdentifier) throws RemoteException {
        return mMultiProviderDatabaseHelper.addSongToPlaylist(songRef, playlistRef, providerIdentifier);
    }

    @Override
    public String addPlaylist(String playlistName) throws RemoteException {
        return mMultiProviderDatabaseHelper.addPlaylist(playlistName);
    }

    @Override
    public List<Genre> getGenres() {
        return null;
    }

    private void removeCallback(final IProviderCallback cb) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    MultiProviderDatabaseHelper.LocalCallback mLocalCallback = new MultiProviderDatabaseHelper.LocalCallback() {
        @Override
        public void playlistUpdated(final Playlist playlist) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    synchronized (mCallbacks) {
                        for (IProviderCallback cb : mCallbacks) {
                            try {
                                cb.onPlaylistAddedOrUpdated(mProviderIdentifier, playlist);
                            } catch (DeadObjectException e) {
                                removeCallback(cb);
                            } catch (RemoteException e) {
                                Log.e(TAG, "RemoteException when notifying a callback", e);
                            }
                        }
                    }
                }
            });
        }

        @Override
        public void searchFinished(final SearchResult searchResult) {
            if (searchResult != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (mCallbacks) {
                            for (IProviderCallback cb : mCallbacks) {
                                try {
                                    cb.onSearchResult(searchResult);
                                } catch (DeadObjectException e) {
                                    removeCallback(cb);
                                } catch (RemoteException e) {
                                    Log.e(TAG, "RemoteException when notifying a callback", e);
                                }
                            }
                        }
                    }
                });
            }
        }
    };

    public void startSearch(String query) {
        mMultiProviderDatabaseHelper.startSearch(query);
    }

    @Override
    public Bitmap getLogo(String ref) throws RemoteException {
        return null;
    }

    @Override
    public List<String> getSupportedRosettaPrefix() throws RemoteException {
        return null;
    }

    @Override
    public void setPlaylistOfflineMode(String ref, boolean offline) throws RemoteException {
        Log.e(TAG, "Unimplemented");
    }

    @Override
    public void setOfflineMode(boolean offline) throws RemoteException {
        Log.e(TAG, "Unimplemented");
    }

}
