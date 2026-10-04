import React from 'react';
import Link from '@docusaurus/Link';
import Layout from '@theme/Layout';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';

export default function Home() {
  const {i18n} = useDocusaurusContext();
  const spanish = i18n.currentLocale === 'es';

  return (
    <Layout title="Ezcript" description="Ruby scripts for Paper servers">
      <main className="container margin-vert--lg">
        <h1>Ezcript</h1>
        <p>
          {spanish
            ? 'Crea funciones, minijuegos y modalidades para Paper con scripts Ruby.'
            : 'Create Paper server features, minigames, and game modes with Ruby scripts.'}
        </p>
        <Link className="button button--primary button--lg" to="/docs/">
          {spanish ? 'Abrir la guía' : 'Read the guide'}
        </Link>
      </main>
    </Layout>
  );
}
