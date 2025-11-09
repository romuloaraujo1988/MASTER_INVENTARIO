<img src="./01patec1.png"
style="width:2.39514in;height:0.42986in" />asasasa

> Guia de Orientações para Geração dosArquivos para implantação do SIADS
>
> **Guia** **de** **Orientações** **Gerais** **para**
>
> **Geração** **dos** **Arquivos** **de** **Implantação**
>
> **Dezembro/2024**
>
> 1

<img src="./4spwbbtz.png"
style="width:2.39514in;height:0.42986in" />asasasa

> Guia de Orientações para Geração dosArquivos para implantação do SIADS
>
> **HISTÓRICO** **DE** **VERSÕES**

||
||
||
||
||
||
||

> 2

<img src="./uxnqrhvr.png"
style="width:2.39514in;height:0.42986in" />asasasa

> Guia de Orientações para Geração dosArquivos para implantação do SIADS
>
> **Sumário**
>
> 1\.
> OBJETIVO........................................................................................................................................4
>
> 2\. REGRASGERAIS PARAGERAÇÃO DOSARQUIVOS
> ...................................................................5
>
> 3\. LEIAUTE
> DOSARQUIVOS...............................................................................................................6
>
> 3

<img src="./vyuspzqb.png"
style="width:2.39514in;height:0.42986in" />asasasa

> Guia de Orientações para Geração dosArquivos para implantação do SIADS
>
> **1.** **OBJETIVO**
>
> O módulo de Implantação do SIADS foi desenvolvido para agilizar a
> adoção do sistema
>
> pelos diversos entes da administração pública brasileira. Entre suas
> principais
>
> funcionalidades, destaca-se o mapeamento das informações do sistema
> atual do ente público
>
> para os dados do SIADS, realizando uma correspondência (“de-para”)
> entre os dois sistemas.
>
> Esse processo assegura a transferência de dados relevantes e
> necessários para a
>
> implantação no SIADS.
>
> Para viabilizar esse mapeamento, o módulo de implantação necessita das
> informações
>
> do sistema de origem, que devem ser importados em arquivos no
> **<u>formato texto simples</u>**.
>
> Este documento tem como objetivo fornecer as orientações necessárias
> para a correta
>
> geração desses arquivos.
>
> O documento abrange quatro tipos de arquivos para importação:
> **UOrg,** **Material** **de**
>
> **Consumo,** **Material** **Permanente** **e** **Material**
> **Intangível.**
>
> **UOrg**: tem como finalidade a importação dos dados das Unidades
> Organizacionais;
>
> **Material** **de** **Consumo**: tem como finalidade a importação dos
> dados dos Bens de
>
> Consumo (Estoque/Almoxarifado);
>
> **Material** **Permanente**: tem como finalidade a importação dos
> dados dos Bens
>
> Permanentes (Patrimônio) ;
>
> **MaterialIntangível**:temcomofinalidadeaimportaçãodos dados dos Bens
> Intangíveis.
>
> Aseção 3 detalha o leiaute de cada um desses arquivos.
>
> 4

<img src="./yxy1j3fi.png"
style="width:2.39514in;height:0.42986in" />asasasa

> Guia de Orientações para Geração dosArquivos para implantação do SIADS
>
> **2.** **REGRASGERAIS** **PARAGERAÇÃODOSARQUIVOS**
>
> **<u>Formato e Delimitadores</u>**
>
> O arquivo de importação deve ser um arquivo-texto no formato CSV onde
> cada linha
>
> representa um registro. A codificação utilizada deverá ser UTF-8.
> Recomenda-se evitar o uso
>
> de editores de texto como Word e Libreoffice, pois os mesmos
> acrescentam caracteres de
>
> controle no texto que podem causar erro na validação e importação do
> arquivo.
>
> Os campos devem ser separados pelo caractere delimitador “¥” (símbolo
> do iene, moeda

japonesa). Paracampos opcionais emque não houver informaçãoa ser
preenchida, deve-se incluir

apenas os delimitadores de início e fim de campo: “¥¥”. Cada linha deve
ser finalizada com o

caractere delimitador “£” (símbolo da libra esterlina). Esses caracteres
delimitadores não devem

compor o conteúdo de nenhum campo do arquivo.

> **<u>Estrutura do Arquivo</u>**
>
> Oarquivo deverá ser estruturado conforme exemplo abaixo.Aprimeira
> linha deverá ser
>
> um registro do tipo *Header*, em seguida deverão vir N(uma ou mais)
> linhas de
>
> detalhamento(*Detail*) onde cada linha representa um registro a ser
> inserido/atualizado
>
> (identificador da linha “D”) ou deletado (identificador da linha “E”)
> no SIADS Implantação. O
>
> arquivodeveráser
> finalizadocomumregistrodotipo*Trailer*.Cabedestacarquenesseregistro
>
> Trailer o último campo deverá constar de modo fixo a palavra FIM
> seguido do delimitador “£”
>
> que indica o final da linha.
>
> O sistema não permite linhas em branco ou repetidas. **EXEMPLO**
> **DEARQUIVO**
>
> H¥UG¥1¥25000¥02146445459¥£ ====\> Registro do tipo *Header*
> D¥179001¥NOME DAUG 01¥£ =====\|
>
> D¥179002¥NOME DAUG 02¥£ ======\> Registros do tipo *Detail*
> D¥179003¥NOMEDAUG 03¥£ =====\|
>
> E¥179004¥£ ===================\| T¥12042017104732¥3¥FIM¥£ =======\>
> Registro do tipo *Tralier*
>
> 5

<img src="./jzb4swth.png"
style="width:2.39514in;height:0.42986in" />asasasa

> Guia de Orientações para Geração dosArquivos para implantação do SIADS
>
> **3.** **LEIAUTE** **DOSARQUIVOS**
>
> **<u>ARQUIVO DE UORG:</u>**
>
> **LINHADE** **CABEÇALHO**

||
||
||
||
||
||
||
||
||
||

> Exemplo: H¥UO¥1¥25000¥170531¥84480343172¥£
>
> **LINHADEDADOS** **PARAINCLUSÃO**

||
||
||
||
||
||
||
||
||
||
||
||
||
||
||
||
||
||
||
||
||
||
||
||

> 6

<img src="./rz24bse2.png"
style="width:2.39514in;height:0.42986in" />asasasa

> Guia de Orientações para Geração dosArquivos para implantação do SIADS
>
> Exemplo:
> D¥00041001¥170531¥NOME¥SIGLA¥ENDERECO¥CEP8CEP8¥PAIS¥TELEFON8¥RAM4¥99999999999¥NOMERESPONSAVEL¥99
> 9999999999¥NUMEROPORTARIANOMEACAO¥999999¥NOMEREDUZIDO¥11122010¥NUMERODOCUMENTOCRIACAO¥CE¥FOR
> TALEZA¥EMAIL¥999999¥SIM¥£
>
> **LINHADEDADOS** **PARAEXCLUSÃO**

||
||
||
||
||
||

> Exemplo: E¥10426¥£
>
> **LINHADE** **RODAPÉ**

||
||
||
||
||
||
||
||

> Exemplo: T¥12042017104732¥3¥FIM¥£
>
> **EXEMPLO** **DEARQUIVO** **DE** **UORG**
>
> H¥UO¥4¥25000¥02146445459¥£
> D¥00041001¥170531¥NOME¥SIGLA¥ENDERECO¥CEP8CEP8¥PAIS¥TELEFON8¥RAM4¥99999999999¥NOMERESPONSAVEL¥9
> 99999999999¥NUMEROPORTARIANOMEACAO¥999999¥NOMEREDUZIDO¥11122010¥NUMERODOCUMENTOCRIACAO¥CE¥FO
> RTALEZA¥EMAIL¥999999¥SIM¥£
>
> T¥12042019104732¥1¥FIM¥£
>
> **<u>ARQUIVO DE MATERIAL DE CONSUMO:</u>**
>
> **LINHADE** **CABEÇALHO**

||
||
||
||
||
||
||
||
||
||
||

> Exemplo: H¥CO¥1¥25000¥00001¥36899038315¥00001¥£
>
> 7

<img src="./gsekyqnt.png"
style="width:2.39514in;height:0.42986in" />asasasa

> Guia de Orientações para Geração dosArquivos para implantação do SIADS
>
> **LINHADE** **DADOS**

||
||
||
||
||
||
||
||
||
||
||
||
||
||

> Exemplo: D¥AB99999¥C2805006045¥ VALVULA DE ADMISSAODO
> MOTOR¥UN¥115610139¥PA60T0000¥179014¥40000¥FALSE¥£
>
> **LINHADEDADOS** **PARAEXCLUSÃO**

||
||
||
||
||
||
||

> Exemplo: E¥C2805006045¥UN¥£
>
> **LINHADE** **RODAPÉ**

||
||
||
||
||
||
||
||
||
||

> Exemplo: T¥04052017083540¥4¥1974¥13000¥FIM£
>
> 8

<img src="./kdeqz0f1.png"
style="width:2.39514in;height:0.42986in" />asasasa

> Guia de Orientações para Geração dosArquivos para implantação do SIADS
>
> **EXEMPLO** **DEARQUIVO** **DE** **MATERIAL** **DE** **CONSUMO**
>
> H¥CO¥1¥25000¥00001¥36899038315¥00001¥£
>
> D¥AB99999¥C2805006045¥ VALVULA DE ADMISSAODO
> MOTOR¥UN¥115610139¥PA60T0000¥179014¥40000¥FALSE¥£
> D¥AA999998¥S0772004815¥SERVIÇO TREINAMENTO
> DRAYTEC¥UN¥0000349039¥PA65G0010¥179014¥15¥5000¥FALSE¥£
> E¥C8923524070¥UN¥£
>
> T¥04052017083540¥4¥1974¥13000¥FIM¥£
>
> **<u>ARQUIVO DE MATERIALPERMANENTE:</u>**
>
> **LINHADE** **CABEÇALHO**

||
||
||
||
||
||
||
||
||
||
||

> Exemplo: H¥PE¥1¥25000¥170531¥02146445459¥00001¥£
>
> **LINHADE** **DADOS**

||
||
||
||
||
||
||
||
||
||
||
||
||
||
||
||
||
||
||
||
||

> 9

<img src="./zsovx0hg.png"
style="width:2.39514in;height:0.42986in" />asasasa

> Guia de Orientações para Geração dosArquivos para implantação do SIADS
>
> Exemplo: D¥P101479014¥MATERIAL
> PERMANENTE01¥254602152¥ENDERECO¥1790001¥1¥2¥3¥11052017¥10000¥MERCADO
> XPTO¥ESPECIFICACAO¥11052018¥9000000¥1234567891¥MARCA¥MODELO¥FABRICANTE¥GARANTIDOR¥CONTRATO¥1105
> 2017¥11052018¥02146445459¥ANTONIO¥FALSE¥01012019¥999¥12¥£
>
> **LINHADEDADOS** **PARAEXCLUSÃO** **DE** **NÚMERO** **PATRIMONIAL**

||
||
||
||
||
||
||

> Exemplo: E¥P101479014¥1234567891¥£
>
> **Observação:** **neste** **caso** **o** **bem** **com**
> **patrimônio** **1234567891** **será** **excluído.**
>
> **LINHADEDADOS** **PARAEXCLUSÃO** **DE** **MATERIAL**

||
||
||
||
||
||

> Exemplo: E¥P101479018¥£
>
> **Observação:** **neste** **caso** **o** **material** **P101479018**
> **e** **todos** **seus** **bens** **serão** **excluídos.**
>
> **LINHADE** **RODAPÉ**

||
||
||
||
||

> 10

<img src="./ell0wayq.png"
style="width:2.39514in;height:0.42986in" />asasasa

> Guia de Orientações para Geração dosArquivos para implantação do SIADS
>
> Exemplo: T¥15052017185412¥3¥1000¥FIM¥£
>
> **EXEMPLO** **DEARQUIVO** **DE** **MATERIAL** **PERMANENTE**
>
> H¥PE¥1¥25000¥00001¥02146445459¥00001¥£
> D¥P101479014¥MATERIALPERMANENTE01¥254602152¥ENDERECO¥1790001¥1¥2¥3¥11052017¥10000¥MERCADOXPTO¥ESPECIFICACAO¥1105201
> 8¥9000000¥1234567891¥MARCA¥MODELO¥FABRICANTE¥GARANTIDOR¥CONTRATO¥11052017¥11052018¥02146445459¥ANTONIO¥¥¥¥¥£
> D¥P101479015¥MATERIALPERMANENTE02¥254602153¥ENDERECO¥1790002¥2¥2¥1¥11052018¥20000¥AQUISICAO¥ESPECIFICACAO¥¥¥123456789
> 2¥¥¥¥¥¥¥¥¥¥¥¥¥¥£
> D¥P101479016¥MATERIALPERMANENTE03¥254602154¥ENDERECO¥1790003¥1¥2¥2¥11052019¥10000¥AQUISICAO¥ESPECIFICACAO¥¥¥123456789
> 3¥¥¥¥¥¥¥¥¥¥¥¥¥¥£
>
> **<u>ARQUIVO DE MATERIALINTANGÍVEL:</u>**
>
> **LINHADE** **CABEÇALHO**

||
||
||
||
||
||
||
||
||
||
||

> Exemplo: H¥IN¥1¥25000¥170531¥02146445459¥00001¥£
>
> **LINHADE** **DADOS**

||
||
||
||
||
||
||
||
||
||
||
||
||
||

> 11

<img src="./xraauvc0.png"
style="width:2.39514in;height:0.42986in" />asasasa

> Guia de Orientações para Geração dosArquivos para implantação do SIADS
>
> Exemplo: D¥P101479014¥MATERIAL
> INTANGIVEL01¥254602152¥ENDERECO¥1790001¥1¥2¥3¥11052017¥10000¥MERCADO
> XPTO¥ESPECIFICACAO¥11052018¥9000000¥1234567891¥MARCA¥MODELO¥FABRICANTE¥GARANTIDOR¥CONTRATO¥1105
> 2017¥11052018¥02146445459¥ANTONIO¥FALSE¥01012019¥999¥12¥£
>
> **LINHADEDADOS** **PARA** **EXCLUSÃO** **DE** **NÚMERO**
> **PATRIMONIAL**

||
||
||
||
||
||
||

> Exemplo: E¥P101479014¥1234567891¥£
>
> **Observação:** **neste** **caso** **o** **bem** **com**
> **patrimônio** **1234567891** **será** **excluído.**
>
> **LINHADEDADOS** **PARA** **EXCLUSÃO** **DE** **MATERIAL**

||
||
||
||
||
||

> Exemplo: E¥P101479018¥£
>
> **Observação:** **neste** **caso** **o** **material** **P101479018**
> **e** **todos** **seus** **bens** **serão** **excluídos.**
>
> **LINHADE** **RODAPÉ**

||
||
||

> 12

<img src="./y1wbcm4h.png"
style="width:2.39514in;height:0.42986in" />asasasa

> Guia de Orientações para Geração dosArquivos para implantação do SIADS
>
> Exemplo: T¥15052017185412¥3¥1000¥FIM¥£
>
> **EXEMPLO** **DEARQUIVO** **DE** **MATERIAL** **INTANGIVEL**
>
> H¥PE¥1¥25000¥00001¥02146445459¥00001¥£
> D¥P101479014¥MATERIALINTANGIVEL01¥254602152¥ENDERECO¥1790001¥1¥2¥3¥11052017¥10000¥MERCADOXPTO¥ESPECIFICACAO¥11052018¥
> 9000000¥1234567891¥MARCA¥MODELO¥FABRICANTE¥GARANTIDOR¥CONTRATO¥11052017¥11052018¥02146445459¥ANTONIO¥¥¥¥¥£
> D¥P101479015¥MATERIALINTANGIVEL02¥254602153¥ENDERECO¥1790002¥2¥2¥1¥11052018¥20000¥AQUISICAO¥ESPECIFICACAO¥¥¥1234567892¥
> ¥¥¥¥¥¥¥¥¥¥¥¥¥£
> D¥P101479016¥MATERIALINTANGIVEL03¥254602154¥ENDERECO¥1790003¥1¥2¥2¥11052019¥10000¥AQUISICAO¥ESPECIFICACAO¥¥¥1234567893¥
> ¥¥¥¥¥¥¥¥¥¥¥¥¥£
>
> T¥15042019185412¥3¥40000¥FIM¥£
>
> 13
