package com.gib.tiklasat.service;

import com.gib.tiklasat.dto.AddressCreateDto;
import com.gib.tiklasat.dto.AddressDto;
import com.gib.tiklasat.entity.Address;
import com.gib.tiklasat.entity.User;
import com.gib.tiklasat.exception.ResourceNotFoundException;
import com.gib.tiklasat.repository.AddressRepository;
import com.gib.tiklasat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service // Spring'e bunun bir Service (İş mantığı) sınıfı olduğunu söylüyoruz.
@RequiredArgsConstructor // Lombok'un bu harika özelliği, final olan repository'leri otomatik olarak içeri (inject) alır.
public class AddressService {

    // Veritabanı ile konuşacak kuryelerimiz (Repository'ler)
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    /**
     * 1. ADRESLERİ LİSTELEME
     * Kullanıcının e-postasını alıp, ona ait tüm adresleri DTO'ya (Kargo kutusuna) çevirip döneriz.
     */
    @Transactional(readOnly = true) // Sadece okuma yapacağımız için bunu eklemek performansı artırır.
    public List<AddressDto> getUserAddresses(String email) {

        // Adım 1: Önce bu e-postaya sahip kullanıcıyı veritabanından bul. Yoksa hata fırlat.
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        // Adım 2: AddressRepository'ye daha önce yazdığımız "findByUserId" metodunu kullanarak adresleri getir.
        List<Address> addresses = addressRepository.findByUserId(user.getId());

        // Adım 3: Gelen bu Entity (Veritabanı) listesini, AddressDto listesine çevir ve gönder.
        return addresses.stream()
                .map(AddressDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 2. YENİ ADRES EKLEME
     * Kullanıcıdan gelen AddressCreateDto bilgilerini alıp, veritabanına yeni bir kayıt açarız.
     */
    @Transactional
    public AddressDto addAddress(String email, AddressCreateDto dto) {

        // Adım 1: Adresi kim ekliyor? Kullanıcıyı bul.
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        // Adım 2: Tertemiz, yepyeni ve boş bir Adres nesnesi oluştur.
        Address address = new Address();

        // Adım 3: DTO'dan (React formundan) gelen verileri bu boş adrese doldur.
        address.setTitle(dto.getTitle());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setFullAddress(dto.getFullAddress());

        // Eğer React tarafından "isDefault" gönderilmemişse, otomatik false yapalım.
        address.setDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false);

        // Adım 4: Bu adres kimin? Adresi kullanıcıya bağla (Foreign Key ilişkisini burada kuruyoruz).
        address.setUser(user);

        // Adım 5: Adresi veritabanına kaydet. (Kaydedince veritabanı buna otomatik ID ve Tarih verecek)
        Address savedAddress = addressRepository.save(address);

        // Adım 6: Kaydedilmiş (ID'si olan) adresi DTO'ya çevirip React'a "Al, başarıyla kaydettim" diye dönüyoruz.
        return AddressDto.fromEntity(savedAddress);
    }

    /**
     * 3. ADRES GÜNCELLEME
     * Var olan bir adresin ID'sini ve yeni bilgileri alır, üzerine yazar.
     */
    @Transactional
    public AddressDto updateAddress(String email, UUID addressId, AddressCreateDto dto) {

        // Adım 1: Kullanıcıyı bul
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        // Adım 2: Güncellenmek istenen adresi bul.
        // (Güvenlik önlemi olarak sadece kendi adresini bulmasını sağlıyoruz)
        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Adres bulunamadı veya size ait değil"));

        // Adım 3: Eski bilgileri, formdan gelen yeni bilgilerle değiştir.
        address.setTitle(dto.getTitle());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setFullAddress(dto.getFullAddress());
        if (dto.getIsDefault() != null) {
            address.setDefault(dto.getIsDefault());
        }

        // Adım 4: Güncellenmiş haliyle veritabanına kaydet (save metodu ID'si olan veriyi günceller)
        Address updatedAddress = addressRepository.save(address);

        return AddressDto.fromEntity(updatedAddress);
    }

    /**
     * 4. ADRES SİLME
     */
    @Transactional
    public void deleteAddress(String email, UUID addressId) {

        // Adım 1: Kullanıcıyı bul
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        // Adım 2: Adresi bul (Başkasının adresini silmesin diye id ve userId ile arıyoruz)
        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Adres bulunamadı veya size ait değil"));

        // Adım 3: Adresi veritabanından sil.
        addressRepository.delete(address);
    }
}