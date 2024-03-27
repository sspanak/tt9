exports.print = function(str) {
	process.stdout.write(`${str}\n`);
};

exports.printError = function(str) {
	process.stderr.write(`${str}\n`);
};