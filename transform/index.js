const { glob } = require('glob');
const fs = require('fs');

var argv = require('yargs')
    .usage('Usage: $0 export [options]')
    .command('export', 'export to roam')
    .example('$0 export -i input_folder -o output_file', 'generates export file from markdowns in folder')
    .alias('i', 'input_folder')
    .alias('o', 'output_file')
    .demandOption(['i', 'o'])
    .help('h')
    .alias('h', 'help')
    .argv;

Object.defineProperty(Array.prototype, 'flat', {
    value: function(depth = 1) {
        return this.reduce(function (flat, toFlatten) {
        return flat.concat((Array.isArray(toFlatten) && (depth>1)) ? toFlatten.flat(depth-1) : toFlatten);
        }, []);
    }
});

    
const markdown_files = (input_folder) => {
    return glob.sync(`${input_folder}/**/*.md`);
}


const extractId = (line) => {
    const match = line.match(/.*{{\s*id:+\s*([^}]+)}}.*/);
    if (match && match.length >= 2) {
        return match[1];
    }
}


const hasFlashcardToken = (line) => {
    const match = line.match(/.*(#Flashcard|\[\[Flashcard\]\]).*/i);
    if (match && match.length >= 2) {
        return match[1];
    }
}


const get_flashcard_data = (file) => {
    const content = fs.readFileSync(file, 'utf8');

    const lines = content.split('\n');
    const id = lines.map(extractId);
    const flashcardToken = lines.map(hasFlashcardToken);

    const data = 
        id.map((id, i) => ({id, text: lines[i], hasFlashcard: !!flashcardToken[i] }))
        .flat()
        .filter(i => i.hasFlashcard && i.id)
        
    return data;
}

const processMath = (flashcard) => {
    console.log(flashcard)
    const text = flashcard.text;

    const mathText = text.replace(/\$\$(.*?)\$\$/g, (a,b) => `\\(${b})\\`);
    return {
        ...flashcard,
        text: mathText,
    }
}

const format = (flashcard) => ({
        type: 'cloze',
        id: flashcard.id,
        text: flashcard.text
    });

const main = (output_file, input_folder) => {
    md_files = markdown_files(input_folder);

    const flashcards = md_files
            .map(get_flashcard_data)
            .flat()
            .map(processMath)
            .map(format)
    
    const decks = {
        decks: [
            {
                "name": "Roam2Anki", 
                "cards": flashcards
            }
        ]
    }

    const data = JSON.stringify(decks);

    fs.writeFileSync(output_file, data);

    console.log(md_files, decks)

}

main(argv.o, argv.i)

