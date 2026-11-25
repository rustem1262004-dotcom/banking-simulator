// ===== Словарь: казахский (kk), русский (ru), английский (en) =====
const T = {
  en: { signin:'Sign in', register:'Register', email:'Email', password:'Password', fullName:'Full name', logout:'Log out',
    accounts:'Accounts', transfer:'Transfer', history:'History', newAccount:'New account', accName:'Account name',
    currency:'Currency', initial:'Initial deposit', create:'Create', deposit:'Deposit', withdraw:'Withdraw', amount:'Amount',
    desc:'Description', from:'From account', to:'To my account', toId:'Or destination account ID', send:'Send',
    date:'Date', type:'Type', status:'Status', all:'All', month:'Month', account:'Account', print:'Print / PDF',
    csv:'Download CSV', income:'Income', expense:'Expenses', empty:'No data yet', ok:'Done', hello:'Hello',
    DEPOSIT:'Deposit', WITHDRAWAL:'Withdrawal', TRANSFER:'Transfer', COMPLETED:'Completed', FAILED:'Failed',
    e_funds:'Insufficient funds', e_login:'Invalid email or password', e_taken:'This email is already registered',
    e_owner:'This account does not belong to you' },
  ru: { signin:'Войти', register:'Регистрация', email:'Эл. почта', password:'Пароль', fullName:'Полное имя', logout:'Выйти',
    accounts:'Счета', transfer:'Перевод', history:'История', newAccount:'Новый счёт', accName:'Название счёта',
    currency:'Валюта', initial:'Начальный взнос', create:'Создать', deposit:'Пополнить', withdraw:'Снять', amount:'Сумма',
    desc:'Описание', from:'Со счёта', to:'На мой счёт', toId:'Или ID счёта получателя', send:'Отправить',
    date:'Дата', type:'Тип', status:'Статус', all:'Все', month:'Месяц', account:'Счёт', print:'Печать / PDF',
    csv:'Скачать CSV', income:'Приход', expense:'Расход', empty:'Пока нет данных', ok:'Готово', hello:'Здравствуйте',
    DEPOSIT:'Пополнение', WITHDRAWAL:'Снятие', TRANSFER:'Перевод', COMPLETED:'Выполнено', FAILED:'Ошибка',
    e_funds:'Недостаточно средств', e_login:'Неверная почта или пароль', e_taken:'Эта почта уже зарегистрирована',
    e_owner:'Этот счёт вам не принадлежит' },
  kk: { signin:'Кіру', register:'Тіркелу', email:'Электрондық пошта', password:'Құпия сөз', fullName:'Толық аты-жөні', logout:'Шығу',
    accounts:'Шоттар', transfer:'Аударым', history:'Тарих', newAccount:'Жаңа шот', accName:'Шот атауы',
    currency:'Валюта', initial:'Бастапқы сома', create:'Құру', deposit:'Толықтыру', withdraw:'Ақша алу', amount:'Сома',
    desc:'Сипаттама', from:'Қай шоттан', to:'Өз шотыма', toId:'Немесе алушы шотының ID-і', send:'Жіберу',
    date:'Күні', type:'Түрі', status:'Күйі', all:'Барлығы', month:'Ай', account:'Шот', print:'Басып шығару / PDF',
    csv:'CSV жүктеп алу', income:'Кіріс', expense:'Шығыс', empty:'Әзірге деректер жоқ', ok:'Орындалды', hello:'Сәлеметсіз бе',
    DEPOSIT:'Толықтыру', WITHDRAWAL:'Ақша алу', TRANSFER:'Аударым', COMPLETED:'Орындалды', FAILED:'Қате',
    e_funds:'Шотта қаражат жеткіліксіз', e_login:'Пошта немесе құпия сөз қате', e_taken:'Бұл пошта тіркелген',
    e_owner:'Бұл шот сізге тиесілі емес' }
};
// Сообщения сервера (на английском) -> ключи словаря
const ERR = { 'Insufficient funds':'e_funds', 'Invalid email or password':'e_login',
  'This email is already registered':'e_taken', 'This account does not belong to you':'e_owner' };
const LOC = { en:'en-GB', ru:'ru-RU', kk:'kk-KZ' };

// ===== Помощники =====
const $ = s => document.querySelector(s), $$ = s => [...document.querySelectorAll(s)];
let lang = localStorage.getItem('lang') || 'ru', user = null, accs = [], txs = [];
const t = k => (T[lang] && T[lang][k]) || T.en[k] || k;
const money = n => Number(n).toLocaleString(LOC[lang], { minimumFractionDigits:2, maximumFractionDigits:2 });
const esc = s => String(s ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));

function note(text, bad) {
  const m = $('#msg'); m.textContent = text; m.className = bad ? 'bad' : 'good'; m.hidden = !text;
  clearTimeout(note.timer); note.timer = setTimeout(() => m.hidden = true, 4000);
}
const fail = e => note(ERR[e.message] ? t(ERR[e.message]) : e.message, true);

// Один запрос к серверу. Сессия идёт в cookie автоматически.
async function api(url, method = 'GET', body) {
  const r = await fetch(url, { method, headers:{ 'Content-Type':'application/json' }, body: body ? JSON.stringify(body) : undefined });
  const d = await r.json().catch(() => null);
  if (!r.ok) {
    if (r.status === 401 && url !== '/api/auth/login') showAuth();
    throw new Error((d && d.error) || 'Error ' + r.status);
  }
  return d;
}

// ===== Переключение экранов и языка =====
function show(v) {
  $$('.view').forEach(e => e.hidden = e.id !== v);
  $$('#nav button').forEach(b => b.classList.toggle('on', b.dataset.v === v));
}
function showAuth() { user = null; $('#nav').hidden = true; $('#logout').hidden = true; show('auth'); }
function applyLang() {
  document.documentElement.lang = lang;
  $$('[data-i]').forEach(e => e.textContent = t(e.dataset.i));
  $('#lang').value = lang;
  if (user) { renderAccounts(); fillSelects(); renderTx(); }
}
async function enter(u) {
  user = u; lang = u.language || lang; localStorage.setItem('lang', lang);
  $('#nav').hidden = false; $('#logout').hidden = false;
  await loadAccounts(); applyLang(); show('accounts');
}

// ===== Счета =====
async function loadAccounts() { accs = await api('/api/accounts'); renderAccounts(); fillSelects(); }
function renderAccounts() {
  $('#hello').textContent = t('hello') + ', ' + (user ? user.fullName : '');
  $('#accList').innerHTML = accs.length ? accs.map(a => `
    <div class="card">
      <div class="muted">${esc(a.name)} · ID ${a.id}</div>
      <div class="big">${money(a.balance)} ${esc(a.currency)}</div>
      <div class="muted small">${esc(a.number)}</div>
      <form class="op row" data-id="${a.id}">
        <input type="number" step="0.01" min="0.01" required placeholder="${t('amount')}">
        <button data-op="deposit">${t('deposit')}</button>
        <button data-op="withdraw" class="alt">${t('withdraw')}</button>
      </form>
    </div>`).join('') : `<p class="muted">${t('empty')}</p>`;
}
function fillSelects() {
  for (const id of ['#trFrom', '#trTo', '#hAcc']) {
    const el = $(id), old = el.value;
    el.innerHTML = accs.map(a => `<option value="${a.id}">${esc(a.name)} (${money(a.balance)} ${esc(a.currency)})</option>`).join('');
    if (old) el.value = old;
  }
}

// ===== История и выписка =====
// Знак операции относительно выбранного счёта: +1 приход, -1 расход
function sign(x, id) { return x.type === 'DEPOSIT' ? 1 : x.type === 'WITHDRAWAL' ? -1 : (x.fromAccountId == id ? -1 : 1); }
function filtered() {
  const ty = $('#hType').value, mo = $('#hMonth').value;
  return txs.filter(x => (!ty || x.type === ty) && (!mo || String(x.createdAt).startsWith(mo)));
}
async function loadTx() {
  const id = $('#hAcc').value;
  txs = id ? await api('/api/transactions?accountId=' + id) : [];
  renderTx();
}
function renderTx() {
  const id = $('#hAcc').value; let inc = 0, out = 0;
  $('#txBody').innerHTML = filtered().map(x => {
    const s = sign(x, id), v = Number(x.amount);
    if (x.status === 'COMPLETED') s > 0 ? inc += v : out += v;
    return `<tr><td>${new Date(x.createdAt).toLocaleString(LOC[lang])}</td><td>${t(x.type)}</td><td>${esc(x.description)}</td>
      <td class="num ${s > 0 ? 'in' : 'out'}">${s > 0 ? '+' : '−'}${money(v)}</td><td>${t(x.status)}</td></tr>`;
  }).join('') || `<tr><td colspan="5" class="muted">${t('empty')}</td></tr>`;
  $('#sumIn').textContent = money(inc); $('#sumOut').textContent = money(out);
}
function downloadCsv() {
  const id = $('#hAcc').value, q = v => '"' + String(v ?? '').replace(/"/g, '""') + '"';
  const rows = [['Date','Type','Description','Amount','Status'],
    ...filtered().map(x => [x.createdAt, x.type, x.description, (sign(x, id) * Number(x.amount)).toFixed(2), x.status])];
  const blob = new Blob(['\ufeff' + rows.map(r => r.map(q).join(',')).join('\r\n')], { type:'text/csv;charset=utf-8' });
  const a = document.createElement('a'); a.href = URL.createObjectURL(blob); a.download = 'statement.csv'; a.click();
  URL.revokeObjectURL(a.href);
}

// ===== События =====
const on = (sel, fn) => $(sel).addEventListener('submit', async e => {
  e.preventDefault();
  try { await fn(e); } catch (x) { fail(x); }
});

$('#lang').onchange = e => { lang = e.target.value; localStorage.setItem('lang', lang); applyLang(); };
$$('.tabs button').forEach(b => b.onclick = () => {
  $$('.tabs button').forEach(x => x.classList.toggle('on', x === b));
  $('#loginForm').hidden = b.dataset.t !== 'login'; $('#regForm').hidden = b.dataset.t !== 'reg';
});
$$('#nav button').forEach(b => b.onclick = async () => {
  show(b.dataset.v);
  try { await loadAccounts(); if (b.dataset.v === 'history') await loadTx(); } catch (x) { fail(x); }
});
$('#logout').onclick = async () => { try { await api('/api/auth/logout', 'POST'); } catch {} showAuth(); };
$('#hAcc').onchange = loadTx; $('#hType').onchange = renderTx; $('#hMonth').onchange = renderTx;
$('#printBtn').onclick = () => window.print(); $('#csvBtn').onclick = downloadCsv;

on('#loginForm', async e => enter(await api('/api/auth/login', 'POST', Object.fromEntries(new FormData(e.target)))));
on('#regForm', async e => {
  const d = Object.fromEntries(new FormData(e.target));
  await api('/api/auth/register', 'POST', { ...d, language: lang });
  await enter(await api('/api/auth/login', 'POST', { email: d.email, password: d.password }));
});
on('#newAcc', async e => {
  const d = Object.fromEntries(new FormData(e.target));
  await api('/api/accounts', 'POST', { name: d.name, currency: d.currency, initialDeposit: Number(d.initialDeposit) || 0 });
  e.target.reset(); note(t('ok')); await loadAccounts();
});
on('#accList', async e => {   // пополнение и снятие (кнопка определяется через e.submitter)
  const f = e.target;
  await api('/api/transactions/' + e.submitter.dataset.op, 'POST', { accountId: +f.dataset.id, amount: +f.elements[0].value });
  note(t('ok')); await loadAccounts();
});
on('#trForm', async e => {
  const to = $('#trToId').value || $('#trTo').value;
  await api('/api/transactions/transfer', 'POST', { fromAccountId: +$('#trFrom').value, toAccountId: +to,
    amount: +$('#trAmount').value, description: $('#trDesc').value });
  e.target.reset(); note(t('ok')); await loadAccounts();
});

// ===== Старт: если сессия ещё жива, сразу заходим =====
applyLang();
api('/api/auth/me').then(enter).catch(showAuth);
