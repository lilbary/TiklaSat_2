package com.gib.tiklasat.service;

import com.gib.tiklasat.dto.UserDto;
import com.gib.tiklasat.dto.UserRegisterDto;
import com.gib.tiklasat.entity.User;
import com.gib.tiklasat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // KULLANICI KAYIT OLMA (REGISTER) METODU
    @Transactional
    public UserDto register(UserRegisterDto request) {
        
        // 1. KURAL: Bu e-posta daha önce alınmış mı?
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Bu e-posta adresi zaten kullanılıyor!");
        }

        // 2. KURAL: Şifre en az 6 karakter mi?
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new RuntimeException("Şifreniz en az 6 karakter olmalıdır!");
        }

        // 3. Veritabanına kaydedilecek boş bir Entity (User) oluştur.
        User newUser = new User();
        
        // 4. Dışarıdan gelen formdaki (request) bilgileri Entity'ye aktar.
        newUser.setEmail(request.getEmail());
        newUser.setFullName(request.getFullName());
        newUser.setPassword(request.getPassword()); // İleride buraya şifreleme (hash) ekleyeceğiz!
        newUser.setPhone(request.getPhone());

        // 5. Veritabanına kaydet.
        newUser = userRepository.save(newUser);

        // 6. Kaydolan kullanıcıyı şifresinden arındırarak (UserDto ile süzerek) geri döndür.
        return UserDto.fromEntity(newUser);
    }

    // TEST AMAÇLI: Tüm kullanıcıları listeleme
    @Transactional(readOnly = true)
    public java.util.List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserDto::fromEntity)
                .collect(java.util.stream.Collectors.toList());
    }
}
