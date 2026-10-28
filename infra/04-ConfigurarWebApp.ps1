# Ajusta a configuracao geral do Web App: Always On ligado, FTP/FTPS desabilitado,
# startup file da aplicacao e limite de start do container.
#
# As variaveis de ambiente da aplicacao (DB_URL, DB_USERNAME, DB_PASSWORD e
# LLM_API_KEY) NAO sao definidas aqui: o pipeline de CD as aplica a partir do
# Variable Group do Azure DevOps.

foreach ($nome in @("RG", "WEBAPP")) {
    if (-not (Get-Variable -Name $nome -ValueOnly -ErrorAction SilentlyContinue)) {
        Write-Host "Variavel `$$nome nao definida. Rode primeiro: . .\00-Variaveis.ps1" -ForegroundColor Red
        exit 1
    }
}

az webapp config set -g $RG -n $WEBAPP --always-on true --ftps-state Disabled

# Startup file fixo: o deploy do pipeline publica o jar como app.jar em
# /home/site/wwwroot. Fica aqui (uma vez, na criacao da infra) e nao no CD,
# porque cada "az webapp config set" reinicia o Web App.
az webapp config set -g $RG -n $WEBAPP --startup-file "java -jar /home/site/wwwroot/app.jar"

# Limite de start do container em segundos. O start do Spring Boot com Flyway
# no B1 pode passar do padrao de 230 s, e o App Service reinicia o container em
# ciclo. O pipeline de CD tambem aplica este valor (nao e segredo).
az webapp config appsettings set -g $RG -n $WEBAPP --settings WEBSITES_CONTAINER_START_TIME_LIMIT=900
