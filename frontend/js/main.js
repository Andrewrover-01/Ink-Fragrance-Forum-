/**
 * 墨香论坛 — Frontend JavaScript
 *
 * Responsibilities:
 *  1. Load forum stats from GET /api/stats
 *  2. Load paginated post list from GET /api/posts
 *  3. Handle search via GET /api/posts/search
 *  4. New-post modal — submit to POST /api/posts
 */

'use strict';

/* ─────────────────────────────────────────────────────────
   Configuration
───────────────────────────────────────────────────────── */
const API_BASE = 'http://localhost:8080/api';
const PAGE_SIZE = 10;

/* ─────────────────────────────────────────────────────────
   State
───────────────────────────────────────────────────────── */
let currentPage = 0;
let totalPages  = 0;
let searchKeyword = '';

/* ─────────────────────────────────────────────────────────
   DOM refs
───────────────────────────────────────────────────────── */
const statMembers  = document.getElementById('statMembers');
const statPosts    = document.getElementById('statPosts');
const statComments = document.getElementById('statComments');

const postsList    = document.getElementById('postsList');
const postsEmpty   = document.getElementById('postsEmpty');
const pagination   = document.getElementById('pagination');

const searchForm   = document.getElementById('searchForm');
const searchInput  = document.getElementById('searchInput');

const newPostBtn   = document.getElementById('newPostBtn');
const enterForumBtn= document.getElementById('enterForumBtn');
const registerBtn  = document.getElementById('registerBtn');

const postModal    = document.getElementById('postModal');
const modalBackdrop= document.getElementById('modalBackdrop');
const modalClose   = document.getElementById('modalClose');
const modalCancelBtn = document.getElementById('modalCancelBtn');
const postForm     = document.getElementById('postForm');

/* ─────────────────────────────────────────────────────────
   Toast helper
───────────────────────────────────────────────────────── */
function createToastContainer() {
  let tc = document.querySelector('.toast-container');
  if (!tc) {
    tc = document.createElement('div');
    tc.className = 'toast-container';
    document.body.appendChild(tc);
  }
  return tc;
}

function showToast(message, type = 'info', duration = 3500) {
  const tc = createToastContainer();
  const toast = document.createElement('div');
  toast.className = `toast toast--${type}`;
  toast.textContent = message;
  tc.appendChild(toast);
  setTimeout(() => toast.remove(), duration);
}

/* ─────────────────────────────────────────────────────────
   API helpers
───────────────────────────────────────────────────────── */
async function apiFetch(url, options = {}) {
  const response = await fetch(url, {
    headers: { 'Content-Type': 'application/json', ...options.headers },
    ...options,
  });
  if (!response.ok) {
    let msg = `请求失败 (${response.status})`;
    try {
      const err = await response.json();
      msg = err.message || msg;
    } catch (_) { /* ignore */ }
    throw new Error(msg);
  }
  return response.json();
}

/* ─────────────────────────────────────────────────────────
   Stats
───────────────────────────────────────────────────────── */
async function loadStats() {
  try {
    const data = await apiFetch(`${API_BASE}/stats`);
    animateCount(statMembers,  data.memberCount);
    animateCount(statPosts,    data.postCount);
    animateCount(statComments, data.commentCount);
  } catch (err) {
    console.warn('Stats unavailable:', err.message);
    [statMembers, statPosts, statComments].forEach(el => { el.textContent = '—'; });
  }
}

/** Animate a number from 0 to target */
function animateCount(el, target, duration = 1200) {
  const start   = Date.now();
  const step = () => {
    const elapsed = Date.now() - start;
    const progress = Math.min(elapsed / duration, 1);
    // ease-out cubic
    const eased = 1 - Math.pow(1 - progress, 3);
    el.textContent = Math.floor(eased * target).toLocaleString('zh-CN');
    if (progress < 1) requestAnimationFrame(step);
  };
  requestAnimationFrame(step);
}

/* ─────────────────────────────────────────────────────────
   Post List
───────────────────────────────────────────────────────── */
async function loadPosts(page = 0) {
  postsList.innerHTML = '';
  postsEmpty.textContent = '加载中…';
  postsEmpty.style.display = 'block';

  try {
    let url;
    if (searchKeyword.trim()) {
      url = `${API_BASE}/posts/search?keyword=${encodeURIComponent(searchKeyword)}&page=${page}&size=${PAGE_SIZE}`;
    } else {
      url = `${API_BASE}/posts?page=${page}&size=${PAGE_SIZE}`;
    }

    const data = await apiFetch(url);
    totalPages  = data.totalPages;
    currentPage = data.number;

    if (data.content.length === 0) {
      postsEmpty.textContent = searchKeyword
        ? `未找到含"${searchKeyword}"的帖子`
        : '暂无帖子，快来第一个发帖吧！';
      renderPagination(0, 0);
      return;
    }

    postsEmpty.style.display = 'none';
    data.content.forEach(post => postsList.appendChild(createPostCard(post)));
    renderPagination(data.number, data.totalPages);
  } catch (err) {
    postsEmpty.textContent = '无法连接到服务器，请确认后端已启动（端口 8080）。';
    console.error('Load posts error:', err);
  }
}

function createPostCard(post) {
  const card = document.createElement('article');
  card.className = 'post-card';
  card.setAttribute('role', 'listitem');

  const category = post.category
    ? `<span class="post-card__category">${escHtml(post.category)}</span>`
    : '';

  const date = post.createdAt
    ? new Date(post.createdAt).toLocaleDateString('zh-CN', { year:'numeric', month:'long', day:'numeric' })
    : '';

  card.innerHTML = `
    ${category}
    <h3 class="post-card__title">${escHtml(post.title)}</h3>
    <div class="post-card__meta">
      <span>✍ ${escHtml(post.authorName || '匿名')}</span>
      ${date ? `<span>📅 ${date}</span>` : ''}
    </div>
    <div class="post-card__stats">
      <span>👁 ${(post.viewCount || 0).toLocaleString('zh-CN')}</span>
      <span>💬 ${(post.commentCount || 0).toLocaleString('zh-CN')}</span>
    </div>
  `;

  card.addEventListener('click', () => {
    showToast(`已选择「${post.title}」`, 'info');
  });

  return card;
}

function escHtml(str) {
  if (!str) return '';
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

/* ─────────────────────────────────────────────────────────
   Pagination
───────────────────────────────────────────────────────── */
function renderPagination(currentPage, totalPages) {
  pagination.innerHTML = '';
  if (totalPages <= 1) return;

  const prevBtn = document.createElement('button');
  prevBtn.className = 'page-btn';
  prevBtn.textContent = '‹ 上一页';
  prevBtn.disabled = currentPage === 0;
  prevBtn.addEventListener('click', () => loadPosts(currentPage - 1));
  pagination.appendChild(prevBtn);

  for (let i = 0; i < totalPages; i++) {
    const btn = document.createElement('button');
    btn.className = 'page-btn' + (i === currentPage ? ' active' : '');
    btn.textContent = i + 1;
    btn.addEventListener('click', () => loadPosts(i));
    pagination.appendChild(btn);
  }

  const nextBtn = document.createElement('button');
  nextBtn.className = 'page-btn';
  nextBtn.textContent = '下一页 ›';
  nextBtn.disabled = currentPage >= totalPages - 1;
  nextBtn.addEventListener('click', () => loadPosts(currentPage + 1));
  pagination.appendChild(nextBtn);
}

/* ─────────────────────────────────────────────────────────
   Search
───────────────────────────────────────────────────────── */
searchForm.addEventListener('submit', (e) => {
  e.preventDefault();
  searchKeyword = searchInput.value.trim();
  loadPosts(0);
});

searchInput.addEventListener('input', () => {
  if (searchInput.value.trim() === '' && searchKeyword !== '') {
    searchKeyword = '';
    loadPosts(0);
  }
});

/* ─────────────────────────────────────────────────────────
   Modal
───────────────────────────────────────────────────────── */
function openModal() {
  postModal.setAttribute('open', '');
  modalBackdrop.classList.add('visible');
  document.body.style.overflow = 'hidden';
  document.getElementById('postTitle').focus();
}

function closeModal() {
  postModal.removeAttribute('open');
  modalBackdrop.classList.remove('visible');
  document.body.style.overflow = '';
  postForm.reset();
}

newPostBtn.addEventListener('click', openModal);
modalClose.addEventListener('click', closeModal);
modalCancelBtn.addEventListener('click', closeModal);
modalBackdrop.addEventListener('click', closeModal);

document.addEventListener('keydown', (e) => {
  if (e.key === 'Escape' && postModal.hasAttribute('open')) closeModal();
});

/* Submit new post */
postForm.addEventListener('submit', async (e) => {
  e.preventDefault();

  const title    = document.getElementById('postTitle').value.trim();
  const category = document.getElementById('postCategory').value;
  const content  = document.getElementById('postContent').value.trim();

  if (!title) { showToast('请填写帖子标题', 'error'); return; }
  if (!content) { showToast('请填写帖子内容', 'error'); return; }

  const payload = {
    title,
    content,
    category: category || null,
    // FIXME: Replace hardcoded authorId with the authenticated user's ID once
    // user authentication (e.g. Spring Security + JWT) is implemented.
    // Using authorId=1 here is for demo/development purposes only and MUST
    // NOT be deployed to production without a real auth layer.
    authorId: 1,
  };

  try {
    await apiFetch(`${API_BASE}/posts`, {
      method: 'POST',
      body: JSON.stringify(payload),
    });
    showToast('帖子发布成功！', 'success');
    closeModal();
    searchKeyword = '';
    searchInput.value = '';
    await loadPosts(0);
    await loadStats();
  } catch (err) {
    showToast(`发布失败：${err.message}`, 'error');
  }
});

/* ─────────────────────────────────────────────────────────
   Hero Buttons
───────────────────────────────────────────────────────── */
enterForumBtn.addEventListener('click', () => {
  document.querySelector('.posts-section').scrollIntoView({ behavior: 'smooth' });
});

registerBtn.addEventListener('click', () => {
  showToast('注册功能即将上线，敬请期待！', 'info');
});

/* ─────────────────────────────────────────────────────────
   Init
───────────────────────────────────────────────────────── */
(async function init() {
  await Promise.all([loadStats(), loadPosts(0)]);
})();
