import argparse
import pathlib
import json
from dataclasses import dataclass
import anki
import os

@dataclass
class Card:
    type: str
    id: str
    text: str

@dataclass
class Deck:
    name: str
    cards: list[Card]


def main(fd, username, password):
    ankipath = "./empty-user/collection.anki2"
    col = anki.Collection(ankipath, log=True)

    k = col.backend.sync_login(username = username,  password = password)
    
    col.backend.full_download(k)

    with fd:
        decks = json.load(fd)['decks']
        decks = list(map(lambda deck: parseDeck(deck), decks))

    
    model = get_model(col)

    anki_deck = next(filter(lambda d: d['name'] == "Roam2Anki", col.decks.all()))
    anki_deck_id = anki_deck['id']

    notes = [anki.notes.Note(col, id=noteid) for noteid in col.find_notes("")]

    for card in decks[0].cards:
        success = update_note(notes, card)

        if not success:
            print("should create this card", card)
            add_note(col, model, anki_deck_id, card)


    col.save(trx=False)

    col.backend.full_upload(k)




def update_note(anki_notes: list[anki.notes.Note], card: Card) -> bool:
    note = [note for note in anki_notes if note.fields[1] == card.id]

    if note:
        note[0].fields[0] = card.text

        return True
    
    return False
    
def add_note(col: anki.Collection, model: dict, deck_id: int, card: Card) -> anki.notes.Note:
    note = anki.notes.Note(col, model)
    note.fields = [card.text, card.id, "Roam2Anki"]
    
    col.add_note(note, deck_id)
    

def get_model(col: anki.Collection) -> dict:
    model = col.models.byName("Cloze")
    fields = model['flds']
    

    assert len(fields) == 3
    assert fields[0]['name'] == 'Text'
    assert fields[1]['name'] == 'Id'
    assert fields[2]['name'] == 'type'

    return model

def parseDeck(json_deck) -> Deck:
    name = json_deck['name']
    cards = list(map(lambda card: parseCard(card), json_deck['cards']))

    return Deck(name, cards)


def parseCard(json_card) -> Card:
    _type = json_card['type']
    _id = json_card['id']
    _text = json_card['text']
    return Card(_type, _id, _text)

if __name__ == "__main__":
    
    parser = argparse.ArgumentParser(description='Imports json anki questions into an anki db')

    parser.add_argument('--infile', type=argparse.FileType('r', encoding='UTF-8'), required=True)
    parser.add_argument('--username', type=str, default=os.environ.get('ANKI_USERNAME'))
    parser.add_argument('--password', type=str, default=os.environ.get('ANKI_PASSWORD'))

    args = parser.parse_args()

    main(args.infile, args.username, args.password)
    