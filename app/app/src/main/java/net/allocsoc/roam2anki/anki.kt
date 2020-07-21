package net.allocsoc.roam2anki

import android.content.Context
import com.ichi2.anki.api.AddContentApi

class Anki(context: Context) {
    val api = AddContentApi(context);
    val api2 = Roam2AnkiAddContentApi(context);

    fun execute(deck: AnkiJson) {


        deck.decks.forEach {
            val deckId = createDeckIfNotExist(it.name)
            val modelId = api.modelList.filterValues { it == "Cloze" }.keys.firstOrNull()

            if (modelId != null) {
                val fields = api.getFieldList(modelId)
                val allNotes = api2.getAllNotes();
                val noteMapping = createIdMapping(allNotes, fields)

                if (!fields.map { it.toUpperCase()}.contains("ID"))
                    return

                it.cards.forEach {
                    val card = it
                    val values = fields.asList().map {
                        when(it.toUpperCase()) {
                            "TEXT" -> card.text
                            "ID" -> card.id
                            "TYPE" -> "Roam2Anki"
                            else -> ""
                        }
                    }.toTypedArray()

                    val noteId = noteMapping[card.id]
                    if (noteId == null) {
                        api.addNote(modelId, deckId, values, setOf("RoamCard"))
                    } else {
                        api.updateNoteFields(noteId, values)
                    }
                }
            }

        }
    }

    private fun createDeckIfNotExist(deckname: String): Long {
        val hasDeck = api.deckList.values.contains(deckname);


        if (!hasDeck) {
            api.addNewDeck(deckname)
        }

        return api.deckList.filterValues{ it == deckname }.keys.toList().firstOrNull()!!
    }

    private fun createIdMapping(notes: List<Note>, fields: Array<String>): Map<String, Long> {
        val map = mutableMapOf<String, Long>()
        val idField = fields.indexOfFirst { it.toUpperCase() == "ID" }

        notes.forEach {
            val k = it.mFields[idField]
            val v = it.mId

            map[k] = v
        }

        return map
    }

}
