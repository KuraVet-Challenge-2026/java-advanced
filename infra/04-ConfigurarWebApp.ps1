# Configura o Web App

foreach ($nome in @("RG", "WEBAPP")) {
    if (-not (Get-Variable -Name $nome -ValueOnly -ErrorAction SilentlyContinue)) {
        Write-Host "Variavel `$$nome nao definida. Rode primeiro: . .\00-Variaveis.ps1" -ForegroundColor Red
        exit 1
    }
}

az webapp config set -g $RG -n $WEBAPP --always-on true --ftps-state Disabled

az webapp config set -g $RG -n $WEBAPP --startup-file "java -jar /home/site/wwwroot/app.jar"

az webapp config appsettings set -g $RG -n $WEBAPP --settings WEBSITES_CONTAINER_START_TIME_LIMIT=900
