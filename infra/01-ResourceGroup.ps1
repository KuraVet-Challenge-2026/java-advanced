# Cria o Resource Group que agrupa todos os recursos do KuraVet.

foreach ($nome in @("RG", "LOCAL")) {
    if (-not (Get-Variable -Name $nome -ValueOnly -ErrorAction SilentlyContinue)) {
        Write-Host "Variavel `$$nome nao definida. Rode primeiro: . .\00-Variaveis.ps1" -ForegroundColor Red
        exit 1
    }
}

az group create -n $RG -l $LOCAL
