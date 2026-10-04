const fs = require('node:fs');
const path = require('node:path');
const { spawn } = require('node:child_process');
const vscode = require('vscode');
const { LanguageClient } = require('vscode-languageclient/node');

let client;
let output;

function resolveSettingPath(configuredPath) {
  if (!configuredPath) return undefined;
  const expandedPath = configuredPath.startsWith('~')
    ? path.join(require('node:os').homedir(), configuredPath.slice(1))
    : configuredPath;
  if (path.isAbsolute(expandedPath)) return expandedPath;
  const workspace = vscode.workspace.workspaceFolders?.[0];
  if (!workspace) return undefined;
  return path.resolve(workspace.uri.fsPath, expandedPath);
}

function getJarPath() {
  const configuredPath = vscode.workspace.getConfiguration('ezcript').get('serverJar');
  return resolveSettingPath(configuredPath);
}

function getJavaPath() {
  return vscode.workspace.getConfiguration('ezcript').get('javaPath', 'java');
}

function showJarSetupMessage(message) {
  output.appendLine(message);
  void vscode.window.showWarningMessage(
    `${message} Configure Ezcript: Server Jar in Settings to enable IntelliSense.`,
    'Open Settings',
  ).then((selection) => {
    if (selection === 'Open Settings') {
      return vscode.commands.executeCommand(
        'workbench.action.openSettings',
        '@ext:ezcript.ezcript-vscode serverJar',
      );
    }
    return undefined;
  });
}

async function startLanguageClient() {
  if (client) {
    await client.stop();
    client = undefined;
  }

  const jar = getJarPath();
  if (!jar) {
    showJarSetupMessage('Open a workspace folder and configure the Ezcript plugin jar.');
    return false;
  }
  if (!fs.existsSync(jar) || !fs.statSync(jar).isFile()) {
    showJarSetupMessage(`Ezcript plugin jar not found: ${jar}`);
    return false;
  }

  const serverOptions = {
    command: getJavaPath(),
    args: ['-jar', jar, 'lsp'],
    options: {
      cwd: vscode.workspace.workspaceFolders?.[0]?.uri.fsPath,
    },
  };
  const clientOptions = {
    documentSelector: [{ scheme: 'file', language: 'ezcript' }],
    outputChannel: output,
  };

  client = new LanguageClient('ezcript', 'Ezcript Language Server', serverOptions, clientOptions);
  try {
    await client.start();
    output.appendLine(`Ezcript language server started with ${jar}`);
    return true;
  } catch (error) {
    client = undefined;
    const detail = error instanceof Error ? error.message : String(error);
    output.appendLine(`Could not start the Ezcript language server: ${detail}`);
    void vscode.window.showErrorMessage(
      `Could not start the Ezcript language server. Check Java and the Ezcript output channel.`,
    );
    return false;
  }
}

async function lintCurrentFile() {
  const document = vscode.window.activeTextEditor?.document;
  if (!document || document.languageId !== 'ezcript') {
    void vscode.window.showInformationMessage('Open an .ez file to lint it.');
    return;
  }
  if (!(await document.save())) {
    void vscode.window.showWarningMessage('Save the .ez file before linting it.');
    return;
  }

  const jar = getJarPath();
  if (!jar || !fs.existsSync(jar)) {
    showJarSetupMessage('Ezcript plugin jar not found.');
    return;
  }

  const process = spawn(getJavaPath(), ['-jar', jar, 'lint', document.uri.fsPath], {
    cwd: vscode.workspace.workspaceFolders?.[0]?.uri.fsPath,
    windowsHide: true,
  });
  let stdout = '';
  let stderr = '';
  process.stdout.setEncoding('utf8').on('data', (chunk) => { stdout += chunk; });
  process.stderr.setEncoding('utf8').on('data', (chunk) => { stderr += chunk; });
  process.on('error', (error) => {
    output.appendLine(`Could not run the Ezcript linter: ${error.message}`);
    void vscode.window.showErrorMessage(`Could not run Java: ${error.message}`);
  });
  process.on('close', (code) => {
    const result = `${stdout}${stderr}`.trim();
    if (result) output.appendLine(result);
    if (code === 0) {
      void vscode.window.showInformationMessage('Ezcript lint: no issues found.');
    } else if (code !== null) {
      void vscode.window.showWarningMessage(`Ezcript lint found issues (exit code ${code}). See the Ezcript output channel.`);
    }
  });
}

function activate(context) {
  output = vscode.window.createOutputChannel('Ezcript');
  context.subscriptions.push(
    output,
    vscode.commands.registerCommand('ezcript.restartLanguageServer', startLanguageClient),
    vscode.commands.registerCommand('ezcript.lintCurrentFile', lintCurrentFile),
    vscode.workspace.onDidChangeConfiguration((event) => {
      if (event.affectsConfiguration('ezcript.serverJar') || event.affectsConfiguration('ezcript.javaPath')) {
        void startLanguageClient();
      }
    }),
  );
  void startLanguageClient();
}

async function deactivate() {
  if (client) {
    await client.stop();
    client = undefined;
  }
}

module.exports = { activate, deactivate };
