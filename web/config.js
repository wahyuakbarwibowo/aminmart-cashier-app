// ===========================================
// Google Analytics 4 Configuration
// ===========================================
const GA_MEASUREMENT_ID = 'G-ZD1ZD71PCH'; // Ganti dengan Measurement ID kamu

(function() {
  const script = document.createElement('script');
  script.async = true;
  script.src = `https://www.googletagmanager.com/gtag/js?id=${GA_MEASUREMENT_ID}`;
  document.head.appendChild(script);

  window.dataLayer = window.dataLayer || [];
  function gtag(){dataLayer.push(arguments);}
  gtag('js', new Date());
  gtag('config', GA_MEASUREMENT_ID, {
    'cookie_domain': 'github.io',
    'cookie_path': '/cashier/'
  });
})();

// ===========================================
// Navbar Configuration
// ===========================================
const NAVBAR_CONFIG = {
  logo: {
    src: 'cashier-icon.png',
    alt: 'Logo',
    link: 'index.html'
  },
  brandName: 'Aminmart Cashier',
  links: [
    { href: 'index.html', text: 'Home', activeClass: 'text-brand-600' },
    { href: 'install.html', text: 'Instalasi', activeClass: 'text-brand-600' },
    { href: 'contact.html', text: 'Kontak', activeClass: 'text-brand-600' },
    { href: 'delete-account.html', text: 'Hapus Akun', activeClass: 'text-brand-600', specialClass: 'text-rose-500 hover:text-rose-700' }
  ],
  themeToggle: true
};

// ===========================================
// Navbar Component Renderer
// ===========================================
function renderNavbar(currentPage = '') {
  const navHTML = `
    <!-- Navigation -->
    <nav class="fixed w-full z-50 glass">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div class="flex justify-between h-16 items-center">
                <!-- Logo & Name with Link -->
                <a href="${NAVBAR_CONFIG.logo.link}" class="flex items-center gap-3 hover:opacity-80 transition">
                    <img src="${NAVBAR_CONFIG.logo.src}" alt="${NAVBAR_CONFIG.logo.alt}" class="w-8 h-8 rounded-lg shadow-sm">
                    <span class="font-extrabold text-xl tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-brand-600 to-pink-400">${NAVBAR_CONFIG.brandName}</span>
                </a>

                <!-- Desktop Menu -->
                <div class="hidden md:flex items-center space-x-8 font-semibold text-sm">
                    ${NAVBAR_CONFIG.links.map(link => {
                      const isActive = link.href === currentPage || (currentPage === '' && link.href === 'index.html');
                      let className = 'text-slate-600 hover:text-brand-600 dark:text-slate-400 dark:hover:text-pink-400 transition';

                      if (isActive) {
                        className = link.activeClass;
                      } else if (link.specialClass) {
                        className = link.specialClass + ' transition';
                      }

                      return `<a href="${link.href}" class="${className}">${link.text}</a>`;
                    }).join('')}
                    ${NAVBAR_CONFIG.themeToggle ? `
                    <button id="themeToggle" class="p-2 rounded-xl bg-white dark:bg-slate-900 border border-brand-100 dark:border-brand-900 shadow-sm transition-all hover:scale-105 active:scale-95">
                        <span id="themeToggleIcon">🌙</span>
                    </button>
                    ` : ''}
                </div>

                <!-- Mobile Header Right -->
                <div class="flex md:hidden items-center gap-4">
                    ${NAVBAR_CONFIG.themeToggle ? `
                    <button id="mobileThemeToggle" class="p-2 rounded-xl bg-white dark:bg-slate-900 border border-brand-100 dark:border-brand-900 shadow-sm transition-all">
                        <span class="mobileThemeToggleIcon text-sm">🌙</span>
                    </button>
                    ` : ''}
                    <!-- Hamburger Button -->
                    <button id="menuBtn" class="text-slate-600 dark:text-slate-300 focus:outline-none">
                        <svg class="w-7 h-7" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path id="menuIcon" stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M4 6h16M4 12h16m-7 6h7"></path>
                        </svg>
                    </button>
                </div>
            </div>

            <!-- Mobile Menu Panel -->
            <div id="mobileMenu" class="hidden md:hidden pb-6 pt-2 animate-in fade-in slide-in-from-top-4 duration-300">
                <div class="flex flex-col space-y-4 font-bold text-base">
                    ${NAVBAR_CONFIG.links.map(link => {
                      const isActive = link.href === currentPage || (currentPage === '' && link.href === 'index.html');
                      let className = 'text-slate-600 dark:text-slate-400 hover:text-brand-600 py-2 border-b border-brand-100 dark:border-brand-900/30';

                      if (isActive) {
                        className = link.activeClass + ' py-2 border-b border-brand-100 dark:border-brand-900/30';
                      } else if (link.specialClass) {
                        className = link.specialClass + ' py-2 border-b border-brand-100 dark:border-brand-900/30';
                      }

                      return `<a href="${link.href}" class="${className}">${link.text}</a>`;
                    }).join('')}
                </div>
            </div>
        </div>
    </nav>
  `;

  document.write(navHTML);
}

// ===========================================
// Theme Toggle Logic
// ===========================================
function initThemeToggle() {
  const themeToggle = document.getElementById('themeToggle');
  const mobileThemeToggle = document.getElementById('mobileThemeToggle');
  const html = document.documentElement;

  const toggleTheme = () => {
    html.classList.toggle('dark');
    const isDark = html.classList.contains('dark');
    localStorage.setItem('theme', isDark ? 'dark' : 'light');
    updateIcon(isDark);
  };

  if (themeToggle) themeToggle.addEventListener('click', toggleTheme);
  if (mobileThemeToggle) mobileThemeToggle.addEventListener('click', toggleTheme);

  function updateIcon(isDark) {
    const icon = isDark ? '☀️' : '🌙';
    const desktopIcon = document.getElementById('themeToggleIcon');
    const mobileIcon = document.querySelector('.mobileThemeToggleIcon');
    if (desktopIcon) desktopIcon.innerText = icon;
    if (mobileIcon) mobileIcon.innerText = icon;
  }

  // Initialize theme
  if (localStorage.theme === 'dark' || (!('theme' in localStorage) && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
    html.classList.add('dark');
    updateIcon(true);
  } else {
    html.classList.remove('dark');
    updateIcon(false);
  }
}

// ===========================================
// Mobile Menu Logic
// ===========================================
function initMobileMenu() {
  const menuBtn = document.getElementById('menuBtn');
  const mobileMenu = document.getElementById('mobileMenu');
  const menuIcon = document.getElementById('menuIcon');

  if (menuBtn && mobileMenu && menuIcon) {
    menuBtn.addEventListener('click', () => {
      mobileMenu.classList.toggle('hidden');
      const isOpen = !mobileMenu.classList.contains('hidden');

      if (isOpen) {
        menuIcon.setAttribute('d', 'M6 18L18 6M6 6l12 12');
      } else {
        menuIcon.setAttribute('d', 'M4 6h16M4 12h16m-7 6h7');
      }
    });
  }
}

// ===========================================
// Auto Initialize (if navbar-container exists)
// ===========================================
document.addEventListener('DOMContentLoaded', function() {
  const navbarContainer = document.getElementById('navbar-container');
  if (navbarContainer) {
    const currentPage = window.location.pathname.split('/').pop() || 'index.html';
    navbarContainer.innerHTML = '';

    // Re-render navbar using innerHTML instead of document.write
    const navHTML = `
    <!-- Navigation -->
    <nav class="fixed w-full z-50 glass">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div class="flex justify-between h-16 items-center">
                <!-- Logo & Name with Link -->
                <a href="${NAVBAR_CONFIG.logo.link}" class="flex items-center gap-3 hover:opacity-80 transition">
                    <img src="${NAVBAR_CONFIG.logo.src}" alt="${NAVBAR_CONFIG.logo.alt}" class="w-8 h-8 rounded-lg shadow-sm">
                    <span class="font-extrabold text-xl tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-brand-600 to-pink-400">${NAVBAR_CONFIG.brandName}</span>
                </a>

                <!-- Desktop Menu -->
                <div class="hidden md:flex items-center space-x-8 font-semibold text-sm">
                    ${NAVBAR_CONFIG.links.map(link => {
                      const isActive = link.href === currentPage;
                      let className = 'text-slate-600 hover:text-brand-600 dark:text-slate-400 dark:hover:text-pink-400 transition';

                      if (isActive) {
                        className = link.activeClass;
                      } else if (link.specialClass) {
                        className = link.specialClass + ' transition';
                      }

                      return `<a href="${link.href}" class="${className}">${link.text}</a>`;
                    }).join('')}
                    ${NAVBAR_CONFIG.themeToggle ? `
                    <button id="themeToggle" class="p-2 rounded-xl bg-white dark:bg-slate-900 border border-brand-100 dark:border-brand-900 shadow-sm transition-all hover:scale-105 active:scale-95">
                        <span id="themeToggleIcon">🌙</span>
                    </button>
                    ` : ''}
                </div>

                <!-- Mobile Header Right -->
                <div class="flex md:hidden items-center gap-4">
                    ${NAVBAR_CONFIG.themeToggle ? `
                    <button id="mobileThemeToggle" class="p-2 rounded-xl bg-white dark:bg-slate-900 border border-brand-100 dark:border-brand-900 shadow-sm transition-all">
                        <span class="mobileThemeToggleIcon text-sm">🌙</span>
                    </button>
                    ` : ''}
                    <!-- Hamburger Button -->
                    <button id="menuBtn" class="text-slate-600 dark:text-slate-300 focus:outline-none">
                        <svg class="w-7 h-7" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path id="menuIcon" stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M4 6h16M4 12h16m-7 6h7"></path>
                        </svg>
                    </button>
                </div>
            </div>

            <!-- Mobile Menu Panel -->
            <div id="mobileMenu" class="hidden md:hidden pb-6 pt-2 animate-in fade-in slide-in-from-top-4 duration-300">
                <div class="flex flex-col space-y-4 font-bold text-base">
                    ${NAVBAR_CONFIG.links.map(link => {
                      const isActive = link.href === currentPage;
                      let className = 'text-slate-600 dark:text-slate-400 hover:text-brand-600 py-2 border-b border-brand-100 dark:border-brand-900/30';

                      if (isActive) {
                        className = link.activeClass + ' py-2 border-b border-brand-100 dark:border-brand-900/30';
                      } else if (link.specialClass) {
                        className = link.specialClass + ' py-2 border-b border-brand-100 dark:border-brand-900/30';
                      }

                      return `<a href="${link.href}" class="${className}">${link.text}</a>`;
                    }).join('')}
                </div>
            </div>
        </div>
    </nav>
    `;

    navbarContainer.innerHTML = navHTML;
    initThemeToggle();
    initMobileMenu();
  }
});
