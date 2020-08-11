package net.allocsoc.roam2anki

import android.content.Context
import android.database.Cursor
import com.ichi2.anki.FlashCardsContract
import com.ichi2.anki.api.AddContentApi

import java.util.*

class Note(val mId: Long, val mFields: Array<String>, val mTags: Set<String>)

class Roam2AnkiAddContentApi(context: Context) {

    val mResolver = context.getContentResolver()
    private val PROJECTION = arrayOf(
        FlashCardsContract.Note._ID,
        FlashCardsContract.Note.FLDS,
        FlashCardsContract.Note.TAGS
    )

    fun getAllNotes(): List<Note> {
        val noteUri = FlashCardsContract.Note.CONTENT_URI_V2
        val cursor = mResolver.query(noteUri, PROJECTION, null, null, null)!!
        val result = mutableListOf<Note>()

        return try {
            while(cursor.moveToNext()) {
                val note = createNoteFromCursor(cursor)

                if (note != null)
                    result.add(note)
            }
            result.filter { it.mTags.contains("RoamCard") }
        } finally {
            cursor.close()
        }
    }

    fun createNoteFromCursor(cursor: Cursor):Note? {
        return try {
            val idIndex = cursor.getColumnIndexOrThrow(FlashCardsContract.Note._ID)
            val fldsIndex = cursor.getColumnIndexOrThrow(FlashCardsContract.Note.FLDS)
            val tagsIndex = cursor.getColumnIndexOrThrow(FlashCardsContract.Note.TAGS)
            val fields = splitFields(cursor.getString(fldsIndex))?.filterNotNull().orEmpty().toTypedArray()
            val id = cursor.getLong(idIndex)
            val tags: Set<String> = splitTags(cursor.getString(tagsIndex))?.filterNotNull()?.toSet().orEmpty()

            Note(id, fields, tags)
        } catch (e: Exception) {
            null
        }
    }

    fun splitFields(fields: String?): Array<String?>? {
        return fields?.split("\\x1f".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()
    }

    fun splitTags(tags: String?): Array<String?>? {
        return tags?.trim { it <= ' ' }?.split("\\s+".toRegex())?.toTypedArray()
    }


}