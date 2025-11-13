; Script de Instalação do SIHCP - Sistema de Histórico e Coleta Patrimonial
; Criado com Inno Setup 6.x
; https://jrsoftware.org/isinfo.php

#define MyAppName "SIHCP - Sistema de Inventário"
#define MyAppVersion "1.0.0"
#define MyAppPublisher "Instituto Federal de Mato Grosso - IFMT"
#define MyAppURL "https://ifmt.edu.br"
#define MyAppExeName "SIHCP-Inventario.exe"
#define MyAppJarName "sistema-inventario.jar"

[Setup]
; Informações básicas
AppId={{A1B2C3D4-E5F6-7890-ABCD-EF1234567890}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}

; Diretórios
DefaultDirName={autopf}\SIHCP-Inventario
DefaultGroupName={#MyAppName}
DisableProgramGroupPage=yes

; Saída
OutputDir=..\..\dist
OutputBaseFilename=SIHCP-Inventario-Setup-{#MyAppVersion}
SetupIconFile=icon.ico
Compression=lzma2/max
SolidCompression=yes

; Privilégios
PrivilegesRequired=admin
PrivilegesRequiredOverridesAllowed=dialog

; Interface
WizardStyle=modern
WizardImageFile=wizard-image.bmp
WizardSmallImageFile=wizard-small-image.bmp

; Idioma
ShowLanguageDialog=no

[Languages]
Name: "brazilianportuguese"; MessagesFile: "compiler:Languages\BrazilianPortuguese.isl"

[Tasks]
Name: "desktopicon"; Description: "Criar atalho na Área de Trabalho"; GroupDescription: "Atalhos:"
Name: "quicklaunchicon"; Description: "Criar atalho na Barra de Tarefas"; GroupDescription: "Atalhos:"; Flags: unchecked

[Files]
; Aplicação principal
Source: "..\..\target\{#MyAppJarName}"; DestDir: "{app}"; Flags: ignoreversion
Source: "..\..\lib\*"; DestDir: "{app}\lib"; Flags: ignoreversion recursesubdirs createallsubdirs

; Launcher Windows
Source: "launcher.exe"; DestDir: "{app}"; DestName: "{#MyAppExeName}"; Flags: ignoreversion

; Ícone
Source: "icon.ico"; DestDir: "{app}"; Flags: ignoreversion

; Documentação
Source: "..\..\DOCUMENTAÇÃO\*.md"; DestDir: "{app}\docs"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "..\..\README.md"; DestDir: "{app}"; Flags: ignoreversion isreadme

; Scripts SQL (opcional)
Source: "..\..\sql\*.sql"; DestDir: "{app}\sql"; Flags: ignoreversion recursesubdirs createallsubdirs

[Dirs]
; Criar diretório de configuração
Name: "{localappdata}\SIHCP-Inventario"; Permissions: users-full

[Icons]
; Menu Iniciar
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; IconFilename: "{app}\icon.ico"
Name: "{group}\Configurar Banco de Dados"; Filename: "{app}\{#MyAppExeName}"; Parameters: "--config"; IconFilename: "{app}\icon.ico"
Name: "{group}\Documentação"; Filename: "{app}\docs"
Name: "{group}\Desinstalar {#MyAppName}"; Filename: "{uninstallexe}"

; Área de Trabalho
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; IconFilename: "{app}\icon.ico"; Tasks: desktopicon

; Barra de Tarefas
Name: "{userappdata}\Microsoft\Internet Explorer\Quick Launch\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; IconFilename: "{app}\icon.ico"; Tasks: quicklaunchicon

[Registry]
; Associar extensão .sihcp (opcional)
Root: HKCR; Subkey: ".sihcp"; ValueType: string; ValueName: ""; ValueData: "SIHCPFile"; Flags: uninsdeletevalue
Root: HKCR; Subkey: "SIHCPFile"; ValueType: string; ValueName: ""; ValueData: "Arquivo SIHCP"; Flags: uninsdeletekey
Root: HKCR; Subkey: "SIHCPFile\DefaultIcon"; ValueType: string; ValueName: ""; ValueData: "{app}\icon.ico"
Root: HKCR; Subkey: "SIHCPFile\shell\open\command"; ValueType: string; ValueName: ""; ValueData: """{app}\{#MyAppExeName}"" ""%1"""

; Adicionar ao PATH (opcional)
Root: HKLM; Subkey: "SYSTEM\CurrentControlSet\Control\Session Manager\Environment"; ValueType: expandsz; ValueName: "Path"; ValueData: "{olddata};{app}"; Check: NeedsAddPath('{app}')

[Run]
; Verificar Java
Filename: "{cmd}"; Parameters: "/c java -version"; StatusMsg: "Verificando instalação do Java..."; Flags: runhidden waituntilterminated

; Executar aplicação após instalação
Filename: "{app}\{#MyAppExeName}"; Description: "Executar {#MyAppName}"; Flags: nowait postinstall skipifsilent

[UninstallDelete]
; Limpar configurações (opcional - perguntar ao usuário)
Type: filesandordirs; Name: "{localappdata}\SIHCP-Inventario"

[Code]
var
  JavaInstalled: Boolean;
  ConfigPage: TInputQueryWizardPage;
  
// Verificar se Java está instalado
function IsJavaInstalled: Boolean;
var
  ResultCode: Integer;
begin
  Result := Exec('java', '-version', '', SW_HIDE, ewWaitUntilTerminated, ResultCode);
  if Result then
    Result := (ResultCode = 0);
end;

// Verificar se precisa adicionar ao PATH
function NeedsAddPath(Param: string): Boolean;
var
  OrigPath: string;
begin
  if not RegQueryStringValue(HKLM, 'SYSTEM\CurrentControlSet\Control\Session Manager\Environment', 'Path', OrigPath) then
  begin
    Result := True;
    exit;
  end;
  Result := Pos(';' + Param + ';', ';' + OrigPath + ';') = 0;
end;

// Página de configuração inicial
procedure InitializeWizard;
begin
  // Verificar Java
  JavaInstalled := IsJavaInstalled;
  
  if not JavaInstalled then
  begin
    MsgBox('Java não foi detectado no sistema.' + #13#10 + #13#10 +
           'O SIHCP requer Java 21 ou superior para funcionar.' + #13#10 + #13#10 +
           'Por favor, instale o Java antes de continuar.' + #13#10 +
           'Download: https://adoptium.net/', 
           mbError, MB_OK);
  end;
  
  // Criar página de configuração
  ConfigPage := CreateInputQueryPage(wpSelectDir,
    'Configuração Inicial', 
    'Deseja configurar o banco de dados agora?',
    'Você pode configurar o banco de dados agora ou depois através do menu da aplicação.');
    
  ConfigPage.Add('Servidor do Banco:', False);
  ConfigPage.Add('Porta:', False);
  ConfigPage.Add('Nome do Banco:', False);
  ConfigPage.Add('Usuário:', False);
  ConfigPage.Add('Senha:', True);
  
  // Valores padrão
  ConfigPage.Values[0] := 'localhost';
  ConfigPage.Values[1] := '5432';
  ConfigPage.Values[2] := 'sispatrimonio';
  ConfigPage.Values[3] := 'postgres';
  ConfigPage.Values[4] := '';
end;

// Validar entrada
function NextButtonClick(CurPageID: Integer): Boolean;
begin
  Result := True;
  
  if CurPageID = ConfigPage.ID then
  begin
    // Validar campos se usuário preencheu
    if (ConfigPage.Values[0] <> '') and (ConfigPage.Values[4] = '') then
    begin
      MsgBox('Por favor, preencha a senha do banco de dados.', mbError, MB_OK);
      Result := False;
    end;
  end;
end;

// Salvar configuração após instalação
procedure CurStepChanged(CurStep: TSetupStep);
var
  ConfigFile: string;
  ConfigContent: TStringList;
begin
  if CurStep = ssPostInstall then
  begin
    // Se usuário configurou banco, salvar
    if ConfigPage.Values[0] <> '' then
    begin
      ConfigFile := ExpandConstant('{localappdata}\SIHCP-Inventario\database-config.properties');
      ConfigContent := TStringList.Create;
      try
        ConfigContent.Add('# Database Configuration - Created by installer');
        ConfigContent.Add('host=' + ConfigPage.Values[0]);
        ConfigContent.Add('port=' + ConfigPage.Values[1]);
        ConfigContent.Add('database=' + ConfigPage.Values[2]);
        ConfigContent.Add('username=' + ConfigPage.Values[3]);
        ConfigContent.Add('password=' + ConfigPage.Values[4]);
        
        ForceDirectories(ExtractFileDir(ConfigFile));
        ConfigContent.SaveToFile(ConfigFile);
        
        MsgBox('Configuração salva com sucesso!' + #13#10 + #13#10 +
               'Localização: ' + ConfigFile, 
               mbInformation, MB_OK);
      finally
        ConfigContent.Free;
      end;
    end;
  end;
end;

// Mensagem de desinstalação
function InitializeUninstall(): Boolean;
begin
  Result := True;
  
  if MsgBox('Deseja remover também as configurações do banco de dados?' + #13#10 + #13#10 +
            'Se você pretende reinstalar, mantenha as configurações.',
            mbConfirmation, MB_YESNO) = IDNO then
  begin
    // Não remover configurações
    Result := True;
  end;
end;
