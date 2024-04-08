exports.print = function(str) {
	process.stdout.write(`${str}\n`);
};

exports.printError = function(str) {
	process.stderr.write(`${str instanceof Error ? str.stack : str}\n`);
};

exports.printWordsWithFrequencies = function(words) {
	if (Array.isArray(words)) {
		words.forEach(w => exports.print(`${w.word}${w.frequency ? '\t' + w.frequency : ''}`));
	}
}
