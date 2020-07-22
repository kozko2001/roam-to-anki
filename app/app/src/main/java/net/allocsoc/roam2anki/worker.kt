package net.allocsoc.roam2anki

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.core.awaitResponse
import com.github.kittinunf.fuel.coroutines.awaitObject
import com.github.kittinunf.fuel.coroutines.awaitObjectResponse
import com.github.kittinunf.fuel.coroutines.awaitObjectResponseResult
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.github.kittinunf.fuel.gson.responseObject


class SyncWork(val context: Context, val params: WorkerParameters): CoroutineWorker(context, params) {
    override suspend fun doWork(): Result
    {
        return try {
            val (request, response, result) = Fuel.get("https://allocsoc.net/anki/decks.json").responseObject<AnkiJson>()
            val deck = result.get()
            Anki(context).execute(deck);

            Result.success()
        } catch (error: Throwable) {
            Result.failure()
        }
    }
}
