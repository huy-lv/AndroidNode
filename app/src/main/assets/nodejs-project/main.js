const http = require('http');
const leftPad = require('left-pad');
const versions_server = http.createServer( (request, response) => {
  response.end('Versi1111ons: ' + JSON.stringify(process.versions) + ' left-pad: ' + leftPad(42, 5, '0'));
});
versions_server.listen(3000);
console.log('The node project has started.');