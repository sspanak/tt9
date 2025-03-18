const { basename } = require('path');
const { existsSync, readFileSync } = require('fs');;
const { print, printError } = require('./_printers.js')


function printHelp() {
	print(`Usage ${basename(process.argv[1])} WORD-LIST.txt`);
	print('Normalizes the frequencies in a dictionary with transcriptions.');
}



function validateInput() {
	if (process.argv.length < 3) {
		printHelp();
		process.exit(1);
	}

	if (!existsSync(process.argv[2])) {
		printError(`Failure! Could not find word list file "${process.argv[2]}".`);
		process.exit(2);
	}

	return {
		fileName: process.argv[2]
	};
}


function printWords(wordList) {
	if (Array.isArray(wordList)) {
		wordList.forEach(w => print(
			w.number !== null ? `${w.chinese}\t${w.latin}\t${w.number}` : `${w.chinese}\t${w.latin}`
		));
	}
}


const { fileName } = validateInput();

const data = readFileSync(fileName, 'utf8');
const lines = data.trim().split('\n');

// Parse the data into an array of objects
let entries = lines.map(line => {
	const parts = line.split('\t');
	return {
		original: line,
		chinese: parts[0],
		latin: parts[1],
		number: parts[2] ? parseInt(parts[2], 10) : null
	};
});

// Group entries by the Latin character sequence
const groups = {};
entries.forEach(entry => {
	if (!groups[entry.latin]) {
		groups[entry.latin] = [];
	}
	groups[entry.latin].push(entry);
});

// Process each group: sort by number (descending) and reassign ordinal numbers
let sortedEntries = [];
for (const key in groups) {
	let group = groups[key];

	// Separate entries with and without numbers
	let withNumbers = group.filter(e => e.number !== null);
	let withoutNumbers = group.filter(e => e.number === null);

	// Sort by number in descending order
	withNumbers.sort((a, b) => b.number - a.number);

	// Assign ordinal rankings
	for (let i = 0; i < withNumbers.length; i++) {
		withNumbers[i].number = (withNumbers.length - i).toString();
	}

	// Preserve original order for entries without numbers
	sortedEntries.push(...withNumbers, ...withoutNumbers);
}

printWords(sortedEntries);
