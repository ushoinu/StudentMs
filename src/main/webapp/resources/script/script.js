/* ════════════════════════════════════════════════════════════════════
   script.js — Student Management System
   Features:
     1. Theme toggle (desktop + mobile, localStorage persist)
     2. Hamburger / mobile menu toggle (open, close, backdrop, ESC)
     3. Dynamic mobile menu top position (matches actual navbar height)
     4. Active navbar link highlight
     5. Navbar scroll blur effect
     6. Password visibility toggle
     7. Edit modal helpers (openEditModal / closeEditModal)
   ════════════════════════════════════════════════════════════════════ */

document.addEventListener('DOMContentLoaded', function () {

    /* ══════════════════════════════════════════════════════════════
       1. THEME TOGGLE
          - desktop button : #theme-toggle
          - mobile button  : #theme-toggle-mobile
          - localStorage key: 'students-theme'
       ══════════════════════════════════════════════════════════════ */
    var root           = document.documentElement;
    var themeBtn       = document.getElementById('theme-toggle');
    var themeBtnMobile = document.getElementById('theme-toggle-mobile');

    /**
     * Apply theme to <html data-theme="..."> and update button icons.
     * @param {string} theme - 'light' | 'dark'
     */
    function applyTheme(theme) {
        root.setAttribute('data-theme', theme);
        var icon  = theme === 'dark' ? '🌙' : '🌕';
        var label = theme === 'dark' ? 'Switch to light theme' : 'Switch to dark theme';

        if (themeBtn) {
            themeBtn.textContent = icon;
            themeBtn.setAttribute('aria-label', label);
        }
        if (themeBtnMobile) {
            themeBtnMobile.textContent = icon;
            themeBtnMobile.setAttribute('aria-label', label);
        }
    }

    /* Initial theme: stored value → OS preference → light */
    var stored      = localStorage.getItem('students-theme');
    var prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
    applyTheme(stored || (prefersDark ? 'dark' : 'light'));

    /* Toggle on click */
    function onThemeClick() {
        var next = root.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
        applyTheme(next);
        localStorage.setItem('students-theme', next);
    }

    if (themeBtn)       themeBtn.addEventListener('click', onThemeClick);
    if (themeBtnMobile) themeBtnMobile.addEventListener('click', onThemeClick);


    /* ══════════════════════════════════════════════════════════════
       2. HAMBURGER / MOBILE MENU TOGGLE
          Elements:
            #nav-hamburger   — ☰ / ✕ button
            #nav-mobile-menu — slide-down dropdown
            #nav-backdrop    — invisible overlay (click → close)
       ══════════════════════════════════════════════════════════════ */
    var hamburger  = document.getElementById('nav-hamburger');
    var mobileMenu = document.getElementById('nav-mobile-menu');
    var backdrop   = document.getElementById('nav-backdrop');
    var navMenu    = document.querySelector('.nav-Menu');   /* main bar */

    /**
     * Set mobile menu `top` dynamically so it always sits flush
     * below the actual navbar (handles different padding on breakpoints).
     */
    function syncMenuTop() {
        if (!navMenu || !mobileMenu) return;
        var navHeight = navMenu.getBoundingClientRect().height;
        mobileMenu.style.top = navHeight + 'px';
    }

    /** Open the mobile dropdown menu */
    function openMenu() {
        if (!hamburger || !mobileMenu) return;
        syncMenuTop();                                   /* align to navbar bottom */
        hamburger.classList.add('is-open');
        hamburger.setAttribute('aria-expanded', 'true');
        mobileMenu.classList.add('is-open');
        mobileMenu.setAttribute('aria-hidden', 'false');
        if (backdrop) backdrop.classList.add('is-open');
    }

    /** Close the mobile dropdown menu */
    function closeMenu() {
        if (!hamburger || !mobileMenu) return;
        hamburger.classList.remove('is-open');
        hamburger.setAttribute('aria-expanded', 'false');
        mobileMenu.classList.remove('is-open');
        mobileMenu.setAttribute('aria-hidden', 'true');
        if (backdrop) backdrop.classList.remove('is-open');
    }

    /** Toggle open/close */
    function toggleMenu() {
        if (hamburger && hamburger.classList.contains('is-open')) {
            closeMenu();
        } else {
            openMenu();
        }
    }

    /* Hamburger click */
    if (hamburger) {
        hamburger.addEventListener('click', function (e) {
            e.stopPropagation();   /* backdrop click 이벤트 방지 */
            toggleMenu();
        });
    }

    /* Backdrop click → close */
    if (backdrop) {
        backdrop.addEventListener('click', closeMenu);
    }

    /* Click on any link or button inside mobile menu → close (with small delay) */
    if (mobileMenu) {
        mobileMenu.querySelectorAll('a, button').forEach(function (el) {
            el.addEventListener('click', function () {
                /* Small delay lets JSF navigation start before menu hides */
                setTimeout(closeMenu, 120);
            });
        });
    }

    /* ESC key → close menu (and edit modal if open) */
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') {
            closeMenu();
            closeEditModal();
        }
    });

    /* Resize → close menu if screen becomes ≥768px (tablet/desktop) */
    window.addEventListener('resize', function () {
        if (window.innerWidth >= 768) {
            closeMenu();
        }
        /* Re-sync top in case layout shifted */
        syncMenuTop();
    });


    /* ══════════════════════════════════════════════════════════════
       3. ACTIVE NAVBAR LINK
          Compares current URL path to each link's href.
          Applies to desktop (.nav-list a) and mobile (.nav-mobile-menu a).
       ══════════════════════════════════════════════════════════════ */
    var currentPage = window.location.pathname;

    document.querySelectorAll('.nav-list a, .nav-mobile-menu a').forEach(function (link) {
        var href    = link.getAttribute('href') || '';
        var segment = href.split('/').pop().split('?')[0];
        if (segment && currentPage.includes(segment)) {
            link.classList.add('active');
        }
    });


    /* ══════════════════════════════════════════════════════════════
       4. NAVBAR SCROLL BLUR EFFECT
          Adds backdrop-filter blur after scrolling 10px.
       ══════════════════════════════════════════════════════════════ */
    var navBar = document.querySelector('.nav-bar');

    function handleScroll() {
        if (!navBar) return;
        navBar.style.backdropFilter = window.scrollY > 10
            ? 'blur(12px)'
            : 'blur(0px)';
    }

    window.addEventListener('scroll', handleScroll, { passive: true });
    handleScroll();   /* run once on load */


    /* ══════════════════════════════════════════════════════════════
       5. PASSWORD VISIBILITY TOGGLE
          .password-toggle button এ click করলে input type বদলায়।
          aria-pressed toggle দিয়ে eye/eye-off icon swap হয় (CSS এ)।
       ══════════════════════════════════════════════════════════════ */
    document.querySelectorAll('.password-toggle').forEach(function (button) {
        button.addEventListener('click', function () {
            var wrapper = button.closest('.password-field');
            if (!wrapper) return;
            var input = wrapper.querySelector('input');
            if (!input) return;

            var isHidden = input.type === 'password';
            input.type = isHidden ? 'text' : 'password';
            button.setAttribute('aria-pressed', String(isHidden));
            button.setAttribute('aria-label', isHidden ? 'Hide password' : 'Show password');
        });
    });


    /* ══════════════════════════════════════════════════════════════
       6. EDIT MODAL — close on backdrop click
          (editModalOverlay এর বাইরে click করলে modal বন্ধ হবে)
       ══════════════════════════════════════════════════════════════ */
    var editOverlay = document.getElementById('editModalOverlay');
    if (editOverlay) {
        editOverlay.addEventListener('click', function (e) {
            if (e.target === editOverlay) closeEditModal();
        });
    }

});   /* end DOMContentLoaded */


/* ════════════════════════════════════════════════════════════════════
   EDIT MODAL HELPERS
   (Global scope — called via JSF f:ajax onevent attributes)
   ════════════════════════════════════════════════════════════════════ */

/** Open the edit modal overlay */
function openEditModal() {
    var overlay = document.getElementById('editModalOverlay');
    if (overlay) overlay.classList.add('show');
}

/** Close the edit modal overlay */
function closeEditModal() {
    var overlay = document.getElementById('editModalOverlay');
    if (overlay) overlay.classList.remove('show');
}

/**
 * f:ajax onevent handler — "Edit" button
 * @param {Object} data - JSF ajax event data
 */
function handleEditAjax(data) {
    if (data.status === 'success') openEditModal();
}

/**
 * f:ajax onevent handler — "Update" button
 * @param {Object} data - JSF ajax event data
 */
function handleUpdateAjax(data) {
    if (data.status === 'success') closeEditModal();
}