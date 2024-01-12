const { basename } = require('path');
const { createReadStream, existsSync } = require('fs');
const { createInterface } = require('readline');


function printHelp() {
	console.log(`Usage ${basename(process.argv[1])} LOCALE WORD-LIST.txt`);
	console.log('Sorts a dictionary for optimum search speed.');
}



function validateInput() {
	if (process.argv.length < 3) {
		printHelp();
		process.exit(1);
	}

	if (!existsSync(process.argv[3])) {
		console.error(`Failure! Could not find word list file "${process.argv[3]}."`);
		process.exit(2);
	}

	return {
		fileName: process.argv[3],
		locale: process.argv[2]
	};
}


function printWords(wordList) {
	if (Array.isArray(wordList)) {
		wordList.forEach(w => console.log(`${w.word}${w.frequency ? '\t' + w.frequency : ''}`));
	}
}


async function readWords(fileName) {
	const words = [];

	if (!fileName) {
		return words;
	}

	for await (const line of createInterface({ input: createReadStream(fileName) })) {
		const [word, frequency] = line.split("\t");
		words.push({ 
			word, 
			frequency: Number.isNaN(Number.parseInt(frequency)) ? 0 : Number.parseInt(frequency) 
		});
	}

	return words;
}


function dictionarySort(a, b, letterWeights, locale) {
	if (a.word.length !== b.word.length) {
		return a.word.length - b.word.length;
	}

	for (let i = 0, end = a.word.length; i < end; i++) {
		const charA = a.word.toLocaleLowerCase(locale).charAt(i);
		const charB = b.word.toLocaleLowerCase(locale).charAt(i);
		const distance = letterWeights.get(charA) - letterWeights.get(charB);

		if (distance !== 0) {
			return distance;
		}
	}

	return b.frequency - a.frequency;
}

function getLetterWeights() {
	const letterWeights = new Map();
	letterWeights
		.set('а', 2).set('б', 2).set('в', 2).set('г', 2).set('ґ', 2)
		.set('д', 3).set('е', 3).set('є', 3).set('ж', 3).set('з', 3)
		.set('и', 4).set('і', 4).set('ї', 4).set('й', 4).set('к', 4).set('л', 4)
		.set('м', 5).set('н', 5).set('о', 5).set('п', 5)
		.set('р', 6).set('с', 6).set('т', 6).set('у', 6)
		.set('ф', 7).set('х', 7).set('ц', 7).set('ч', 7)
		.set('ш', 8).set('щ', 8)
		.set('ь', 9).set('ю', 9).set('я', 9);

	letterWeights
		.set('a', 2).set('b', 2).set('c', 2)
		.set('d', 3).set('e', 3).set('f', 3)
		.set('g', 4).set('h', 4).set('i', 4)
		.set('j', 5).set('k', 5).set('l', 5)
		.set('m', 6).set('n', 6).set('o', 6)
		.set('p', 7).set('q', 7).set('r', 7).set('s', 7)
		.set('t', 8).set('u', 8).set('v', 8)
		.set('w', 9).set('x', 9).set('y', 9).set('z', 9);


	return letterWeights;
}


async function work({ fileName, locale }) {	
	return readWords(fileName).then(words => 
		words.sort((a, b) => dictionarySort(a, b, getLetterWeights(), locale))
	);
}



/** main **/
work(validateInput())
	.then(words => printWords(words))
	.catch(e => console.error(e));