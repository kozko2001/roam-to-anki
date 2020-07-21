package net.allocsoc.roam2anki

import android.content.Context
import android.util.Log
import com.ichi2.anki.api.AddContentApi
import java.time.Instant
import java.time.format.DateTimeFormatter

class Anki(context: Context) {
    val api: AddContentApi = AddContentApi(context);

    fun execute(deck: AnkiJson) {

        deck.decks.forEach {
            val deckId = createDeckIfNotExist(it.name)
            val modelId = api.modelList.filterValues { it == "Cloze" }.keys.firstOrNull()

            if (modelId != null) {
                val fields = api.getFieldList(modelId)

                it.cards.forEach {
                    val card = it
                    val values = fields.asList().map {
                        if(it.toUpperCase() == "TEXT") {
                            card.text
                        } else {
                            ""
                        }
                    }.toTypedArray()

                    api.addNote(modelId, deckId, values, setOf());
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


}
