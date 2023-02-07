package com.otcengineering.white_app.utils;

import com.otc.alice.api.model.Shared;
import com.otcengineering.white_app.MyApp;

public class CloudErrorHandler {
    public static String handleError(final Shared.OTCStatus status) {
        return handleError(status.getNumber());
    }

    public static String handleError(final int errorcode) {
        if (MyApp.getUserLocale().getLanguage().equals("in")) {
            return handleErrorBahasa(errorcode);
        } else {
            return handleErrorEnglish(errorcode);
        }
    }

    private static String handleErrorEnglish(final int errorcode) {
        String errorMsg;
        switch (errorcode)
        {
            case -1: errorMsg = "No internet connection.";              break;
            case  1: errorMsg = "Success.";                             break;
            case  2: errorMsg = "Unauthorized.";                        break;
            case  3: errorMsg = "No authorization header.";             break;
            case  4: errorMsg = "Server error.";                        break;
            case  5: errorMsg = "Email not found.";                     break;
            case  6: errorMsg = "Email already used.";                  break;
            case  7: errorMsg = "Username already used.";               break;
            case  8: errorMsg = "Invalid username or password";         break;
            case  9: errorMsg = "Required fields missing.";             break;
            case 10: errorMsg = "Invalid authorization.";               break;
            case 11: errorMsg = "User disabled.";                       break;
            case 12: errorMsg = "User blocked.";                        break;
            case 13: errorMsg = "Malformed password.";                  break;
            case 14: errorMsg = "Malformed email.";                     break;
            case 15: errorMsg = "Malformed username.";                  break;
            case 16: errorMsg = "No activation pending.";               break;
            case 17: errorMsg = "Malformed car number.";                break;
            case 18: errorMsg = "Malformed VIN.";                       break;
            case 19: errorMsg = "Model not found.";                     break;
            case 20: errorMsg = "Profile already exists.";              break;
            case 21: errorMsg = "Malformed date.";                      break;
            case 22: errorMsg = "User profile required.";               break;
            case 23: errorMsg = "Invalid route type.";                  break;
            case 24: errorMsg = "Invalid GPX.";                         break;
            case 25: errorMsg = "Route not found.";                     break;
            case 26: errorMsg = "Country not found.";                   break;
            case 27: errorMsg = "No data.";                             break;
            case 28: errorMsg = "User not ranked.";                     break;
            case 29: errorMsg = "File not found.";                      break;
            case 30: errorMsg = "Dongle Serial Number not found.";      break;
            case 31: errorMsg = "Dongle Serial Number not unique.";     break;
            case 32: errorMsg = "User not enabled.";                    break;
            case 33: errorMsg = "User folder not found.";               break;
            case 34: errorMsg = "POI not found.";                       break;
            case 35: errorMsg = "Route already deleted.";               break;
            case 36: errorMsg = "Invalid phone activation code.";       break;
            case 37: errorMsg = "Route not autosaved.";                 break;
            case 38: errorMsg = "Malformed Dongle Serial Number.";      break;
            case 39: errorMsg = "Activation pending.";                  break;
            case 40: errorMsg = "User not found.";                      break;
            case 41: errorMsg = "User status not valid.";               break;
            case 42: errorMsg = "VIN not found.";                       break;
            case 43: errorMsg = "VIN not unique.";                      break;
            case 44: errorMsg = "Dongle Serial Number already exists."; break;
            case 45: errorMsg = "Last car position not found.";         break;
            case 46: errorMsg = "Description exceeds max length.";      break;
            case 47: errorMsg = "Car status not found.";                break;
            case 48: errorMsg = "VIN already exists.";                  break;
            case 49: errorMsg = "Vechicle not found.";                  break;
            case 50: errorMsg = "Dealer not found.";                    break;
            case 51: errorMsg = "Post not found.";                      break;
            case 52: errorMsg = "Already liked.";                       break;
            case 53: errorMsg = "Already disliked.";                    break;
            case 54: errorMsg = "Message exceeds max length.";          break;
            case 55: errorMsg = "Request not found.";                   break;
            case 56: errorMsg = "Profile not visible.";                 break;
            case 57: errorMsg = "Malformed YouTube URL.";               break;
            case 58: errorMsg = "Exceeds max document number.";         break;
            case 59: errorMsg = "New version not found.";               break;
            case 60: errorMsg = "Malformed firmware version.";          break;
            case 61: errorMsg = "Malformed UUID.";                      break;
            case 62: errorMsg = "SSL required.";                        break;
            case 63: errorMsg = "Malformed dealership name.";           break;
            case 64: errorMsg = "Exceeds max file size.";               break;
            case 65: errorMsg = "Invalid file.";                        break;
            case 66: errorMsg = "Password recovery already exists.";    break;
            case 67: errorMsg = "Phone not found.";                     break;
            case 68: errorMsg = "Not exceeds minimum waiting time.";    break;
            case 69: errorMsg = "Exceeds max attemps.";                 break;
            case 70: errorMsg = "Firmware history not found.";          break;
            case 71: errorMsg = "Firmware update already answered.";    break;
            case 72: errorMsg = "Firmware version already exists.";     break;
            case 73: errorMsg = "Firmware file not found";              break;
            case 74: errorMsg = "Profile not exists.";                  break;
            case 75: errorMsg = "Invalid user email.";                  break;
            case 76: errorMsg = "Invalid user scope.";                  break;
            case 77: errorMsg = "Invalid page number.";                 break;
            case 78: errorMsg = "Notification not found.";              break;
            case 79: errorMsg = "Notification already deleted.";        break;
            case 80: errorMsg = "Invalid CRC.";                         break;
            case 81: errorMsg = "New mobile.";                          break;
            case 82: errorMsg = "Invalid dongle serial number.";        break;
            case 83: errorMsg = "Invalid dongle MAC.";                  break;
            case 84: errorMsg = "Invalid IMEI.";                        break;
            case 85: errorMsg = "Malformed IMEI.";                      break;
            case 86: errorMsg = "Store route not allowed.";             break;
            case 87: errorMsg = "In final period.";                     break;
            case 88: errorMsg = "Not in service period.";               break;
            case 89: errorMsg = "User bluetooth reporting not enabled.";break;
            case 90: errorMsg = "Route already exists";                 break;
            case 91: errorMsg = "Invalid currency code.";               break;
            case 92: errorMsg = "Invalid Hardware type.";               break;
            case 93: errorMsg = "Invalid Hardware scope.";              break;
            case 94: errorMsg = "Promo already purchased.";             break;
            case 95: errorMsg = "Invalid item ID.";                     break;
            case 96: errorMsg = "Invalid User token.";                  break;
            case 97: errorMsg = "Failed transaction.";                  break;
            case 98: errorMsg = "Invalid item type.";                   break;
            case 99: errorMsg = "Invalid item.";                        break;
            case 100: errorMsg = "Invalid date range.";                 break;
            case 101: errorMsg = "This date range has no orders.";      break;
            case 102: errorMsg = "This item is no longer available.";   break;
            case 103: errorMsg = "The shopping cart is empty.";         break;
            case 104: errorMsg = "The shipping cost is invalid.";       break;
            default: errorMsg = "Not error at all.";                    break;
        }

        return errorMsg;
    }

    private static String handleErrorBahasa(final int errorcode) {
        String errorMsg;
        switch (errorcode)
        {
            case -1: errorMsg = "Tidak ada koneksi internet.";                  break;
            case  1: errorMsg = "Sukses.";                                      break;
            case  2: errorMsg = "Unauthorized.";                                break;
            case  3: errorMsg = "No authorization header.";                     break;
            case  4: errorMsg = "Kesalahan Server.";                            break;
            case  5: errorMsg = "Email tidak ditemukan.";                       break;
            case  6: errorMsg = "Email telah digunakan.";                       break;
            case  7: errorMsg = "Username telah digunakan.";                    break;
            case  8: errorMsg = "Username atau password tidak valid.";          break;
            case  9: errorMsg = "Isi kolom yang kosong.";                       break;
            case 10: errorMsg = "Invalid authorization.";                       break;
            case 11: errorMsg = "Akun pengguna tidak tersedia.";                break;
            case 12: errorMsg = "Akun pengguna terblokir.";                     break;
            case 13: errorMsg = "Password tidak sesuai.";                       break;
            case 14: errorMsg = "Email tidak sesuai.";                          break;
            case 15: errorMsg = "Username tidak sesuai.";                       break;
            case 16: errorMsg = "Tidak ada aktifasi yang tertunda.";            break;
            case 17: errorMsg = "Plat nomor tidak sesuai.";                     break;
            case 18: errorMsg = "VIN tidak sesuai.";                            break;
            case 19: errorMsg = "Model tidak ditemukan.";                       break;
            case 20: errorMsg = "Profil sudah digunakan.";                      break;
            case 21: errorMsg = "Tanggal tidak sesuai.";                        break;
            case 22: errorMsg = "Membutuhkan profil user.";                     break;
            case 23: errorMsg = "Tipe rute salah.";                             break;
            case 24: errorMsg = "GPX salah.";                                   break;
            case 25: errorMsg = "Rute tidak ditemukan.";                        break;
            case 26: errorMsg = "Negara tidak ditemukan.";                      break;
            case 27: errorMsg = "Tidak ada data.";                              break;
            case 28: errorMsg = "User tidak berperingkat.";                     break;
            case 29: errorMsg = "File tidak ditemukan.";                        break;
            case 30: errorMsg = "Nomor seri dongle tidak ditemukan.";           break;
            case 31: errorMsg = "Nomor seri dongle tidak unik.";                break;
            case 32: errorMsg = "User tidak aktif.";                            break;
            case 33: errorMsg = "Folder user tidak ditemukan.";                 break;
            case 34: errorMsg = "'Tempat menarik' tidak ditemukan.";            break;
            case 35: errorMsg = "Rute telah dihapus.";                          break;
            case 36: errorMsg = "Kode aktifasi tidak sesuai.";                  break;
            case 37: errorMsg = "Rute tidak tersimpan otomatis.";               break;
            case 38: errorMsg = "Nomor seri dongle tidak sesuai.";              break;
            case 39: errorMsg = "Pending aktifasi.";                            break;
            case 40: errorMsg = "User tidak ditemukan.";                        break;
            case 41: errorMsg = "User status tidak valid.";                     break;
            case 42: errorMsg = "VIN tidak ditemukan.";                         break;
            case 43: errorMsg = "VIN tidak unik.";                              break;
            case 44: errorMsg = "Nomor seri dongle telah digunakan.";           break;
            case 45: errorMsg = "Posisi terakhir kendaraan tidak ditemukan.";   break;
            case 46: errorMsg = "Deskripsi melebihi batas karakter.";           break;
            case 47: errorMsg = "Status kendaraan tidak ditemukan.";            break;
            case 48: errorMsg = "VIN telah digunakan.";                         break;
            case 49: errorMsg = "Kendaraan tidak ditemukan.";                   break;
            case 50: errorMsg = "Dealer tidak ditemukan.";                      break;
            case 51: errorMsg = "Post tidak ditemukan.";                        break;
            case 52: errorMsg = "Disukai.";                                     break;
            case 53: errorMsg = "Tidak disukai.";                               break;
            case 54: errorMsg = "Pesan melebihi batas karakter.";               break;
            case 55: errorMsg = "Permintaan tidak ditemukan.";                  break;
            case 56: errorMsg = "Profil tidak terlihat.";                       break;
            case 57: errorMsg = "YouTube URL tidak sesuai.";                    break;
            case 58: errorMsg = "Melebihi batas maksimum dokumen.";             break;
            case 59: errorMsg = "Versi terbaru tidak ditemukan.";               break;
            case 60: errorMsg = "Firmware version tidak sesuai.";               break;
            case 61: errorMsg = "UUID tidak sesuai.";                           break;
            case 62: errorMsg = "Membutuhkan SSL.";                             break;
            case 63: errorMsg = "Dealer tidak sesuai.";                         break;
            case 64: errorMsg = "Melebihi batas maksimum ukuran dokumen.";      break;
            case 65: errorMsg = "File tidak valid.";                            break;
            case 66: errorMsg = "Pemulihan password telah digunakan.";          break;
            case 67: errorMsg = "Telepon tidak ditemukan.";                     break;
            case 68: errorMsg = "Tidak melebihi batas tunggu minimum.";         break;
            case 69: errorMsg = "Melebihi batas percobaan.";                    break;
            case 70: errorMsg = "Firmware History tidak ditemukan.";            break;
            case 71: errorMsg = "Berhasil update firmware.";                    break;
            case 72: errorMsg = "Versi firmware sudah ada.";                    break;
            case 73: errorMsg = "Berkas firmware tidak ditemukan.";             break;
            case 74: errorMsg = "Profil tidak ada.";                            break;
            case 75: errorMsg = "Email pengguna tidak valid.";                  break;
            case 76: errorMsg = "Cakupan pengguna tidak valid.";                break;
            case 77: errorMsg = "Nomor halaman tidak valid.";                   break;
            case 78: errorMsg = "Pemberitahuan tidak ditemukan.";               break;
            case 79: errorMsg = "Pemberitahuan sudah terhapus.";                break;
            case 80: errorMsg = "CRC tidak valid.";                             break;
            case 81: errorMsg = "Ponsel baru.";                                 break;
            case 82: errorMsg = "Nomor seri dongle tidak valid.";               break;
            case 83: errorMsg = "Dongle MAC tidak valid.";                      break;
            case 84: errorMsg = "IMEI tidak valid.";                            break;
            case 85: errorMsg = "IMEI cacat.";                                  break;
            case 86: errorMsg = "Penyimpanan rute tidak diizinkan.";            break;
            case 87: errorMsg = "In final period.";                             break;
            case 88: errorMsg = "Not in service period.";                       break;
            case 89: errorMsg = "User bluetooth reporting not enabled.";        break;
            case 90: errorMsg = "Invalid currency code.";                       break;
            case 91: errorMsg = "Invalid currency code.";                       break;
            case 92: errorMsg = "Invalid Hardware type.";                       break;
            case 93: errorMsg = "Invalid Hardware scope.";                      break;
            case 94: errorMsg = "Promo already purchased.";                     break;
            case 95: errorMsg = "Invalid item ID.";                             break;
            case 96: errorMsg = "Invalid User token.";                          break;
            case 97: errorMsg = "Failed transaction.";                          break;
            case 98: errorMsg = "Invalid item type.";                           break;
            case 99: errorMsg = "Invalid item.";                                break;
            case 100: errorMsg = "Invalid date range.";                         break;
            case 101: errorMsg = "This date range has no orders.";              break;
            case 102: errorMsg = "This item is no longer available.";           break;
            case 103: errorMsg = "The shopping cart is empty.";                 break;
            case 104: errorMsg = "The shipping cost is invalid.";               break;
            default: errorMsg = "Tidak ada error.";                             break;
        }

        return errorMsg;
    }
}
