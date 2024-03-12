const { basename } = require('path');
const { createReadStream, existsSync } = require('fs');
const { createInterface } = require('readline');


function print(str) {
	process.stdout.write(`${str}\n`);
}


function printHelp() {
	print(`Usage ${basename(process.argv[1])} --blacklist|whitelist DICTIONARY_LOCALE DICTIONARY.TXT FOREIGN_WORDS_LOCALE FOREIGN-WORD-DICTIONARY.txt`);
	print('Removes foreign words from a dictionary. "blacklist" and "whitelist" determine how the FOREIGN-WORD-DICTIONARY.txt is used.');
}



function validateInput() {
	if (process.argv.length < 7) {
		printHelp();
		process.exit(1);
	}

	if (process.argv[2] !== '--blacklist' && process.argv[2] !== '--whitelist') {
		console.error(`Failure! You must specify whether to use the foreign words file as a blacklist or a whitelist."`);
		process.exit(3);
	}

	if (!existsSync(process.argv[4])) {
		console.error(`Failure! Could not find words file "${process.argv[4]}."`);
		process.exit(2);
	}

	if (!existsSync(process.argv[6])) {
		console.error(`Failure! Could not find foreign words file "${process.argv[6]}."`);
		process.exit(2);
	}

	return {
		isBlacklist: process.argv[2] === '--blacklist',
		locale: process.argv[3],
		fileName: process.argv[4],
		foreignWordsLocale: process.argv[5],
		foreignWordsFileName: process.argv[6]
	};
}


async function work({ isBlacklist, locale, fileName, foreignWordsLocale, foreignWordsFileName }) {
	const originalWords = new Map();

	let lineReader = createInterface({ input: createReadStream(fileName) });
	for await (const line of lineReader) {
		originalWords.set(line.toLocaleLowerCase(foreignWordsLocale), line);
	}

	const goodWords = new Set();

	lineReader = createInterface({ input: createReadStream(foreignWordsFileName) });
	for await (const line of lineReader) {
		if (typeof line !== 'string' || line.length === 0) {
			continue;
		}

		const wordKey = line.toLocaleLowerCase(locale);

		if (isBlacklist && originalWords.has(wordKey)) {
			originalWords.delete(wordKey);
		}

		if (!isBlacklist && originalWords.has(wordKey)) {
			goodWords.add(line);
		}
	}

	return Array.from(isBlacklist ? originalWords.values() : goodWords);
}



function printWords(wordList) {
	if (Array.isArray(wordList)) {
		wordList.forEach(w => print(w));
	}
}



/** main **/
work(validateInput())
	.then(words => printWords(words))
	.catch(e => console.error(e));
