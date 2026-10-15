# Remove o Resource Group inteiro (plano, Web App e tudo o que estiver nele).
# A operacao e irreversivel, por isso exige digitar o nome do Resource Group.

foreach ($nome in @("RG")) {
    if (-not (Get-Variable -Name $nome -ValueOnly -ErrorAction SilentlyContinue)) {
        Write-Host "Variavel `$$nome nao definida. Rode primeiro: . .\00-Variaveis.ps1" -ForegroundColor Red
        exit 1
    }
}

$confirmacao = Read-Host "Isto apaga o Resource Group '$RG' e todos os recursos dele. Digite o nome do Resource Group para confirmar"
if ($confirmacao -ne $RG) {
    Write-Host "Confirmacao nao confere. Nada foi removido." -ForegroundColor Yellow
    exit 1
}

az group delete -n $RG --yes
