$smtp = New-Object Net.Mail.SmtpClient("smtp.gmail.com", 587)
$smtp.EnableSsl = $true
$smtp.Credentials = New-Object System.Net.NetworkCredential("agungramadhani2409@gmail.com", "urzidiwxkzedxdrz")
try {
    $smtp.Send("agungramadhani2409@gmail.com", "agungramadhani2409@gmail.com", "Test Loanova", "Test email dari app")
    Write-Host "SUKSES: Email terkirim!"
} catch {
    Write-Host "GAGAL: $($_.Exception.Message)"
    Write-Host "DETAIL: $($_.Exception.InnerException.Message)"
}
