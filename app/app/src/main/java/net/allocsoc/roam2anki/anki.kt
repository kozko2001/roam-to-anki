package net.allocsoc.roam2anki

import android.content.Context
import android.util.Log
import com.ichi2.anki.api.AddContentApi

class Anki(context: Context) {
    val api: AddContentApi = AddContentApi(context);
    val deckId: Long?

    init {
        val hasDeck = api.deckList.values.contains("ROAM");


        if (!hasDeck) {
            api.addNewDeck("ROAM")
        }

        deckId = api.deckList.filterValues{ it == "ROAM" }.keys.toList().firstOrNull()
    }

    fun getModels() {
        api.modelList.values.forEach { Log.e("ANKI", it) }


        api.modelList.forEach { id, name ->
            val fields = api.getFieldList(id);
            Log.e("ANKI", "id ${id} ${name} ${fields} ")
        }

        val modelId = api.modelList.filterValues { it == "Cloze" }.keys.firstOrNull()

        if (modelId != null && deckId != null) {
            api.addNote(modelId, deckId, arrayOf("test 1 2 3 {{c1::Atlanta}}"), setOf());
        }
    }


}
