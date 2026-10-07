# Variaveis compartilhadas pelos scripts de infraestrutura do KuraVet.
#
# Carregue este arquivo com dot-source, na mesma sessao do PowerShell,
# antes de rodar os demais scripts:
#
#     . .\00-Variaveis.ps1
#
# Nenhum segredo fica nestes scripts. As configuracoes sensiveis da aplicacao
# vem do Variable Group do Azure DevOps e sao aplicadas pelo pipeline de CD.

$RG     = "rg-kuravet-rm562396"
$LOCAL  = "brazilsouth"   # se a politica da assinatura bloquear, usar mexicocentral
$PLANO  = "plan-kuravet-rm562396"
$WEBAPP = "kuravet-rm562396"
