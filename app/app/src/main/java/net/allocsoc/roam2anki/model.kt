package net.allocsoc.roam2anki


class AnkiJson (val decks: List<Deck>)

class Deck (val name: String, val cards: List<Card>)

class Card (val type: String, val text: String, val id: String)
