# Test Push Notification (Admin Only)

Endpoint ini digunakan untuk mengirim push notification manual ke specific user (yang sudah login dan punya FCM Token).

**Prerequisite:**
1. Login sebagai Admin untuk mendapatkan `ADMIN_TOKEN`.
2. Pastikan user target (misal `agung`) sudah login via Android dan FCM Token-nya tersimpan di database.

## CURL Command (JSON Body)

```bash
curl --location 'http://localhost:8080/api/notifications/test-push' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer <ADMIN_TOKEN>' \
--data '{
    "username": "agung",
    "title": "Halo dari Backend",
    "message": "Ini adalah tes notifikasi real-time ke Android Anda!"
}'
```

**Response Success (200 OK):**
```json
{
    "status": "success",
    "message": "Notifikasi berhasil dikirim ke user: agung",
    "data": "Notifikasi berhasil dikirim ke user: agung"
}
```
