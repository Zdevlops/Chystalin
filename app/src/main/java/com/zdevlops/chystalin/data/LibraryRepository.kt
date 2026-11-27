package com.zdevlops.chystalin.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class LibraryRepository(context: Context) {

    private val prefs = context.getSharedPreferences("chystalin_library", Context.MODE_PRIVATE)

    fun loadTracks(): List<Track> {
        val raw = prefs.getString(KEY_TRACKS, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    add(
                        Track(
                            id = o.getString("id"),
                            title = o.optString("title", "Tanpa Judul"),
                            artist = o.optString("artist", "Artis tidak diketahui"),
                            filePath = o.getString("filePath"),
                            durationMs = o.optLong("durationMs", 0L),
                            addedAt = o.optLong("addedAt", 0L)
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun saveTracks(tracks: List<Track>) {
        val arr = JSONArray()
        tracks.forEach { t ->
            arr.put(
                JSONObject().apply {
                    put("id", t.id)
                    put("title", t.title)
                    put("artist", t.artist)
                    put("filePath", t.filePath)
                    put("durationMs", t.durationMs)
                    put("addedAt", t.addedAt)
                }
            )
        }
        prefs.edit().putString(KEY_TRACKS, arr.toString()).apply()
    }

    fun loadPlaylists(): List<Playlist> {
        val raw = prefs.getString(KEY_PLAYLISTS, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val ids = o.optJSONArray("trackIds") ?: JSONArray()
                    add(
                        Playlist(
                            id = o.getString("id"),
                            name = o.optString("name", "Playlist"),
                            trackIds = buildList { for (j in 0 until ids.length()) add(ids.getString(j)) },
                            rotation = runCatching {
                                RotationMode.valueOf(o.optString("rotation", RotationMode.REPEAT_ALL.name))
                            }.getOrDefault(RotationMode.REPEAT_ALL)
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun savePlaylists(playlists: List<Playlist>) {
        val arr = JSONArray()
        playlists.forEach { p ->
            arr.put(
                JSONObject().apply {
                    put("id", p.id)
                    put("name", p.name)
                    put("trackIds", JSONArray(p.trackIds))
                    put("rotation", p.rotation.name)
                }
            )
        }
        prefs.edit().putString(KEY_PLAYLISTS, arr.toString()).apply()
    }

    companion object {
        private const val KEY_TRACKS = "tracks_v1"
        private const val KEY_PLAYLISTS = "playlists_v1"
    }
}
