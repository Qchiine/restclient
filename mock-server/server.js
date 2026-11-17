const http = require('node:http');

const PORT = Number(process.env.PORT ?? 8082);

let comptes = [
  { id: 1, solde: 2450.75, type: 'COURANT', dateCreation: '2024-02-14' },
  { id: 2, solde: 18900.0, type: 'EPARGNE', dateCreation: '2023-11-03' },
  { id: 3, solde: 312.4, type: 'COURANT', dateCreation: '2025-06-21' },
];

let nextId = 4;

function toJson(compte) {
  return {
    id: compte.id,
    solde: compte.solde,
    type: compte.type,
    dateCreation: compte.dateCreation,
  };
}

function xmlEscape(value) {
  return String(value)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

function compteToXml(compte) {
  return [
    '<item>',
    `<id>${compte.id}</id>`,
    `<solde>${compte.solde}</solde>`,
    `<type>${xmlEscape(compte.type)}</type>`,
    `<dateCreation>${compte.dateCreation}</dateCreation>`,
    '</item>',
  ].join('');
}

function comptesToXml(list) {
  return `<?xml version="1.0" encoding="UTF-8"?><List>${list.map(compteToXml).join('')}</List>`;
}

function send(res, status, contentType, body) {
  res.writeHead(status, {
    'Content-Type': contentType,
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Headers': 'Content-Type, Accept',
    'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
    'Content-Length': Buffer.byteLength(body),
  });
  res.end(body);
}

function readBody(req) {
  return new Promise((resolve, reject) => {
    let raw = '';
    req.on('data', (chunk) => {
      raw += chunk;
      if (raw.length > 1e6) reject(new Error('payload too large'));
    });
    req.on('end', () => resolve(raw));
    req.on('error', reject);
  });
}

function parsePayload(raw, contentType) {
  if (contentType && contentType.includes('xml')) {
    const pick = (tag) => {
      const match = raw.match(new RegExp(`<${tag}>([\\s\\S]*?)</${tag}>`));
      return match ? match[1].trim() : null;
    };
    const solde = pick('solde');
    return {
      solde: solde === null ? NaN : Number(solde),
      type: pick('type'),
      dateCreation: pick('dateCreation'),
    };
  }
  const parsed = raw ? JSON.parse(raw) : {};
  return {
    solde: Number(parsed.solde),
    type: parsed.type,
    dateCreation: parsed.dateCreation,
  };
}

function wantsXml(req) {
  const accept = req.headers.accept ?? '';
  return accept.includes('xml');
}

const server = http.createServer(async (req, res) => {
  const url = new URL(req.url, `http://localhost:${PORT}`);
  const path = url.pathname;

  if (req.method === 'OPTIONS') {
    return send(res, 204, 'text/plain', '');
  }

  if (path === '/banque/comptes' && req.method === 'GET') {
    if (wantsXml(req)) {
      return send(res, 200, 'application/xml; charset=utf-8', comptesToXml(comptes));
    }
    return send(res, 200, 'application/json; charset=utf-8', JSON.stringify(comptes.map(toJson)));
  }

  if (path === '/banque/comptes' && req.method === 'POST') {
    let payload;
    try {
      payload = parsePayload(await readBody(req), req.headers['content-type']);
    } catch {
      return send(res, 400, 'application/json; charset=utf-8', JSON.stringify({ message: 'JSON invalide.' }));
    }
    if (!Number.isFinite(payload.solde)) {
      return send(res, 422, 'application/json; charset=utf-8', JSON.stringify({ message: 'Solde invalide.' }));
    }
    const created = {
      id: nextId++,
      solde: payload.solde,
      type: payload.type ?? 'COURANT',
      dateCreation: payload.dateCreation ?? new Date().toISOString().slice(0, 10),
    };
    comptes = [...comptes, created];
    if (wantsXml(req)) {
      return send(res, 201, 'application/xml; charset=utf-8', compteToXml(created));
    }
    return send(res, 201, 'application/json; charset=utf-8', JSON.stringify(toJson(created)));
  }

  const idMatch = path.match(/^\/banque\/comptes\/(\d+)$/);
  if (idMatch) {
    const id = Number(idMatch[1]);
    const index = comptes.findIndex((compte) => compte.id === id);

    if (index === -1) {
      return send(res, 404, 'application/json; charset=utf-8', JSON.stringify({ message: 'Compte introuvable.' }));
    }

    if (req.method === 'GET') {
      if (wantsXml(req)) {
        return send(res, 200, 'application/xml; charset=utf-8', compteToXml(comptes[index]));
      }
      return send(res, 200, 'application/json; charset=utf-8', JSON.stringify(toJson(comptes[index])));
    }

    if (req.method === 'PUT') {
      let payload;
      try {
        payload = parsePayload(await readBody(req), req.headers['content-type']);
      } catch {
        return send(res, 400, 'application/json; charset=utf-8', JSON.stringify({ message: 'JSON invalide.' }));
      }
      const updated = {
        ...comptes[index],
        solde: Number.isFinite(payload.solde) ? payload.solde : comptes[index].solde,
        type: payload.type ?? comptes[index].type,
        dateCreation: payload.dateCreation ?? comptes[index].dateCreation,
      };
      comptes = comptes.map((compte) => (compte.id === id ? updated : compte));
      if (wantsXml(req)) {
        return send(res, 200, 'application/xml; charset=utf-8', compteToXml(updated));
      }
      return send(res, 200, 'application/json; charset=utf-8', JSON.stringify(toJson(updated)));
    }

    if (req.method === 'DELETE') {
      comptes = comptes.filter((compte) => compte.id !== id);
      return send(res, 204, 'text/plain', '');
    }
  }

  if (path === '/health') {
    return send(res, 200, 'application/json; charset=utf-8', JSON.stringify({ status: 'ok', comptes: comptes.length }));
  }

  return send(res, 404, 'application/json; charset=utf-8', JSON.stringify({ message: 'Ressource introuvable.' }));
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`REST banque sur http://localhost:${PORT}/banque/comptes (JSON + XML)`);
});
