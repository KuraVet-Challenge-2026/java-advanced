$RG        = "rg-kuravet-rm562396"
$LOCAL     = "mexicocentral"
$PLANO     = "plan-kuravet-rm562396"
$WEBAPP    = "kuravet-rm562396"
$SQLSERVER = "sqlserver-kuravet-rm562396"
$SQLDB     = "bd_kuravet"
$SQLUSER   = "kuravetadmin"
$SQLPASS = Read-Host "Senha do administrador do SQL Server" -AsSecureString
$SQLPASS = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($SQLPASS))