cd# Cria o App Service Plan Linux (SKU B1) que hospeda o Web App.

foreach ($nome in @("RG", "LOCAL", "PLANO")) {
    if (-not (Get-Variable -Name $nome -ValueOnly -ErrorAction SilentlyContinue)) {
        Write-Host "Variavel `$$nome nao definida. Rode primeiro: . .\00-Variaveis.ps1" -ForegroundColor Red
        exit 1
    }
}

az appservice plan create -n $PLANO -g $RG --is-linux --sku B1 --location $LOCAL
