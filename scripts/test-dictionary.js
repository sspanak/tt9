const { basename, dirname, join: joinPath } = require('path');
const { createReadStream, existsSync } = require('fs');
const { createInterface } = require('readline');
const { print, printError, printWordsWithFrequencies } = require('./_printers.js');


function printHelp() {
	print(`Usage ${basename(process.argv[1])} LANGUAGE-DEFINITION.yml VALIDATION-TEXT-FILE.txt [--lowercase]`);
	print('Attempts to type the words from VALIDATION-TEXT-FILE.txt using the alphabet and the dictionary defined in the YML.');
}



function validateInput() {
	if (process.argv.length < 3) {
		printHelp();
		process.exit(1);
	}

	if (!existsSync(process.argv[2])) {
		printError(`Failure! Could not find the language definition file "${process.argv[2]}".`);
		process.exit(2);
	}

	if (!existsSync(process.argv[3])) {
		printError(`Failure! Could not find the validation text file "${process.argv[3]}".`);
		process.exit(2);
	}

	return {
		definitionFile: process.argv[2],
		testWordsFile: process.argv[3],
		lowercase: process.argv.length === 5 && process.argv[4] === '--lowercase',
		definitionsDir: 'definitions',
		dictionariesDir: 'dictionaries',
		minWordLength: 4
	};
}


async function readDefinition(fileName) {
	const retval = { alphabet: '', dictionaryFileName: '', locale: '' };

	if (!fileName) {
		return retval;
	}


	const dictionaryFilePattern = /dictionaryFile:\s([\w\-\.]+)/
	const localePattern = /locale:\s([\w\-]+)/
	const lettersPattern = /^\s+-\s*\[([^\]]+)/;

	for await (const line of createInterface({ input: createReadStream(fileName) })) {
		if (line.includes('SPECIAL') || line.includes('PUNCTUATION')) {
			continue;
		}

		let matches = line.match(lettersPattern);
		if (matches && matches[1]) {
			retval.alphabet += matches[1].replace(/,\s/g, '');
		}

		matches = line.match(dictionaryFilePattern);
		if (matches && matches[1]) {
			retval.dictionaryFileName = matches[1];
		}

		matches = line.match(localePattern);
		if (matches && matches[1]) {
			retval.locale = matches[1];
		}
	}

	return retval;
}


async function readDictionary(locale, fileName) {
	const words = new Map();
	
	if (!fileName) {
		return words;
	}

	for await (const line of createInterface({ input: createReadStream(fileName) })) {
		const word = line.split('\t')[0];
		if (word.length > 0) {
			words.set(word.toLocaleLowerCase(locale), word);
		}
	}

	return words;
}


async function readWords(locale, alphabet, fileName, minWordLength) {
	const words = new Map();
	
	if (!fileName) {
		return words;
	}

	validChars = "\-'" + alphabet + alphabet.toLocaleUpperCase(locale);
	const containsLowercase = new RegExp('[' + alphabet + ']');
	const validWords = new RegExp('(?<![\\p{L}\\d])([' + validChars + ']+)(?![\\p{L}\\d])', 'g');
	const multiSpace = /\s+/g;

	for await (const line of createInterface({ input: createReadStream(fileName) })) {
		let lineWords = line.match(validWords);
		lineWords = lineWords === null ? [] : lineWords;

		for (const word of lineWords) {

			if (word.length < minWordLength || word.match(containsLowercase) === null) {
				continue;
			}

			const key = word.toLocaleLowerCase(locale);

			if (!words.has(key) || (key !== words.get(key) && key === word)) {
				words.set(key, word);
			}
		}
	}

	return words;
}


function testTyping(locale, minWordLength, lowercase, testWords, dictionaryWords) {
	const newWords = [];
	const corrections = new Map();

	if (!(testWords instanceof Map) || !(dictionaryWords instanceof Map)) {
		return { newWords, corrections };
	}

	const variations = new Map();

	testWords.forEach((key, word) => {
		variations.clear();
		variations.set(key, word);

		const parts = word.split(/[\-']/);
		for (let i = 0; parts.length > 1 && i < parts.length; i++) {
			variations.set(`${parts[i]}-`.toLocaleLowerCase(locale), `${parts[i]}-`);
			variations.set(`-${parts[i]}`.toLocaleLowerCase(locale), `-${parts[i]}`);
			variations.set(`-${parts[i]}-`.toLocaleLowerCase(locale), `-${parts[i]}-`);
			variations.set(`${parts[i]}'`.toLocaleLowerCase(locale), `${parts[i]}'`);
			variations.set(`'${parts[i]}`.toLocaleLowerCase(locale), `'${parts[i]}`);
			variations.set(`'${parts[i]}'`.toLocaleLowerCase(locale), `'${parts[i]}'`);
		}

		variations.forEach((variationKey, wordVariation) => {
			if (wordVariation.length < minWordLength) {
				return;
			}

			if (!dictionaryWords.has(variationKey)) {
				if (!wordVariation.includes("'") && !wordVariation.includes('-')) {
					newWords.push(wordVariation);
				}
			} else if (dictionaryWords.get(variationKey) !== wordVariation) {
				if (!lowercase || (lowercase && wordVariation === variationKey)) {
					corrections.set(dictionaryWords.get(variationKey), wordVariation);
				}
			}
		});
	});

	return { newWords, corrections };
}


function formatCorrections({ corrections, newWords }) {
	const output = [];

	if (Array.isArray(newWords) && newWords.length) {
		output.push({ word: `==== NEW WORDS ====` });
		newWords.forEach(word => output.push({ word }));
	}

	if (corrections instanceof Map && corrections.size) {
		output.push({ word: `==== CORRECTED WORDS ====` });
		corrections.forEach((word, key) => {
			output.push({word: `${key} => ${word}` });
		});
	}

	return output;
}


async function work({ definitionFile, testWordsFile, lowercase, definitionsDir, dictionariesDir, minWordLength }) {
	const { alphabet, dictionaryFileName, locale } = await readDefinition(definitionFile);

	const dictionaryFilePath = joinPath(
		dirname(definitionFile).replace('/' + definitionsDir, '/' + dictionariesDir), 
		dictionaryFileName
	);

	const wordsPromise = Promise.all([
		locale,
		minWordLength,
		lowercase,
		readWords(locale, alphabet, testWordsFile, minWordLength),
		readDictionary(locale, dictionaryFilePath)
	]);

	return formatCorrections(testTyping(...await wordsPromise))
}



/** main **/
work(validateInput())
	.then(corrections => printWordsWithFrequencies(corrections))
	.catch(e => printError(e));
