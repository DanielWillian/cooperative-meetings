import http from 'k6/http';
import { check } from 'k6';
import { uuidv4, randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.1.0/index.js';

const subject = {
  id: randomIntBetween(1, 100000),
  name: 'subject ' + randomIntBetween(1, 1000),
}

const poll = {
  name: 'poll ' + randomIntBetween(1, 1000),
}

const vote = {
  agree: true,
}

const params = {
  headers: {
    'Content-Type': 'application/json'
  }
}
var voteParams = JSON.parse(JSON.stringify(params));
voteParams.tags = { name: 'vote' }

export let options = {
  vus: 5,
  iterations: 100,
}

export function setup() {
  var subjectRes = http.post('http://host.docker.internal:8080/subjects', JSON.stringify(subject), params);
  if (subjectRes.status !== 201) {
    throw new Error('incorrect data: ' + JSON.stringify(subjectRes));
  }
  var pollRes = http.post('http://host.docker.internal:8080/subjects/' + subject.id + '/polls',
      JSON.stringify(poll),
      params);
  if (pollRes.status !== 201) {
    throw new Error('incorrect data: ' + JSON.stringify(subjectRes));
  }
  return {
    subjectId: subject.id,
    pollId: JSON.parse(pollRes.body).id,
  }
}

export default function(data) {
  vote.voter = uuidv4();
  var voteRes = http.post('http://host.docker.internal:8080/subjects/' +
          data.subjectId + '/polls/' + data.pollId + '/votes',
      JSON.stringify(vote),
      voteParams);
  check(voteRes, {
      'vote status is 201': (r) => r.status === 201
  });
}

