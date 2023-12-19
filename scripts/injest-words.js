const { basename } = require('path');
const { createReadStream, existsSync } = require('fs');
const { createInterface } = require('readline');


function printHelp() {
	console.log(`Usage ${basename(process.argv[1])} word-list.txt`);
	console.log('Breaks dashed words into separate words, puts multiple words on a line on new lines and deletes repeating new lines.');
}



function validateInput() {
	if (process.argv.length < 3) {
		printHelp();
		process.exit(1);
	}

	if (!existsSync(process.argv[2])) {
		console.error(`Failure! Could not find word list file "${process.argv[3]}."`);
		process.exit(2);
	}

	return {
		fileName: process.argv[2]
	}
}


function printWords(wordList) {
	if (Array.isArray(wordList)) {
		wordList.forEach(w => console.log(w));
	}
}


function cleanSpecialChars(line) {
	const spacesOnly = /^\s+$/;
	const digits = /\d+/;

	if (!line || !line.length || spacesOnly.test(line)) {
		return [];
	}

	return line
		.replaceAll(/[\x01-\x20]+/g, ' ')
		.replaceAll(/[&\s,":;\*/]+/g, ' ')
		.replaceAll(/\s+/g, ' ')
		.replaceAll(/[\[\]\.\?\(\)]/g, '')
		.split(' ')
		.filter(w => w.length > 1 && !digits.test(w));
}


function splitDashedWords(inputWords) {
	if (!Array.isArray(inputWords)) {
		return [];
	}

	const roots = {};
	const words = {};

	for (const word of inputWords) {
		if (!word.includes('-')) {
			words[word] = true;
			continue;
		}

		const parts = word.split('-');
		for (let i = 0; i < parts.length - 1; i++) {
			const key = `${parts[i]}-`;
			if (key in roots) {
				words[key] = true;
			} else {
				roots[key] = true;
				words[parts[i]] = true;
			}
		}

		words[parts[parts.length - 1]] = true;
	}

	return Object.keys(words);
}


async function work({ fileName }) {
	words = [];

	let lineReader = createInterface({ input: createReadStream(fileName) });
	for await (const line of lineReader) {
		newWords = cleanSpecialChars(line);

		words = [
			...words,
			...newWords
		];
	}

	return splitDashedWords(words).filter(w => w.length > 1).sort();
}


/** main **/
work(validateInput())
	.then(words => printWords(words))
	.catch(e => console.error(e));
