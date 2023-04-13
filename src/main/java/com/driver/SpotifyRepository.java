package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        User user = new User(name, mobile);
        users.add(user);
        return user;
    }

    public Artist createArtist(String name) {
        Artist artist = new Artist(name);
        artists.add(artist);
        return artist;
    }

    public Album createAlbum(String title, String artistName) {
        boolean artistFound = false;
        Album album = new Album(title);
        albums.add(album);
        for(Artist artist : artists){
            if(artist.getName().equals(artistName)){
                artistFound = true;
                List<Album> albumsOfArtist = artistAlbumMap.get(artist);
                albumsOfArtist.add(album);
                artistAlbumMap.put(artist,albumsOfArtist);
            }
        }
        if(artistFound == false){
            Artist artist = new Artist(artistName);
            artists.add(artist);
            List<Album> albumsOfArtist =  new ArrayList<>();
            albumsOfArtist.add(album);
            artistAlbumMap.put(artist,albumsOfArtist);
        }
        return album;
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
       boolean albumFound = false;
       Song song = new Song(title, length);
       songs.add(song);
       for(Album album : albumSongMap.keySet()){
           if(album.getTitle().equals(albumName)){
               albumFound = true;
               List<Song> songsOfAlbum = albumSongMap.get(album);
               songsOfAlbum.add(song);
               albumSongMap.put(album, songsOfAlbum);
           }
       }
       if(albumFound == false){
           throw new Exception("Album does not exist");
       }
       else{
           return song;
       }
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {

        Playlist playlist = new Playlist(title);
        List<Song> songsInPlaylist = new ArrayList<>();
        for(Song song : songs){
            if(song.getLength() == length){
                songsInPlaylist.add(song);
            }
        }
        playlistSongMap.put(playlist, songsInPlaylist);
        boolean userFound = false;
        for(User user : users){
            if(user.getMobile().equals(mobile)){
                userFound = true;
                creatorPlaylistMap.put(user, playlist);
                List<User> listeners = new ArrayList<>();
                listeners.add(user);
                playlistListenerMap.put(playlist, listeners);
            }
        }
        if(userFound == false){
            throw new Exception("User does not exist");
        }
        else{
            return playlist;
        }
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        Playlist playlist = new Playlist(title);
        List<Song> songsInPlaylist = new ArrayList<>();
        for(Song song : songs){
            String songName = song.getTitle();
            if(songTitles.contains(songName)){
                songsInPlaylist.add(song);
            }
        }
        playlistSongMap.put(playlist, songsInPlaylist);
        boolean userFound = false;
        for(User user : users){
            if(user.getMobile().equals(mobile)){
                userFound = true;
                creatorPlaylistMap.put(user, playlist);
                List<User> listeners = new ArrayList<>();
                playlistListenerMap.put(playlist, listeners);
            }
        }
        if(userFound == false){
            throw new Exception("User does not exist");
        }
        else{
            return playlist;
        }
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        boolean playlistFound = false;
        boolean userFound = false;
        boolean userIsCreator = false;
        boolean userIsListener = false;
        for(Playlist playlist : playlists){
            if(playlist.getTitle().equals(playlistTitle)){
                playlistFound = true;
                for(User user : users){
                    if(user.getMobile().equals(mobile)){
                        userFound = true;
                        for(User user1 : creatorPlaylistMap.keySet()){
                            if(user1 == user){
                                userIsCreator = true;
                                break;
                            }
                        }
                        List<User> usersOfPlaylist = playlistListenerMap.get(playlist);
                        if(usersOfPlaylist.contains(user)){
                            userIsListener = true;
                        }
                  if(userIsCreator == false && userIsListener == false){
                      List<User> listeners = playlistListenerMap.get(playlist);
                      listeners.add(user);
                      playlistListenerMap.put(playlist, listeners);
                      return playlist;
                  }
                    }
                }
                if(userFound == false){
                    throw new Exception("User does not exist");
                }
            }
        }
        if(playlistFound == false){
            throw new Exception("Playlist does not exist");
        }
        return null;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {

        boolean userFound = false;
        boolean songFound = false;
        for(User user : users){
            if(user.getMobile().equals(mobile)){
                userFound = true;
                for(Song song : songs){
                    if(song.getTitle().equals(songTitle)){
                        songFound = true;
                        if(songLikeMap.containsKey(song)){
                            List<User> songLikedByUsers = songLikeMap.get(song);
                            if(!songLikedByUsers.contains(user)){
                                songLikedByUsers.add(user);
                                songLikeMap.put(song, songLikedByUsers);
                                song.setLikes(song.getLikes()+1);
                                Album album = null;
                                for(Map.Entry<Album, List<Song>> entry : albumSongMap.entrySet()){
                                    List<Song> songs = entry.getValue();
                                    if(songs.contains(song)){
                                        album = entry.getKey();
                                    }
                                }
                                Artist artist;
                                for(Map.Entry<Artist, List<Album>> entry : artistAlbumMap.entrySet()){
                                    List<Album> albums = entry.getValue();
                                    if(albums.contains(album)){
                                        artist = entry.getKey();
                                        artist.setLikes(artist.getLikes()+1);
                                    }
                                }
                            }
                        }
                        else{
                            List<User> songLikedByUsers = new ArrayList<>();
                            songLikedByUsers.add(user);
                            songLikeMap.put(song, songLikedByUsers);
                            song.setLikes(song.getLikes()+1);
                            Album album = null;
                            for(Map.Entry<Album, List<Song>> entry : albumSongMap.entrySet()){
                                List<Song> songs = entry.getValue();
                                if(songs.contains(song)){
                                    album = entry.getKey();
                                }
                            }
                            Artist artist;
                            for(Map.Entry<Artist, List<Album>> entry : artistAlbumMap.entrySet()){
                                List<Album> albums = entry.getValue();
                                if(albums.contains(album)){
                                    artist = entry.getKey();
                                    artist.setLikes(artist.getLikes()+1);
                                }
                            }
                        }
                    }
                }
                if(songFound == false){
                    throw new Exception("Song does not exist");
                }
            }
        }
        if(userFound == false){
            throw new Exception("User does not exist");
        }
        return null;
    }

    public String mostPopularArtist() {
        int max = 0;
        String artistName = null;
        for(Artist artist : artists){
            if(artist.getLikes() > max){
                max = artist.getLikes();
                artistName = artist.getName();
            }
        }
        return artistName;
    }

    public String mostPopularSong() {
        int max = 0;
        String songName = null;
        for(Song song : songs){
            if(song.getLikes() > max){
                max = song.getLikes();
                songName = song.getTitle();
            }
        }
        return songName;
    }
}
