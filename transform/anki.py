import re
import glob
from json import dump
import argparse

ID_RE = re.compile(""".*\\{\\{\\s*id\\:+\\s*([^\\}]+)\\}\\}.*""", re.IGNORECASE)
TAG_RE = re.compile(""".*(\#Flashcard|\[\[Flashcard\]\]).*""", re.IGNORECASE)
MATHJAX = r"\$\$(.*?)\$\$"



def getIdAndTagOfLine(line):
    qid = ID_RE.match(line)
    tag = TAG_RE.match(line)
    return qid, tag, line


def processFile(file):
    r = []
    with open(file, "r") as f:
        lines = f.read().split("\n")

        v = map(getIdAndTagOfLine, lines)
        v = filter(lambda x: x[0] and x[1], v)
        for (qid, tag, line) in v:
            id = qid.groups()[0]
            r.append((line, id))
    return r

def processFolder(folder):
    files = glob.glob(f"{folder}/*.md")
    flashcards = map(processFile, files)
    flashcards = [y for x in flashcards for y in x]
    return flashcards


def processMath(text):
    """
    Translates the support of MathJax we use in Obsidian $$ MATH_HERE $$
    to the one that anki supports \( MATH_HERE \)
    """
    return re.sub(MATHJAX, r"\\(\1\\)", text) 

def format(flashcards):
    cards = map(
        lambda card: {"type": "cloze", "id": card[1], "text": processMath(card[0])}, flashcards
    )
    return {"decks": [{"name": "Roam2Anki", "cards": list(cards)}]}


def main(args):
    flashcards = processFolder(args.folder)
    d = format(flashcards)
    d = dump(d, args.outfile)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description="convert roam export markdown to flashcards"
    )
    parser.add_argument("-i", dest="folder", required=True, type=str)
    parser.add_argument(
        "-o",
        dest="outfile",
        required=True,
        type=argparse.FileType("w", encoding="UTF-8"),
    )

    args = parser.parse_args()

    main(args)
