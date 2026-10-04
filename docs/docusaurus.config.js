import {themes as prismThemes} from 'prism-react-renderer';

const config = {
  title: 'Ezcript',
  tagline: 'Ruby scripts for Paper servers',
  url: 'https://ezcript.dev',
  baseUrl: '/',
  organizationName: 'ezcript',
  projectName: 'ezcript',
  onBrokenLinks: 'throw',
  markdown: {
    hooks: {
      onBrokenMarkdownLinks: 'warn',
    },
  },
  i18n: {
    defaultLocale: 'en',
    locales: ['en', 'es'],
  },
  presets: [
    [
      'classic',
      {
        docs: {
          sidebarPath: './sidebars.js',
          editUrl: undefined,
        },
        blog: false,
        theme: {
          customCss: './src/css/custom.css',
        },
      },
    ],
  ],
  themeConfig: {
    navbar: {
      title: 'Ezcript',
      items: [
        {type: 'docSidebar', sidebarId: 'tutorialSidebar', position: 'left', label: 'Guide'},
        {type: 'localeDropdown', position: 'right'},
      ],
    },
    prism: {
      theme: prismThemes.github,
      darkTheme: prismThemes.dracula,
    },
  },
};

export default config;
